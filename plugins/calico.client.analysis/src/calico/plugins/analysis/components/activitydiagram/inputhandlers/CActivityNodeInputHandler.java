package calico.plugins.analysis.components.activitydiagram.inputhandlers;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.CConnector;
import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.CGroupInputHandler;
import calico.plugins.analysis.components.AnalysisComponent;
import calico.plugins.analysis.components.activitydiagram.ControlFlow;

public class CActivityNodeInputHandler extends CGroupInputHandler {

	public CActivityNodeInputHandler(long u) {
		super(u);
		// TODO Auto-generated constructor stub
	}
	
//	@Override
	public void actionStrokeToAnotherGroup(long strokeUID, long targetGroup) {
		 if(CGroupController.groupdb.get(targetGroup) instanceof AnalysisComponent && CGroupController.groupdb.get(this.uuid) instanceof AnalysisComponent){
            CConnectorController.connectors.put(uuid, new ControlFlow(uuid, CCanvasController.getCurrentUUID(), CalicoDataStore.PenColor, CalicoDataStore.PenThickness, CStrokeController.strokes.get(strokeUID).getRawPolygon()));
     		CStrokeController.delete(strokeUID);
     		
    		// Add to the canvas
    		CCanvasController.no_notify_add_child_connector( CCanvasController.getCurrentUUID(), uuid);
    		
    		// We need to notify the groups 
    		CGroupController.no_notify_add_connector(CConnectorController.connectors.get(uuid).getAnchorUUID(CConnector.TYPE_HEAD), uuid);
    		CGroupController.no_notify_add_connector(CConnectorController.connectors.get(uuid).getAnchorUUID(CConnector.TYPE_TAIL), uuid);	
         }
         else{
//        	 CConnectorController.connectors.put(uuid, new CConnector(uuid, CCanvasController.getCurrentUUID(), CalicoDataStore.PenColor, CalicoDataStore.PenThickness, CStrokeController.strokes.get(strokeUID).getRawPolygon()));
        	 CStrokeController.show_stroke_bubblemenu(strokeUID, false);
         }
//		CConnectorController.no_notify_create(Calico.uuid(), CCanvasController.getCurrentUUID(), 0l, CalicoDataStore.PenColor, CalicoDataStore.PenThickness, this.uuid, targetGroup, strokeUID);

	}

	

}
