package calico.plugins.analysis.components.activitydiagram;

import java.util.HashMap;
import java.util.HashSet;

import calico.components.CConnector;
import calico.components.CGroup;
import calico.controllers.CConnectorController;
import calico.plugins.analysis.components.AnalysisComponent;

public class ForkNode extends CGroup implements AnalysisComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ForkNode(long uuid, long cuid, long puid) {
		super(uuid, cuid, puid);
		// TODO Auto-generated constructor stub
	}

	public HashSet<ControlFlow> getIncomingPaths(){
		HashSet<ControlFlow> incomingpaths=new HashSet<ControlFlow>();
		
		for(long cuuid: this.childConnectors){
			//if I am the head of this connector
			if(CConnectorController.connectors.get(cuuid) instanceof ControlFlow && CConnectorController.connectors.get(cuuid).getAnchorUUID(CConnector.TYPE_HEAD)==this.uuid){
				incomingpaths.add((ControlFlow)CConnectorController.connectors.get(cuuid));
			}
		}
		return incomingpaths;				
	}

	public HashSet<ControlFlow> getOutgoingPaths(){
		HashSet<ControlFlow> outgoingpaths=new HashSet<ControlFlow>();
		
		for(long cuuid: this.childConnectors){
			//if I am the head of this connector
			if(CConnectorController.connectors.get(cuuid) instanceof ControlFlow && CConnectorController.connectors.get(cuuid).getAnchorUUID(CConnector.TYPE_TAIL)==this.uuid){
				outgoingpaths.add((ControlFlow)CConnectorController.connectors.get(cuuid));
			}
		}
		return outgoingpaths;				
	}
	
}
