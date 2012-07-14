package calico.plugins.analysis.components.activitydiagram;

import java.util.HashSet;

import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;

/**
 * Implements the funcionalities to handle
 * the activity diagram model drawn in the calico gui
 * @author motta
 *
 */
public class ActivityDiagram {

	public static InitialNode getInitialNode(){
        for(long uuid: CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getChildGroups()){
        	if(CGroupController.groupdb.get(uuid) instanceof calico.plugins.analysis.components.activitydiagram.InitialNode){
				return (InitialNode) CGroupController.groupdb.get(uuid);
			}
        }
        return null;
	}
	
	public static HashSet<ControlFlow> getControlFlows(){
		HashSet<ControlFlow> flows=new HashSet<ControlFlow>();
		for(long uuid: CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getChildConnectors()){
			if(CConnectorController.connectors.get(uuid) instanceof ControlFlow){
				flows.add((ControlFlow) CConnectorController.connectors.get(uuid));
			}		
		}	
		return flows;
	}
	
}
