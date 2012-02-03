package calico.plugins.iip.components;

import calico.Calico;
import calico.components.CGroupImage;
import calico.iconsets.CalicoIconManager;

public class CCanvasLinkBadge extends CGroupImage
{
	public enum Type
	{
		INCOMING,
		OUTGOING;
	}

	public static final double BADGE_WIDTH = 30.0;
	public static final double BADGE_HEIGHT = 30.0;
	
	private final CCanvasLinkAnchor link;
	private final Type type;

	public CCanvasLinkBadge(CCanvasLinkAnchor link)
	{
		super(Calico.uuid(), link.getCanvasId(), link.getLink().getLinkType().image);
		
		this.link = link;
		if (link.getCanvasId() == link.getLink().getAnchorA().getCanvasId())
		{
			type = Type.OUTGOING;
		}
		else
		{
			type = Type.INCOMING;
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
	
	public Type getType()
	{
		return type;
	}
}
