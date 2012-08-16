package calico.plugins.iip.components;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import calico.plugins.iip.components.graph.IntentionGraph;

/**
 * Represents a link in this plugin's internal model of the intention graph. The Piccolo component representative is the
 * <code>CCanvasLinkArrow</code>.
 * 
 * @author Byron Hawkins
 */
public class CCanvasLink
{
	/**
	 * Simple enum which specifies how a link relates to a particular <code>CIntentionCell</code>.
	 * 
	 * @author Byron Hawkins
	 */
	public enum LinkDirection
	{
		INCOMING,
		OUTGOING;
	}

	/**
	 * Specifies how far away from an arrow an input press event may occur and still be considered a press on the arrow.
	 */
	private static final double HIT_PROXIMITY = 10.0;

	private long uuid;

	/**
	 * The "from" end of the arrow.
	 */
	private CCanvasLinkAnchor anchorA;
	/**
	 * The "to" end of the arrow.
	 */
	private CCanvasLinkAnchor anchorB;

	/**
	 * The label text of the arrow, or null if there is no label.
	 */
	private String label;

	// these instances are used for calculations
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

	/**
	 * Discern whether an input event at <code>point</code> can be considered effective for this arrow.
	 */
	public boolean contains(Point2D point)
	{
		hitTestPoint.setLocation(point);
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).globalToLocal(hitTestPoint);
		hitTestLink.setLine(anchorA.getPoint(), anchorB.getPoint());
		double proximity = hitTestLink.ptSegDist(hitTestPoint);
		return proximity < HIT_PROXIMITY;
	}
}
