package calico.plugins.iip.components.canvas;

import calico.Calico;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.util.IntentionalInterfacesGraphics;
import edu.umd.cs.piccolo.nodes.PImage;

public class CCanvasLinkToken extends PImage
{
	public static final double TOKEN_WIDTH = 30.0;
	public static final double TOKEN_HEIGHT = 30.0;

	private final long uuid;

	private final CCanvasLinkAnchor link;
	private final CCanvasLink.LinkDirection direction;

	public CCanvasLinkToken(CCanvasLinkAnchor link)
	{
		super(IntentionalInterfacesGraphics.superimposeCellAddress(link.getLink().getLinkType().image, link.getOpposite().getCanvasId()));

		uuid = Calico.uuid();

		this.link = link;
		if (link.getCanvasId() == link.getLink().getAnchorA().getCanvasId())
		{
			direction = CCanvasLink.LinkDirection.OUTGOING;
		}
		else
		{
			direction = CCanvasLink.LinkDirection.INCOMING;
		}
	}

	public long getId()
	{
		return uuid;
	}

	public CCanvasLinkAnchor getLink()
	{
		return link;
	}

	public CCanvasLink.LinkDirection getDirection()
	{
		return direction;
	}
}
