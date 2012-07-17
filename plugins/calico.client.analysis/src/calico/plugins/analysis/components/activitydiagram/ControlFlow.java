package calico.plugins.analysis.components.activitydiagram;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.Color;
import java.awt.Polygon;

import calico.components.CConnector;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.analysis.AnalysisNetworkCommands;
import calico.plugins.analysis.components.AnalysisComponent;

public class ControlFlow extends CConnector implements AnalysisComponent{

	private static final long serialVersionUID = 1L;
	
	/** Declares that we are able to load this control flow */
	protected int networkLoadCommand = AnalysisNetworkCommands.ANALYSIS_CONTROL_FLOW_LOAD;
	
	/** If this control flow is going out from a decision node then this represents its probability*/
	double probability;
	
	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

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
	
	/**
	 * Serialize this control flow in a packet
	 */
	@Override
	public CalicoPacket[] getUpdatePackets(long uuid, long cuid) {
		//Creates the packet for saving this CGroup
		CalicoPacket packet = super.getUpdatePackets(uuid, cuid)[0];

		//Appends my own informations to the end of the packet
		packet.putDouble(this.probability);

		return new CalicoPacket[] { packet };

	}
	
	@Override
	protected ObjectArrayList<Class<?>> internal_getBubbleMenuButtons()
	{
		ObjectArrayList<Class<?>> bubbleMenuButtons = new ObjectArrayList<Class<?>>(); 
		bubbleMenuButtons.add(calico.components.bubblemenu.connectors.ConnectorLinearizeButton.class);
		bubbleMenuButtons.add(calico.components.bubblemenu.connectors.ConnectorMakeStrokeButton.class);
		bubbleMenuButtons.add(calico.components.bubblemenu.connectors.ConnectorMoveHeadButton.class);
		bubbleMenuButtons.add(calico.components.bubblemenu.connectors.ConnectorMoveTailButton.class);
		//motta.lrd: the probability distribution button for the analysis
		bubbleMenuButtons.add(calico.plugins.analysis.components.buttons.ProbabilityBubbleButton.class);

		return bubbleMenuButtons;
	}
	
	public String toString(){
		return "ControlFlow: " + this.uuid + "[" + this.getIncomingNode() + "<->" + this.getOutgoingNode() + "]";
	}

}
