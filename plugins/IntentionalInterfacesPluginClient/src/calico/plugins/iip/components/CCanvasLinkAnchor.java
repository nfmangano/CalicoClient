package calico.plugins.iip.components;

import java.awt.Point;

import calico.components.arrow.AbstractArrowAnchorPoint;
import calico.plugins.iip.controllers.CIntentionCellController;

public class CCanvasLinkAnchor extends AbstractArrowAnchorPoint
{
	public enum Type
	{
		FLOATING,
		INTENTION_CELL;
	}

	private final long uuid;
	private long canvas_uuid;
	private long group_uuid;
	private Type type;

	private CCanvasLink link;

	private CCanvasLinkAnchor(long uuid, long canvas_uuid, Type type)
	{
		super();

		this.uuid = uuid;
		this.canvas_uuid = canvas_uuid;
		this.group_uuid = 0L;
		this.type = type;
	}

	public CCanvasLinkAnchor(long uuid, long canvas_uuid)
	{
		this(uuid, canvas_uuid, Type.INTENTION_CELL);
	}

	public CCanvasLinkAnchor(long uuid, long canvas_uuid, long group_uuid)
	{
		this(uuid, canvas_uuid);

		this.group_uuid = group_uuid;
	}

	public CCanvasLinkAnchor(long uuid, int x, int y)
	{
		this(uuid, 0L, Type.FLOATING);

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

	public long getGroupId()
	{
		return group_uuid;
	}

	public Type getType()
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

	public void move(long canvas_uuid, long group_uuid, int x, int y)
	{
		this.canvas_uuid = canvas_uuid;
		this.group_uuid = group_uuid;
		point.x = x;
		point.y = y;
	}
}
