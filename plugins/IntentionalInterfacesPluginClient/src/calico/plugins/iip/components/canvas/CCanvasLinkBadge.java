package calico.plugins.iip.components.canvas;

import calico.Calico;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.util.IntentionalInterfacesGraphics;
import edu.umd.cs.piccolo.nodes.PImage;

public class CCanvasLinkBadge
{
	public static final double BADGE_WIDTH = 30.0;
	public static final double BADGE_HEIGHT = 30.0;

	private final long uuid;

	private final CCanvasLinkAnchor anchor;
	private final CCanvasLink.LinkDirection direction;

	private PImage image;

	public CCanvasLinkBadge(CCanvasLinkAnchor anchor)
	{
		uuid = Calico.uuid();

		this.anchor = anchor;

		updateImage();

		if (anchor.getCanvasId() == anchor.getLink().getAnchorA().getCanvasId())
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

	public CCanvasLinkAnchor getLinkAnchor()
	{
		return anchor;
	}

	public CCanvasLink.LinkDirection getDirection()
	{
		return direction;
	}

	public PImage getImage()
	{
		return image;
	}

	public void updateImage()
	{
		image = new PImage(IntentionalInterfacesGraphics.superimposeCellAddress(anchor.getLink().getLinkType().image, anchor.getOpposite().getCanvasId()));
	}
}
