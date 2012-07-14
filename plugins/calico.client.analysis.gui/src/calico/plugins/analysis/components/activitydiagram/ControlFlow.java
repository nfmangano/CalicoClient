package calico.plugins.analysis.components.activitydiagram;

import java.awt.Color;
import java.awt.Polygon;

import calico.components.CConnector;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;
import calico.plugins.analysis.components.AnalysisComponent;

public class ControlFlow extends CConnector implements AnalysisComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ControlFlow(long uuid, long cuid, Color color, float thickness,
			Polygon polygon) {
		super(uuid, cuid, color, thickness, polygon);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns the incoming node
	 * @return
	 */
	public AnalysisComponent getIncomingNode(){
		return (AnalysisComponent)CGroupController.groupdb.get(this.getAnchorUUID(TYPE_TAIL));
	}
	
	/**
	 * Returns the outgoing node
	 * @return
	 */
	public AnalysisComponent getOutgoingNode(){
		return (AnalysisComponent)CGroupController.groupdb.get(this.getAnchorUUID(TYPE_HEAD));
	}	
	
	public String toString(){
		return "ControlFlow: " + this.uuid + "[" + this.getIncomingNode() + "<->" + this.getOutgoingNode() + "]";
	}

}
