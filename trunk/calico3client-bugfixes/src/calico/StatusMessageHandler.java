package calico;

import javax.swing.ProgressMonitor;

import org.apache.james.mime4j.util.StringArrayMap;

import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

public class StatusMessageHandler implements CalicoEventListener {

	private ProgressMonitor progressMonitor;
	
	private static StringArrayMap progressMonitors = new StringArrayMap();
	
	private static StatusMessageHandler instance = null;
	
	public static StatusMessageHandler getInstance()
	{
		if (instance == null)
			instance = new StatusMessageHandler();
		
		return instance;
	}
	
	private StatusMessageHandler()
	{
		CalicoEventHandler.getInstance().addListener(NetworkCommand.STATUS_SENDING_LARGE_FILE_START, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(NetworkCommand.STATUS_SENDING_LARGE_FILE, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(NetworkCommand.STATUS_SENDING_LARGE_FILE_FINISHED, this, CalicoEventHandler.PASSIVE_LISTENER);
	}
	
	@Override
	public void handleCalicoEvent(int event, CalicoPacket p) {
		if (event == NetworkCommand.STATUS_SENDING_LARGE_FILE_START)
		{

			
			if (progressMonitor == null)
			{
				p.rewind();
				p.getInt();
				int progress = (int) ( p.getDouble() / p.getDouble() * 100); 
				String message = p.getString();
				
				startProgressMonitor(message);
			}
		}
		else if (event == NetworkCommand.STATUS_SENDING_LARGE_FILE)
		{

			p.rewind();
			p.getInt();
			int progress = Math.abs((int) ( p.getDouble() / p.getDouble() * 100)); 
			String message = p.getString();

			if (progressMonitor == null)
				startProgressMonitor(message);
			progressMonitor.setNote(message);
			progressMonitor.setProgress(progress);
		}
		else if (event == NetworkCommand.STATUS_SENDING_LARGE_FILE_FINISHED)
		{
			p.rewind();
			p.getInt();
			int progress = (int) ( p.getDouble() / p.getDouble() * 100); 
			String message = p.getString();
			
			if (progressMonitor == null /*|| progressMonitor.getNote().compareTo(message) != 0 */)
				return;
			
			progressMonitor.close();
			progressMonitor = null;
		}	
	}
	
	
	public void startProgressMonitor(String message)
	{
		if (progressMonitor != null)
			return;
		progressMonitor = new ProgressMonitor(null,
                message,
                "", 0, 100);
		progressMonitor.setProgress(0);
		progressMonitor.setMillisToPopup(1);
		progressMonitor.setMillisToDecideToPopup(1);
	}

}
