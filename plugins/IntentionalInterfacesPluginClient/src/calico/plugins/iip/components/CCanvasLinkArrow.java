package calico.plugins.iip.components;

import java.awt.Color;
import java.awt.geom.Point2D;

import calico.Geometry;
import calico.components.arrow.AbstractArrow;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

public class CCanvasLinkArrow extends AbstractArrow<CCanvasLinkAnchor>
{
	private static final Color NORMAL_COLOR = Color.black;
	private static final Color HIGHLIGHTED_COLOR = new Color(0xFFFF30);

	private final CCanvasLink link;

	public CCanvasLinkArrow(CCanvasLink link)
	{
		super(Color.black, TYPE_NORM_HEAD_B);

		this.link = link;

		setAnchorA(link.getAnchorA());
		setAnchorB(link.getAnchorB());
	}

	public long getId()
	{
		return link.getId();
	}

	public void setHighlighted(boolean b)
	{
		if (b)
		{
			setColor(HIGHLIGHTED_COLOR);
		}
		else
		{
			setColor(NORMAL_COLOR);
		}

		redraw();
	}

	@Override
	protected void addRenderingElements()
	{
		super.addRenderingElements();

		PText label = new PText(link.getLabel());
		PBounds bounds = label.computeFullBounds(null);
		double[] center = Geometry.computePointOnLine(link.getAnchorA().getPoint().getX(), link.getAnchorA().getPoint().getY(), link.getAnchorB().getPoint()
				.getX(), link.getAnchorB().getPoint().getY(), 0.5);
		label.setBounds(center[0], center[1], bounds.getWidth(), bounds.getHeight());
		label.setTextPaint(getColor());
		addChild(0, label);
	}
}
