package calico.plugins.iip.components;

import calico.Calico;
import calico.components.CGroup;

public class CCanvasLinkBadge extends CGroup
{
	private final CCanvasLinkAnchor link;

	public CCanvasLinkBadge(CCanvasLinkAnchor link)
	{
		super(Calico.uuid(), link.getCanvasId());
		
		this.link = link;
	}
	
	public long getId()
	{
		return getUUID();
	}
}
