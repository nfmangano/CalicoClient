package calico.plugins.iip.components.piemenu.canvas;

import calico.components.piemenu.PieMenuButton;
import calico.plugins.iip.components.CCanvasLink;

public class CreateDesignInsideLinkButton  extends PieMenuButton
{
	public CreateDesignInsideLinkButton()
	{
		super(CCanvasLink.LinkType.DESIGN_INSIDE.image);
	}
	
	@Override
	public void onClick()
	{
		System.out.println("Design inside the active scrap");
	}
}

