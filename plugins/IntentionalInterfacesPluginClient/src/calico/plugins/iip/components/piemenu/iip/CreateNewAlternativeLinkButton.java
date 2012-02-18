package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class CreateNewAlternativeLinkButton extends PieMenuButton
{
	public CreateNewAlternativeLinkButton()
	{
		super(CCanvasLink.LinkType.NEW_ALTERNATIVE.image);
	}
	
	@Override
	public void onClick()
	{
		System.out.println("Start creating a new alternative arrow from here");
	}
}
