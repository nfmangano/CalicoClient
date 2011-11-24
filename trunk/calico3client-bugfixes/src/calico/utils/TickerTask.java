package calico.utils;

public class TickerTask
{
	/**
	 * This is the timestamp after which the task should run
	 */
	public long run_after = 0L;
	

	/**
	 * This runs the task
	 * @return true to KEEPALIVE
	 */
	public boolean runtask()
	{
		return false;
	}
	
}
