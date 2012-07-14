package calico.plugins.analysis.components.activitydiagram;

import java.util.HashMap;
import java.util.HashSet;

import calico.components.*;
import calico.controllers.CConnectorController;
import calico.plugins.analysis.components.AnalysisComponent;

public class DecisionNode extends CGroup implements AnalysisComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<Long,Double> probabilityPaths=new HashMap<Long,Double>();

	public DecisionNode(long uuid, long cuid, long puid) {
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
	
	public void addProbabilityPath(long controlFlowID, double probability){
		this.probabilityPaths.put(controlFlowID, probability);
	}

	public HashMap<Long, Double> getProbabilityPaths(){
		return probabilityPaths;
	}
	
	public String toString(){
		String s="Decision node: ";
		for(long cuuid: probabilityPaths.keySet()){
			s=s+" "+ cuuid + "[" +probabilityPaths.get(cuuid) + "]";
		}
		return s+"\n";
	}

}
