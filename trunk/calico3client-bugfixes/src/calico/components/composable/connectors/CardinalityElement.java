package calico.components.composable.connectors;

import java.awt.Font;
import java.awt.Point;
import java.awt.Polygon;

import calico.components.CConnector;
import calico.components.composable.Composable;
import calico.components.composable.ComposableElement;
import calico.controllers.CConnectorController;
import calico.networking.netstuff.ByteUtils;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

public class CardinalityElement extends ComposableElement {

	private int type;
	private String text;
	private Font font;
	
	public CardinalityElement(long uuid, long cuuid, int type, String text, Font font) {
		super(uuid, cuuid);
		this.type = type;
		this.text = text;
		this.font = font;
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
		return true;
	}
	
	public PNode getNode()
	{
		if (!CConnectorController.exists(cuuid))
			return null;
		
		CConnector connector = CConnectorController.connectors.get(cuuid);
		Polygon linePoints = connector.getRawPolygon();
		
		PText textNode = new PText(this.text);
		textNode.setFont(this.font);
		
		Point p;
		
		if (this.type == CConnector.TYPE_HEAD)
		{
			p = new Point(linePoints.xpoints[linePoints.npoints-1], linePoints.ypoints[linePoints.npoints-1]);
		}
		else if (this.type == CConnector.TYPE_TAIL)
		{
			p = new Point(linePoints.xpoints[0], linePoints.ypoints[0]);
		}
		else
		{
			return null;
		}
		
		textNode.setX(p.x + 15);
		textNode.setY(p.y);
		
		return textNode;
	}
	
	public CalicoPacket getPacket(long uuid, long cuuid)
	{
		int packetSize = ByteUtils.SIZE_OF_INT 				//Command
					+ ByteUtils.SIZE_OF_INT 				//Element Type
					+ (2 * ByteUtils.SIZE_OF_LONG) 			//UUID & CUUID
					+ ByteUtils.SIZE_OF_INT					//Anchor Type
					+ CalicoPacket.getSizeOfString(text)	//Cardinality Text
					+ CalicoPacket.getSizeOfString(font.getName()) //Font name
					+ (2 * ByteUtils.SIZE_OF_INT);			//Font style and size
		
		CalicoPacket packet = new CalicoPacket(packetSize);
		
		packet.putInt(NetworkCommand.ELEMENT_ADD);
		packet.putInt(ComposableElement.TYPE_CARDINALITY);
		packet.putLong(uuid);
		packet.putLong(cuuid);
		packet.putInt(type);
		packet.putString(text);
		packet.putString(font.getName());
		packet.putInt(font.getStyle());
		packet.putInt(font.getSize());
		
		return packet;
	}
	
	public CalicoPacket getPacket()
	{
		return getPacket(this.uuid, this.cuuid);
	}
}
