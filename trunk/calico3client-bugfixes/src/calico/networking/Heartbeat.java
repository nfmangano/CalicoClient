package calico.networking;

import calico.*;
import calico.components.*;
import calico.components.grid.*;

/**
 * This sends a heartbeat signal to the server, and does some cleanup functions
 *
 * @author Mitch Dempsey
 */
public class Heartbeat extends Thread
{
	public void run()
	{
		try
		{
			while(true)
			{
				//doIHasMaster
				Thread.sleep(5000);
			
				//Networking.send(new CalicoPacket(NetworkCommand.HEARTBEAT));
			}//while
		}
		catch(Exception e)
		{
			
		}
	}//run
}
