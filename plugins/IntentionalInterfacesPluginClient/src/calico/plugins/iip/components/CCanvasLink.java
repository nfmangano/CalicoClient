package calico.plugins.iip.components;

import java.awt.Image;

import calico.iconsets.CalicoIconManager;

public class CCanvasLink
{
	public enum LinkType
	{
		NEW_IDEA("intention.new-idea"),
		NEW_PERSPECTIVE("intention.new-perspective"),
		NEW_ALTERNATIVE("intention.new-alternative"),
		DESIGN_INSIDE("intention.design-inside");
		
		public final Image image;
		
		private LinkType(String imageId)
		{
			image = CalicoIconManager.getIconImage(imageId);
		}
	}

	private long uuid;

	private LinkType linkType;

	private CCanvasLinkAnchor anchorA;
	private CCanvasLinkAnchor anchorB;

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
}
