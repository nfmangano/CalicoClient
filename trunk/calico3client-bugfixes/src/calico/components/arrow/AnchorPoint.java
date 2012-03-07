package calico.components.arrow;

import java.awt.Point;


/**
 * Used to denote the anchor points in {@link CArrow}
 * 
 * @author mdempsey
 * 
 */
public class AnchorPoint extends AbstractArrowAnchorPoint implements Cloneable
{
	private int type = CArrow.TYPE_CANVAS;
	private long uuid = 0L;

	public AnchorPoint(int type, Point point, long uuid)
	{
		super(point);

		this.type = type;
		this.uuid = uuid;
	}

	public AnchorPoint(Point point, long uuid)
	{
		this(CArrow.TYPE_CANVAS, point, uuid);
	}

	public AnchorPoint(int type, long uuid, Point point)
	{
		this(type, point, uuid);
	}

	public int getType()
	{
		return this.type;
	}

	public long getUUID()
	{
		return this.uuid;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public void setUUID(long uuid)
	{
		this.uuid = uuid;
	}

	// For cloning dolly
	public AnchorPoint clone()
	{
		return new AnchorPoint(type, new Point(getPoint().x, getPoint().y), uuid);
	}
}