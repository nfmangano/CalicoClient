package calico.plugins.iip.components;

import java.awt.Color;

import calico.components.arrow.AbstractArrow;

public class CCanvasLinkArrow extends AbstractArrow<CCanvasLinkAnchor>
{
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
}
