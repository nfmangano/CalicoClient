package calico.components.arrow;

import java.awt.Point;

public abstract class AbstractArrowAnchorPoint
{
	private Point point = new Point(0,0);

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
