package calico.components;

import java.awt.*;

/**
 * Used to denote the anchor points in {@link CArrow}
 * @author mdempsey
 *
 */
public class AnchorPoint implements Cloneable
{
	private int type = CArrow.TYPE_CANVAS;
	private Point point = new Point(0,0);
	private long uuid = 0L;
	
	public AnchorPoint(Point point, long uuid)
	{
		this(CArrow.TYPE_CANVAS, point, uuid);
	}
	public AnchorPoint(int type, Point point, long uuid)
	{
		this.type = type;
		this.point = point;
		this.uuid = uuid;
	}
	public AnchorPoint(int type, long uuid, Point point)
	{
		this(type, point, uuid);
	}
	public int getType()
	{
		return this.type;
	}
	public Point getPoint()
	{
		return this.point;
	}
	public long getUUID()
	{
		return this.uuid;
	}
	public void translate(int x, int y)
	{
		this.point.translate(x, y);
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
		return new AnchorPoint(type, new Point(point.x, point.y), uuid);
	}
}