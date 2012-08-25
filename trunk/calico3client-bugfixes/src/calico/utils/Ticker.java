package calico.utils;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.controllers.CCanvasController;
import calico.perspectives.GridPerspective;

public class Ticker extends Thread
{
	private ReferenceArrayList<TickerTask> ticker_tasks = new ReferenceArrayList<TickerTask>();
	
	public static Ticker ticker = null;
	
	private long tickcount = 0;
	private long ticker_starttime = 0;
	
	public Ticker()
	{
		Ticker.ticker = this;
	}
	
	public void run()
	{
		System.out.println("TICKET");
		ticker_starttime = System.currentTimeMillis();
		while(true)
		{
			this.tickcount++;
			
			// DO A BUNCH OF STUFF HERE
			
			// Check for tasks
			if(this.ticker_tasks.size()>0)
			{
				try
				{
					long curtime = System.currentTimeMillis();
					for(int i=(this.ticker_tasks.size()-1);i>=0;i--)
					{
						// do we want to run it?
						if(this.ticker_tasks.get(i).run_after<curtime)
						{
							// Run it
							if(!this.ticker_tasks.get(i).runtask())
							{
								this.ticker_tasks.remove(i);
							}
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			

			if(CalicoDataStore.gridObject!=null && onTick(66) && GridPerspective.getInstance().isActive() && !Calico.isGridLoading )
			{
				CalicoDataStore.gridObject.updateCells();
			}
			
			// TODO: maybe record when the last input was, and only run this after some time has passed
			try {
				if(onTick(66) && CCanvasController.getCurrentUUID()!=0L) {//
					//System.out.println(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).isValid());
					
					//XXXXXXXX REMOVING THIS LINE TO SPEED THINGS UP XXXXXXX 
//					CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer().repaint();
					
					//System.out.println(CalicoDataStore.calicoObj.isActive());
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			
			if(onTick(CalicoOptions.core.tickrate*100) && !Calico.isGridLoading)
			{
				CCanvasController.no_notify_flush_dead_objects();
			}
			
			/*if(onTick(CalicoOptions.core.tickrate*CalicoOptions.core.hash_check_request_interval))
			{
				Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_REQUEST_HASH_CHECK, 0L));
			}*/
			
			//if(onTick(66))
			//{
			//	Calico.logger.debug("TICKRATE: "+getAverageTickrate());
			//}
			
			// ^^^^^^^^^^^^^^^^^^^^^^^^^
			try
			{
				Thread.sleep( (long) (1000.0/CalicoOptions.core.tickrate) );
			}
			catch(InterruptedException ie)
			{
				
			}
		}//while
	}//run
	
	private boolean onTick(int tick)
	{
		return ((this.tickcount%tick)==0);
	}
	
	public double getAverageTickrate()
	{
		double actual_tickrate = 0.0;
		
		double seconds = ((System.currentTimeMillis() - ticker_starttime)/1000.0);
		
		actual_tickrate = this.tickcount / seconds;
		
		return actual_tickrate;
	}
	
	
	public static void schedule(long whatTime, TickerTask task)
	{
		task.run_after = whatTime;
		Ticker.ticker.ticker_tasks.add(task);
	}
	
	// miliseconds
	public static void scheduleIn(int inWhatTime, TickerTask task)
	{
		schedule(System.currentTimeMillis() + inWhatTime, task);
	}
	
	
}


