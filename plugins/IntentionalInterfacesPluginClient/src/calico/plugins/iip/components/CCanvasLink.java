package calico.plugins.iip.components;

import java.awt.Image;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class CCanvasLink
{
	public enum LinkDirection
	{
		INCOMING,
		OUTGOING;
	}

	private static final double HIT_PROXIMITY = 10.0;

	private long uuid;

	private CCanvasLinkAnchor anchorA;
	private CCanvasLinkAnchor anchorB;

	private String label;

	private final Line2D hitTestLink = new Line2D.Double();
	private final Point2D hitTestPoint = new Point2D.Double();

	public CCanvasLink(long uuid, CCanvasLinkAnchor anchorA, CCanvasLinkAnchor anchorB, String label)
	{
		this.uuid = uuid;
		this.anchorA = anchorA;
		this.anchorB = anchorB;

		setLabel(label);

		anchorA.setLink(this);
		anchorB.setLink(this);
	}

	public long getId()
	{
		return uuid;
	}

	public CCanvasLinkAnchor getAnchorA()
	{
		return anchorA;
	}

	public CCanvasLinkAnchor getAnchorB()
	{
		return anchorB;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String userLabel)
	{
		this.label = userLabel;
	}

	public boolean contains(Point2D point)
	{
		hitTestPoint.setLocation(point);
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).globalToLocal(hitTestPoint);
		hitTestLink.setLine(anchorA.getPoint(), anchorB.getPoint());
		double proximity = hitTestLink.ptSegDist(hitTestPoint);
		return proximity < HIT_PROXIMITY;
	}
}
