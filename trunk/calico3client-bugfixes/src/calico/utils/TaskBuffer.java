package calico.utils;

import javax.swing.SwingUtilities;

/**
 * Buffers repetitive tasks such that they are executed no more frequently than the specified
 * <code>minExecutionInterval</code>. The <code>client</code> notifies this buffer for each new task, and this buffer in
 * turn notifies the client when it is time to execute the tasks. The definition and execution of the tasks remain in
 * the hands of the client, so this buffer serves only as a scheduling mechanism. Client tasks are invoked on the AWT
 * EventDispatchThread for simplicity of thread coordination.
 * 
 * @author Byron Hawkins
 */
public class TaskBuffer
{
	public interface Client
	{
		void executeTasks();
	}

	private final Client client;
	private final long minExecutionInterval;

	private final Daemon daemon = new Daemon();
	private final ClientTask clientTask = new ClientTask();

	public TaskBuffer(Client client, long minExecutionInterval)
	{
		this.client = client;
		this.minExecutionInterval = minExecutionInterval;
	}

	public void start()
	{
		daemon.start();
	}

	public void taskPending()
	{
		daemon.wakeUp();
	}
	
	private String getThreadName()
	{
		return getClass().getSimpleName();
	}

	private class Daemon extends Thread
	{
		private long lastBroadcast = 0L;
		private boolean taskPending = false;

		Daemon()
		{
			super(getThreadName());

			setDaemon(true);
		}

		void wakeUp()
		{
			synchronized (this)
			{
				taskPending = true;
				notify();
			}
		}

		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					synchronized (this)
					{
						if (taskPending)
						{
							long bufferingPause;
							while ((bufferingPause = minExecutionInterval - (System.currentTimeMillis() - lastBroadcast)) > 0L)
							{
								try
								{
									wait(bufferingPause);
								}
								catch (InterruptedException wakeUpCall)
								{
								}
							}
							invokeTasks();
						}
						else
						{
							try
							{
								wait();
							}
							catch (InterruptedException wakeUpCall)
							{
							}

							if (taskPending && ((System.currentTimeMillis() - lastBroadcast) > minExecutionInterval))
							{
								invokeTasks();
							}
						}
					}
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
			}
		}

		private synchronized void invokeTasks()
		{
			taskPending = false;
			lastBroadcast = System.currentTimeMillis();
			SwingUtilities.invokeLater(clientTask);
		}
	}

	private class ClientTask implements Runnable
	{
		@Override
		public void run()
		{
			client.executeTasks();
		}
	}
}
