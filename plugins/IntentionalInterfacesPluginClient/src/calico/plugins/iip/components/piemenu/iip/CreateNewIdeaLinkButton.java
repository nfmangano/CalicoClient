package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class CreateNewIdeaLinkButton extends PieMenuButton
{
	public CreateNewIdeaLinkButton()
	{
		super(CalicoIconManager.getIconImage("intention.new-idea"));
	}
	
	@Override
	public void onClick()
	{
		System.out.println("Start creating a new idea arrow from here");
	}
}
