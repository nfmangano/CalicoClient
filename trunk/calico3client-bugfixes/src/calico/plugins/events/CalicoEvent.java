package calico.plugins.events;

import org.apache.log4j.Logger;

import calico.networking.netstuff.CalicoPacket;

public class CalicoEvent
{
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	
	public CalicoEvent()
	{
		
	}
	
	
	public final String getEventName()
	{
		return this.getClass().getSimpleName();
	}
	
	
	public void getPacketData(CalicoPacket p)
	{
		// To be implemented by the plugin
	}
	
	
	
	public void execute() throws Exception{}
	public void execute(Class<?> firingPlugin) throws Exception
	{
		execute();
	}
	
	// Logging
	protected void debug(String message){logger.debug(message);}
	protected void error(String message){logger.error(message);}
	protected void trace(String message){logger.trace(message);}
	protected void warn(String message){logger.warn(message);}
	protected void info(String message){logger.info(message);}
}
