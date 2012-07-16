package calico.plugins.analysis.components.activitydiagram;

import calico.components.CConnector;
import calico.components.CGroup;
import calico.controllers.CConnectorController;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.analysis.AnalysisNetworkCommands;
import calico.plugins.analysis.components.AnalysisComponent;

public class ActivityNode extends CGroup implements AnalysisComponent {

	private static final long serialVersionUID = 1L;
	
	/** The response time of this activity node */
	double responseTime = 1.0d;
	
	/** Declares that we are able to load this activity node */
	protected int networkLoadCommand = AnalysisNetworkCommands.ANALYSIS_ACTIVITY_NODE_LOAD;

	public ActivityNode(long uuid, long cuid, long puid) {
		super(uuid, cuid, puid);
	}
	
	public double getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(double responseTime) {
		this.responseTime = responseTime;
	}

	/**
	 * Returns the incoming connector
	 * TODO: Refactor for usability. We can have more than one incoming connector
	 */
	public ControlFlow getIncomingConnector() {
		for (long cuuid : this.childConnectors) {
			// if I am the head of this connector
			if (CConnectorController.connectors.get(cuuid) instanceof ControlFlow
					&& CConnectorController.connectors.get(cuuid)
							.getAnchorUUID(CConnector.TYPE_HEAD) == this.uuid) {
				return (ControlFlow) CConnectorController.connectors.get(cuuid);
			}
		}
		// no connector found
		return null;
	}

	/**
	 * Returns the outgoing connector
	 * TODO: Refactor for usability. We can have more than one incoming connector
	 */
	public ControlFlow getOutgoingConnector() {
		for (long cuuid : this.childConnectors) {
			// if I am the head of this connector
			if (CConnectorController.connectors.get(cuuid) instanceof ControlFlow
					&& CConnectorController.connectors.get(cuuid)
							.getAnchorUUID(CConnector.TYPE_TAIL) == this.uuid) {
				return (ControlFlow) CConnectorController.connectors.get(cuuid);
			}
		}
		// no connector found
		return null;
	}

	/**
	 * Serialize this activity node in a packet
	 */
	@Override
	public CalicoPacket[] getUpdatePackets(long uuid, long cuid, long puid,
			int dx, int dy, boolean captureChildren) {
		//Creates the packet for saving this CGroup
		CalicoPacket packet = super.getUpdatePackets(uuid, cuid, puid, dx, dy,
				captureChildren)[0];

		//Appends my own informations to the end of the packet
		packet.putDouble(responseTime);

		return new CalicoPacket[] { packet };

	}

	public String toString() {
		return "Activity Node: " + this.uuid + "\n";
	}



}
