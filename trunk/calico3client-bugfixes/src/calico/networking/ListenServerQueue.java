package calico.networking;

import calico.*;
import calico.components.*;
import calico.modules.*;

/**
 * This reads packets from the queue and then sends them to the handler
 *
 * @author Mitch Dempsey
 */
public class ListenServerQueue extends Thread
{
	public void run()
	{
		try
		{
			while(true)
			{
				PacketHandler.receive(Networking.recvQueue.take());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			ErrorMessage.fatal("Network RX Queue Error");
		}
	}
}
	
