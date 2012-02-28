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
		NEW_PERSPECTIVE("intention.new-perspective"),
		NEW_ALTERNATIVE("intention.new-alternative"),
		DESIGN_INSIDE("intention.design-inside");
		
		public final Image image;
		
		private LinkType(String imageId)
		{
			image = CalicoIconManager.getIconImage(imageId);
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
	
	private final Line2D hitTestLink = new Line2D.Double();
	private final Point2D hitTestPoint = new Point2D.Double();

	public CCanvasLink(long uuid, LinkType linkType, CCanvasLinkAnchor anchorA, CCanvasLinkAnchor anchorB)
	{
		this.uuid = uuid;
		this.linkType = linkType;
		this.anchorA = anchorA;
		this.anchorB = anchorB;
		
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
	
	public boolean contains(Point2D point)
	{
		hitTestPoint.setLocation(point);
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).globalToLocal(hitTestPoint);
		hitTestLink.setLine(anchorA.getPoint(), anchorB.getPoint());
		double proximity = hitTestLink.ptSegDist(hitTestPoint);
		return proximity < HIT_PROXIMITY;
	}
}
