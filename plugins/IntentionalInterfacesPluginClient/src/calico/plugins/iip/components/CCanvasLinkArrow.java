package calico.plugins.iip.components;

public class CCanvasLinkArrow
{
	private final CCanvasLink link;

	public CCanvasLinkArrow(CCanvasLink link)
	{
		this.link = link;
	}
	
	public long getId()
	{
		return link.getId();
	}
}
