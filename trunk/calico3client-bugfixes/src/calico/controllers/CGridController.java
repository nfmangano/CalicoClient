package calico.controllers;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.grid.CGrid;
import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.modules.MessageObject;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.perspectives.GridPerspective;

public class CGridController {
	
	private static CGridController instance;
	
	public static CGridController getInstance()
	{
		if (instance == null)
			instance = new CGridController();
		return instance;
	}
	
	private CGridController()
	{
		CalicoEventHandler.getInstance().addListener(NetworkCommand.CONSISTENCY_FINISH, listener, CalicoEventHandler.ACTION_PERFORMER_LISTENER);
	}
	
	private static CalicoEventListener listener = new CalicoEventListener() {		

		@Override
		public void handleCalicoEvent(int event, CalicoPacket p) {
			if (event == NetworkCommand.CONSISTENCY_FINISH)
			{
				CONSISTENCY_FINISH(p);
			}
			
		}
	};
	
	private static void CONSISTENCY_FINISH(CalicoPacket p)
	{
		//long[] canvasuids = CCanvasController.getCanvasIDList();
		
		/*
		for(int can=0;can<canvasuids.length;can++)
		{
			CCanvasController.render(canvasuids[can]);
		}*/
		
		if( GridPerspective.getInstance().isActive() )
		{
			Calico cal = CalicoDataStore.calicoObj;
			
			cal.getContentPane().removeAll();
			cal.getContentPane().add( CGrid.getInstance().getComponent() );
			CGrid.getInstance().refreshCells();
	        cal.pack();
	        cal.setVisible(true);
			cal.repaint();
		}

		MessageObject.showNotice("The grid has loaded");
		Calico.isGridLoading = false;

	}
}
