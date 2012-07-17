package calico.plugins.analysis.components.activitydiagram.inputhandlers;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.CConnector;
import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.CGroupInputHandler;
import calico.networking.Networking;
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
			 long new_uuid = Calico.uuid();
			 CConnectorController.connectors.put(new_uuid, new ControlFlow(new_uuid, CCanvasController.getCurrentUUID(), CalicoDataStore.PenColor, CalicoDataStore.PenThickness, CStrokeController.strokes.get(strokeUID).getRawPolygon()));
     		CConnectorController.snapConnectorToEdge(new_uuid, this.uuid);
     		CConnectorController.snapConnectorToEdge(new_uuid, targetGroup);
            
            CStrokeController.delete(strokeUID);
     		
    		// Add to the canvas
    		CCanvasController.no_notify_add_child_connector( CCanvasController.getCurrentUUID(), new_uuid);
    		
    		// We need to notify the groups 
    		CGroupController.no_notify_add_connector(CConnectorController.connectors.get(new_uuid).getAnchorUUID(CConnector.TYPE_HEAD), new_uuid);
    		CGroupController.no_notify_add_connector(CConnectorController.connectors.get(new_uuid).getAnchorUUID(CConnector.TYPE_TAIL), new_uuid);
    		
    		Networking.send(CConnectorController.connectors.get(new_uuid).getUpdatePackets()[0]);
         }
         else {
        	 
        	 super.actionStrokeToAnotherGroup(strokeUID, targetGroup);
        	 
         }

	}

	

}
