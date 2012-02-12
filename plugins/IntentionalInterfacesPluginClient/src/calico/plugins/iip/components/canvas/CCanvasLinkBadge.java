package calico.plugins.iip.components.canvas;

import calico.Calico;
import calico.components.CGroupImage;
import calico.iconsets.CalicoIconManager;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CCanvasLink.LinkDirection;

public class CCanvasLinkBadge extends CGroupImage
{
	public static final double BADGE_WIDTH = 30.0;
	public static final double BADGE_HEIGHT = 30.0;
	
	private final CCanvasLinkAnchor link;
	private final CCanvasLink.LinkDirection direction;

	public CCanvasLinkBadge(CCanvasLinkAnchor link)
	{ 
		super(Calico.uuid(), link.getCanvasId(), link.getLink().getLinkType().image);
		
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
		return getUUID();
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
