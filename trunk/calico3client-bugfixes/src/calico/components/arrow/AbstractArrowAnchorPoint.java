package calico.components.arrow;

import java.awt.Point;

public abstract class AbstractArrowAnchorPoint
{
	protected final Point point;

	protected AbstractArrowAnchorPoint()
	{
		this(new Point());
	}
	
	protected AbstractArrowAnchorPoint(Point point)
	{
		this.point = point;
	}

	public Point getPoint()
	{
		return this.point;
	}

	public void translate(int x, int y)
	{
		this.point.translate(x, y);
	}
}
