package calico.plugins.iip.components;

import java.awt.Point;

import calico.components.arrow.AbstractArrowAnchorPoint;
import calico.plugins.iip.controllers.CIntentionCellController;

public class CCanvasLinkAnchor extends AbstractArrowAnchorPoint
{
	public enum ArrowEndpointType
	{
		FLOATING,
		INTENTION_CELL;
	}
	
	public class Badge
	{
		private long groupId;
		private Point position;
		
		public long getGroupId()
		{
			return groupId;
		}
		
		public void setGroupId(long groupId)
		{
			this.groupId = groupId;
		}
		
		public Point getPosition()
		{
			return position;
		}
		
		public void setPosition(Point position)
		{
			this.position = position;
		}
	}

	private final long uuid;
	private long canvas_uuid;
	private Badge badge = null;
	private ArrowEndpointType type;

	private CCanvasLink link;

	private CCanvasLinkAnchor(long uuid, long canvas_uuid, ArrowEndpointType type)
	{
		super();

		this.uuid = uuid;
		this.canvas_uuid = canvas_uuid;
		this.type = type;
	}

	public CCanvasLinkAnchor(long uuid, long canvas_uuid, int x, int y)
	{
		this(uuid, canvas_uuid, ArrowEndpointType.INTENTION_CELL);

		this.point.setLocation(x, y);
	}

	public CCanvasLinkAnchor(long uuid, int x, int y)
	{
		this(uuid, 0L, ArrowEndpointType.FLOATING);

		this.point.setLocation(x, y);
	}

	public long getId()
	{
		return uuid;
	}

	public CCanvasLinkAnchor getOpposite()
	{
		if (link.getAnchorA() == this)
		{
			return link.getAnchorB();
		}
		else
		{
			return link.getAnchorA();
		}
	}

	public long getCanvasId()
	{
		return canvas_uuid;
	}
	
	public boolean hasBadge()
	{
		return badge != null;
	}

	public Badge getBadge()
	{
		if (badge == null)
		{
			badge = new Badge();
		}
		return badge;
	}

	public ArrowEndpointType getArrowEndpointType()
	{
		return type;
	}

	public Point getPoint()
	{
		return point;
	}

	public CCanvasLink getLink()
	{
		return link;
	}

	void setLink(CCanvasLink link)
	{
		this.link = link;
	}

	public void move(long canvas_uuid, int x, int y)
	{
		this.canvas_uuid = canvas_uuid;
		point.x = x;
		point.y = y;
	}
}
