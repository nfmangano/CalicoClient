package calico.components.composable.connectors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import calico.components.CConnector;
import calico.components.composable.Composable;
import calico.components.composable.ComposableElement;
import calico.controllers.CConnectorController;
import calico.networking.netstuff.ByteUtils;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

public class HighlightElement extends ComposableElement {
	
	private float transparency;
	private Stroke stroke;
	private Color color;

	public HighlightElement(long uuid, long cuuid, float transparency, Stroke stroke, Color color) {
		super(uuid, cuuid);
		
		this.transparency = transparency;
		this.stroke = stroke;
		this.color = color;
	}
	
	public void applyElement() 
	{
		
	}
	
	public void removeElement()
	{
		
	}
	
	public Composable getComposable()
	{
		return CConnectorController.connectors.get(cuuid);
	}
	
	public boolean isDrawable()
	{
		if (!CConnectorController.exists(cuuid))
			return false;
		
		return CConnectorController.connectors.get(cuuid).isHighlighted();
	}
	
	public PNode getNode()
	{
		if (!CConnectorController.exists(cuuid))
			return null;
		
		CConnector connector = CConnectorController.connectors.get(cuuid);
		
		PPath path = new PPath();
		path.setTransparency(transparency);
		path.setStroke(stroke);
		path.setStrokePaint(color);
		path.setPathTo(connector.getPathReference());
		
		return path;
	}
	
	public CalicoPacket getPacket(long uuid, long cuuid)
	{
		BasicStroke s = (BasicStroke)stroke;
		int sDashLength = (s.getDashArray() == null) ? 0 : s.getDashArray().length;
		
		int packetSize = ByteUtils.SIZE_OF_INT 						//Command
				+ ByteUtils.SIZE_OF_INT								//Element Type
				+ (2 * ByteUtils.SIZE_OF_LONG)						//UUID & CUUID
				+ ByteUtils.SIZE_OF_INT								//Transparency
				+ (6 * ByteUtils.SIZE_OF_INT)						//Line Width, End Cap, Line Join, MiterLimit, Dash Array Length, Dash Phase
				+ (sDashLength * ByteUtils.SIZE_OF_INT) //Dash Array
				+ ByteUtils.SIZE_OF_INT;							//Color
		
		CalicoPacket packet = new CalicoPacket(packetSize);
		
		packet.putInt(NetworkCommand.ELEMENT_ADD);
		packet.putInt(ComposableElement.TYPE_HIGHLIGHT);
		packet.putLong(uuid);
		packet.putLong(cuuid);
		packet.putFloat(transparency);
		
		packet.putFloat(s.getLineWidth());
		packet.putInt(s.getEndCap());
		packet.putInt(s.getLineJoin());
		packet.putFloat(s.getMiterLimit());
		packet.putInt(sDashLength);
		for (int i = 0; i < sDashLength; i++)
		{
			packet.putFloat(s.getDashArray()[i]);
		}
		packet.putFloat(s.getDashPhase());
		
		packet.putColor(color);
		
		return packet;
	}
	
	public CalicoPacket getPacket()
	{
		return getPacket(this.uuid, this.cuuid);
	}

}
