package calico.plugins.iip.components;

import java.awt.Point;

import calico.components.arrow.AbstractArrowAnchorPoint;
import calico.plugins.iip.controllers.CIntentionCellController;

/**
 * Represents an endpoint of a <code>CCanvasLink</code> in this plugin's internal model of the intention graph. This
 * class has no visual counterpart, it is for modeling purposes only.
 * 
 * @author Byron Hawkins
 */
public class CCanvasLinkAnchor extends AbstractArrowAnchorPoint
{
	/**
	 * Simple enum specifying whether an endpoint is attached to any <code>CIntentionCell</code>.
	 * 
	 * @author Byron Hawkins
	 */
	public enum ArrowEndpointType
	{
		FLOATING,
		INTENTION_CELL;
	}

	private final long uuid;
	/**
	 * Identifies the canvas corresponding to the CIC at which this anchor is attached, or <code>-1L</code> if this
	 * anchor is not attached to any CIC.
	 */
	private long canvas_uuid;
	/**
	 * If this anchor is attached to a specific scrap, for the "design inside" feature, that scrap's id is specified
	 * here. Otherwise <code>group_uuid</code> will be <code>0L</code>.
	 */
	private long group_uuid;
	/**
	 * Specifies whether this arrow is floating or attached to a CIC.
	 */
	private ArrowEndpointType type;

	/**
	 * The link for which this endoint acts as an anchor.
	 */
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
		this(uuid, -1L, ArrowEndpointType.FLOATING);

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

	/**
	 * True when this anchor is attached to a specific scrap, for the "design inside" feature.
	 */
	public boolean hasGroup()
	{
		return group_uuid > 0L;
	}

	public long getGroupId()
	{
		return group_uuid;
	}

	/**
	 * Attach this anchor to the scrap <code>group_uuid</code>.
	 */
	public void setGroupId(long group_uuid)
	{
		this.group_uuid = group_uuid;
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

	/**
	 * Move this anchor to pixel coordinates <code>(x, y)</code> in the Intention View coordinate space (shared by
	 * CICs), and attach it to <code>canvas_uuid</code> (which may be <code>-1L</code> to indicate a floating anchor).
	 */
	public void move(long canvas_uuid, ArrowEndpointType type, int x, int y)
	{
		this.canvas_uuid = canvas_uuid;
		this.type = type;
		point.x = x;
		point.y = y;
	}
}
