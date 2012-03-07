package calico.plugins.iip.components;

import java.awt.Image;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class CCanvasLink
{
	public enum LinkType
	{
		NEW_PERSPECTIVE("intention.new-perspective", "(P)"),
		NEW_ALTERNATIVE("intention.new-alternative", "(A)"),
		DESIGN_INSIDE("intention.design-inside", "(D)");

		public final Image image;
		public final String labelPrefix;

		private LinkType(String imageId, String labelPrefix)
		{
			image = CalicoIconManager.getIconImage(imageId);
			this.labelPrefix = labelPrefix;
		}
	}

	public enum LinkDirection
	{
		INCOMING,
		OUTGOING;
	}

	private static final double HIT_PROXIMITY = 10.0;

	private long uuid;

	private LinkType linkType;

	private CCanvasLinkAnchor anchorA;
	private CCanvasLinkAnchor anchorB;

	/**
	 * There are two versions of the label. The <code>userLabel</code> is stored in the server, and consists of the
	 * label text entered by the user. The <code>label</code> is derived from the <code>linkType</code> and
	 * <code>userLabel</code>, and is never stored anywhere outside this class.
	 */
	private String userLabel;
	private String label;

	private final Line2D hitTestLink = new Line2D.Double();
	private final Point2D hitTestPoint = new Point2D.Double();

	public CCanvasLink(long uuid, LinkType linkType, CCanvasLinkAnchor anchorA, CCanvasLinkAnchor anchorB, String label)
	{
		this.uuid = uuid;
		this.linkType = linkType;
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

	public LinkType getLinkType()
	{
		return linkType;
	}

	public void setLinkType(LinkType linkType)
	{
		this.linkType = linkType;
	}

	public String getLabel()
	{
		return label;
	}

	public String getUserLabel()
	{
		return userLabel;
	}

	public void setLabel(String userLabel)
	{
		this.userLabel = userLabel;
		this.label = linkType.labelPrefix + " " + userLabel;
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
