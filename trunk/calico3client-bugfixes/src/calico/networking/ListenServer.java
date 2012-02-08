package calico.networking;

import it.unimi.dsi.fastutil.io.BinIO;

import java.awt.Component;
import java.io.*;
import java.net.*;

import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.components.grid.CGrid;
import calico.controllers.CCanvasController;
import calico.events.CalicoEventHandler;
import calico.modules.*;
import calico.networking.netstuff.ByteUtils;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;


/**
 * This reads packets from the network and then adds them to the packet queue
 *
 * @author Mitch Dempsey
 */
public class ListenServer implements Runnable
{

	public static Logger logger = Logger.getLogger(ListenServer.class.getName());
	
	private InputStream in = null;
	private static ProgressMonitor progressMonitor;
	private int previousProgress = 0;
	
	private long lastHeartBeatFromServer;
	
	public ListenServer()
	{
		openInputStream();
		
		if (CalicoDataStore.GridRows == 0 || CalicoDataStore.GridCols == 0)
			startProgressMonitor();
//			CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE_START, CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE_START, ((double)0), 
//					((double)100), "Synchronizing with server... "));
		
	}

	private void openInputStream() {
		try
		{
			in = Networking.socket.getInputStream();
		}
		catch(Exception e)
		{
			logger.error("Could not get input stream");
		}
	}
	
	public void run()
	{
		logger.debug("ListenServer thread is starting.");
//		try
//		{
		
			while(true)
			{
				lastHeartBeatFromServer = System.currentTimeMillis();
				try {
					if (!Networking.socket.isConnected())
					{
						throw new CalicoLostServerConnectionException();
					}
					else if((System.currentTimeMillis()-this.lastHeartBeatFromServer)>=CalicoOptions.network.timeout)
					{
						logger.debug("Server timeout: " + (System.currentTimeMillis()-this.lastHeartBeatFromServer));
						throw new CalicoLostServerConnectionException();
					}
					byte[] data = new byte[ByteUtils.SIZE_OF_INT];
					
					Networking.receivingPacketsFromServer = true;
					int available = in.available();
					while (available < 4)
					{
	//					logger.info("Input stream doesn't have enough byte, blocking for 10ms. (Need: " + 4 + ", has: " + available);
						Thread.sleep(1l);
						available = in.available();
						
						if((System.currentTimeMillis()-this.lastHeartBeatFromServer)>=CalicoOptions.network.timeout)
						{
							logger.debug("Server timeout: " + (System.currentTimeMillis()-this.lastHeartBeatFromServer));
							throw new CalicoLostServerConnectionException();
						}
						if (Networking.sendingPacketsToServer)
							lastHeartBeatFromServer = System.currentTimeMillis();
						
					}
					in.read(data);
					
					int size = ByteUtils.readInt(data, 0);
					
					if(size<=0)
					{
						logger.debug("PACKET SIZE: "+size);
						throw new CalicoLostServerConnectionException();
					}
					
					CalicoPacket tpack = new CalicoPacket(size);
					available = in.available();
					
					int offset = 0;
					
					boolean showProgressBar = false; //size > CalicoOptions.network.cluster_size;
					String msg = "Downloading large message from the server, please wait...";
					
					if (showProgressBar)
						CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE_START, CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE_START, 0, 1, msg));
					while (offset < size)
					{
			//			logger.info("Input stream doesn't have enough byte, blocking for 10ms. (Need: " + 4 + ", has: " + available);
						Thread.sleep(1l);
						available = in.available();
						if (offset + available > size)
							available = size - offset;
						in.read(tpack.getBuffer(), offset, available);
//						BinIO.loadBytes(this.in, tpack.getBuffer(), offset, available);
						offset += available;
//						if (size > CalicoOptions.network.cluster_size)
//						{
//							System.out.println("Loading large packet (" + size + "), " + (/() * 100 + " percent complete");
//						}
				        if (showProgressBar)
							CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE, 
									CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE, (double)offset, 
									(double)size, msg));	
					}
					if (showProgressBar)
						CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE_FINISHED, CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE_FINISHED, 1d, 1d, msg));
					
//					while (available < size)
//					{
//	//					logger.info("Input stream doesn't have enough byte, blocking for 10ms. (Need: " + size + ", has: " + available);
//						Thread.sleep(1l);
//						available = in.available();
//					}
					
//					in.read( tpack.getBuffer() );
					
					if(logger.isTraceEnabled())
					{
						logger.trace("rx " + tpack.toString());
					}
					
					// Catching this here so that it doesn't disconnect the client
					try
					{
						PacketHandler.receive(tpack);
					}
					catch (Exception e)
					{
						logger.warn("Failed to process packet " + tpack.toString());
					}
					Networking.receivingPacketsFromServer = false;
					
					if (Networking.connectionState != Networking.ConnectionState.Connected)
					{
						if (progressMonitor == null)
						{
//							CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE_START, CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE_START, ((double)0), 
//									((double)100), "Synchronizing with server... "));
							startProgressMonitor();
						}
						else if (tpack.getCommand() == NetworkCommand.CONSISTENCY_FINISH)
						{
//							CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE_FINISHED, CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE_FINISHED, ((double)100), 
//									((double)100), "Synchronizing with server... "));
							progressMonitor.close();
							progressMonitor = null;
							Networking.connectionState = Networking.ConnectionState.Connected;
							continue;
						}
						else if (progressMonitor.isCanceled()) {
			                System.exit(0);
			            }
						
						double cuuid = new Long(tpack.getCUUID()).doubleValue();
						double numCanvases = new Integer(CalicoDataStore.GridRows * CalicoDataStore.GridCols).doubleValue() + 1d;
						int progress = new Double(((cuuid*100d) / (numCanvases*100d)) * 100).intValue();
						if (progress < previousProgress)
							progress = previousProgress;
						
//						CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE, 
//								CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE, ((double)progress), 
//								((double)100), "Synchronizing with server... "));
											
						if (progress >= 0)
						{
							progressMonitor.setProgress(progress);
				            String message =
				                String.format("Completed %d%%.\n", progress);
				            progressMonitor.setNote(message);
	
						}
						previousProgress = progress;
					}
					else if (Networking.connectionState == Networking.connectionState.Connected && progressMonitor != null)
					{
						progressMonitor.close();
						progressMonitor = null;
					}
				}
				catch (InterruptedException e1) {
					e1.printStackTrace();
					logger.debug("ListenServer thread was interrupted.");
					break;
				}
				catch(Exception e)
				{
//					logger.error("Inputstream lost connection to the server! Attempting to reconnect...");
					
//					e.printStackTrace();
					try {
						Thread.sleep(1000l);
//						logger.debug("Attempting to connect in: ");
//						logger.debug("5... ");
//						Thread.sleep(1000l);
//						logger.debug("4... ");
//						Thread.sleep(1000l);
//						logger.debug("3... ");
//						Thread.sleep(1000l);
//						logger.debug("2... ");
//						Thread.sleep(1000l);
//						logger.debug("1... ");
//						Thread.sleep(1000l);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (!Networking.socket.isConnected())
						continue;

//					Networking.connectToServer();
					openInputStream();
				}
				
			}//while
//		}
//		catch(CalicoLostServerConnectionException clsce)
//		{
//			logger.fatal("We lost our connection to the server!");
//			try {
//				Networking.socket.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.exit(1);
//		}
//		catch(IllegalStateException ise)
//		{
//			logger.fatal("The receive queue has exploded.");
//			ise.printStackTrace();
//		}
//		
//		catch(IOException e)
//		{
//			e.printStackTrace();
//		}
//		catch(InterruptedException e)
//		{
//			logger.error(e.getMessage());
//			e.printStackTrace();
//		}
//		catch(Exception e)
//		{
//			//Networking.recvQueue.put(new CalicoPacket(rdata));
//			e.printStackTrace();
//		}
	}
	
	public static void startProgressMonitor()
	{
		progressMonitor = new ProgressMonitor(CalicoDataStore.calicoObj,
                "Synchronizing with server: " + CalicoDataStore.ServerHost,
                "", 0, 100);
		progressMonitor.setProgress(0);
		progressMonitor.setMillisToPopup(1);
		progressMonitor.setMillisToDecideToPopup(1);
	}
}


