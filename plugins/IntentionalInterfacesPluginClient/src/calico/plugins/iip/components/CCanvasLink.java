package calico.plugins.iip.components;

public class CCanvasLink
{
	public enum LinkType
	{
		NEW_IDEA,
		NEW_PERSPECTIVE,
		NEW_ALTERNATIVE,
		DESIGN_INSIDE;
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
	
	public void setLinkType(LinkType linkType)
	{
		this.linkType = linkType;
	}
}
