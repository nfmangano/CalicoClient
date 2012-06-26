package calico.components.composable.connectors;

import java.awt.BasicStroke;
import java.awt.Stroke;

import calico.components.CConnector;
import calico.components.composable.Composable;
import calico.components.composable.ComposableElement;
import calico.controllers.CConnectorController;
import calico.networking.netstuff.ByteUtils;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

public class LineStyleElement extends ComposableElement {

	private Stroke originalStroke, newStroke;
	
	public LineStyleElement(long uuid, long cuuid, Stroke newStroke, Stroke originalStroke) {
		super(uuid, cuuid);

		this.newStroke = newStroke;
		this.originalStroke = originalStroke;
	}

	public void applyElement() 
	{
		if (!CConnectorController.exists(cuuid))
			return;
		
		CConnector connector = CConnectorController.connectors.get(cuuid);
		connector.setStroke(newStroke);
	}
	
	public void removeElement()
	{
		if (!CConnectorController.exists(cuuid))
			return;
		
		CConnector connector = CConnectorController.connectors.get(cuuid);
		connector.setStroke(originalStroke);
	}
	
	public Composable getComposable()
	{
		return CConnectorController.connectors.get(cuuid);
	}
	
	public CalicoPacket getPacket(long uuid, long cuuid)
	{
		BasicStroke n = (BasicStroke)newStroke;
		BasicStroke o = (BasicStroke)originalStroke;
		int nDashLength = (n.getDashArray() == null) ? 0 : n.getDashArray().length;
		int oDashLength = (o.getDashArray() == null) ? 0 : o.getDashArray().length;
		
		int packetSize = ByteUtils.SIZE_OF_INT  					//Command
				+ ByteUtils.SIZE_OF_INT  							//Element Type
				+ (2 * ByteUtils.SIZE_OF_LONG) 						//UUID & CUUID
				+ (6 * ByteUtils.SIZE_OF_INT) 						//Line Width, End Cap, Line Join, MiterLimit, Dash Array Length, Dash Phase
				+ (nDashLength * ByteUtils.SIZE_OF_INT) //Dash Array
				+ (6 * ByteUtils.SIZE_OF_INT) 						//Line Width, End Cap, Line Join, MiterLimit, Dash Array Length, Dash Phase
				+ (oDashLength * ByteUtils.SIZE_OF_INT); //Dash Array

		
		CalicoPacket packet = new CalicoPacket(packetSize);
		
		packet.putInt(NetworkCommand.ELEMENT_ADD);
		packet.putInt(ComposableElement.TYPE_LINESTYLE);
		packet.putLong(uuid);
		packet.putLong(cuuid);
		
		//New stroke
		packet.putFloat(n.getLineWidth());
		packet.putInt(n.getEndCap());
		packet.putInt(n.getLineJoin());
		packet.putFloat(n.getMiterLimit());
		packet.putInt(nDashLength);
		for (int i = 0; i < nDashLength; i++)
		{
			packet.putFloat(n.getDashArray()[i]);
		}
		packet.putFloat(n.getDashPhase());
		
		//Original stroke
		packet.putFloat(o.getLineWidth());
		packet.putInt(o.getEndCap());
		packet.putInt(o.getLineJoin());
		packet.putFloat(o.getMiterLimit());
		packet.putInt(oDashLength);
		for (int i = 0; i < oDashLength; i++)
		{
			packet.putFloat(o.getDashArray()[i]);
		}
		packet.putFloat(o.getDashPhase());
		
		
		return packet;
	}
	
	public CalicoPacket getPacket()
	{
		return getPacket(this.uuid, this.cuuid);
	}
}
