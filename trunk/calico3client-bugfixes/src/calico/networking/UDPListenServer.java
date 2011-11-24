package calico.networking;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import calico.Calico;
import calico.modules.*;
import calico.networking.netstuff.CalicoPacket;


/**
 * This reads packets from the network and then adds them to the packet queue
 *
 * @author Mitch Dempsey
 */
public class UDPListenServer implements Runnable
{
	public UDPListenServer()
	{
		
	}
	
	public void run()
	{
		try
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
			
			while(true)
			{
				buffer.clear();
				
				int bytesRead = Networking.udpChannel.read(buffer);

				if(bytesRead==0)
				{
					// none
					//return null;
				}
				else if(bytesRead==-1)
				{
					// discon
					//return null;
				}
				else
				{
					//System.out.println("Bytes Read: "+bytesRead);
					byte[] data = new byte[bytesRead];
					System.arraycopy(buffer.array(), 0, data, 0, data.length);
					//CalicoPacket pack = new CalicoPacket(data);
					PacketHandler.receive( new CalicoPacket(data) );
					
				}
				// TESTING
				//PacketHandler.receive(new CalicoPacket(data2));
				
				
			}//while
		}
		catch(IllegalStateException ise)
		{
			Calico.logger.fatal("The receive queue has exploded.");
			ise.printStackTrace();
		}
		catch(Exception e)
		{
			//Networking.recvQueue.put(new CalicoPacket(rdata));
			e.printStackTrace();
		}
	}
}


