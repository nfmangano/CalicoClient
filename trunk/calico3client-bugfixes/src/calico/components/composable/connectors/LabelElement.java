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

public class LabelElement extends ComposableElement{

	private String text;
	private Font font;
	
	public LabelElement(long uuid, long cuuid, String text, Font font) {
		super(uuid, cuuid);
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
		
		Point p = new Point(linePoints.xpoints[linePoints.npoints / 2], linePoints.ypoints[linePoints.npoints / 2]);
		
		textNode.setX(p.x - (textNode.getWidth() / 2));
		textNode.setY(p.y);


		
		return textNode;
	}
	
	public CalicoPacket getPacket(long uuid, long cuuid)
	{
		int packetSize = ByteUtils.SIZE_OF_INT 				//Command
				+ ByteUtils.SIZE_OF_INT 				//Element Type
				+ (2 * ByteUtils.SIZE_OF_LONG) 			//UUID & CUUID
				+ CalicoPacket.getSizeOfString(text)	//Label Text
				+ CalicoPacket.getSizeOfString(font.getName()) //Font name
				+ (2 * ByteUtils.SIZE_OF_INT);			//Font style and size
	
		CalicoPacket packet = new CalicoPacket(packetSize);
		
		packet.putInt(NetworkCommand.ELEMENT_ADD);
		packet.putInt(ComposableElement.TYPE_LABEL);
		packet.putLong(uuid);
		packet.putLong(cuuid);
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
