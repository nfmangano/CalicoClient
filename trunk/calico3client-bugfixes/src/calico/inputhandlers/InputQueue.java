package calico.inputhandlers;

import java.util.concurrent.ArrayBlockingQueue;

import calico.*;
import calico.modules.ErrorMessage;


public class InputQueue implements Runnable
{
	private static ArrayBlockingQueue<InputEventInfo> eventQueue = new ArrayBlockingQueue<InputEventInfo>(8192);
	
	public static void queue(InputEventInfo ev)
	{
		eventQueue.add(ev);
	}
	
	public void run()
	{
		
		//This method is a bit awkward in that it requires two while loops
		// The inner while loop cannot have the try catch within it because that slows down the processing of points significantly
		// That means that we need a second outer while loop to prevent this method from stopping
		while (true)
		{
			try
			{
				while(true)
				{
					CalicoInputManager.handleInputFromQueue(eventQueue.take());
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
}