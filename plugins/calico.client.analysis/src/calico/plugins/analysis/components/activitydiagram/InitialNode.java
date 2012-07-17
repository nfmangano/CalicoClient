package calico.plugins.analysis.components.activitydiagram;

import java.awt.BasicStroke;
import java.awt.Color;

import calico.CalicoDraw;
import calico.CalicoOptions;
import calico.components.CConnector;
import calico.components.CGroup;
import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
import calico.plugins.analysis.components.AnalysisComponent;

public class InitialNode extends CGroup implements AnalysisComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InitialNode(long uuid, long cuid, long puid) {
		super(uuid, cuid, puid);
		// TODO Auto-generated constructor stub
		color = new Color(245,245,245);
	}
	
	public String toString(){
		return "Initial Node: " + this.uuid + "\n";
	}
	
	/**
	 * Returns the outgoing connector 
	 */
	public ControlFlow getOutgoingConnector(){
		for(long cuuid: this.childConnectors){
			//if I am the head of this connector
			if(CConnectorController.connectors.get(cuuid) instanceof ControlFlow && CConnectorController.connectors.get(cuuid).getAnchorUUID(CConnector.TYPE_TAIL)==this.uuid){
				return (ControlFlow)CConnectorController.connectors.get(cuuid);
			}
		}
		//no connector found
		return null;
	}


}

