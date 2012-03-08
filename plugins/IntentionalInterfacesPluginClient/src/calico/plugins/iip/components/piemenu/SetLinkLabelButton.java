package calico.plugins.iip.components.piemenu;

import calico.components.piemenu.PieMenuButton;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.LinkLabelDialog;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class SetLinkLabelButton extends PieMenuButton
{
	private CCanvasLink link = null;

	public SetLinkLabelButton()
	{
		super(CalicoIconManager.getIconImage("intention.set-link-label"));
	}

	public void setContext(CCanvasLink link)
	{
		this.link = link;
	}
	
	@Override
	public void onClick()
	{
		if (link == null)
		{
			System.out.println("Warning: set link label button displayed without having been prepared with a link!");
			return;
		}
		
		LinkLabelDialog.Action action = LinkLabelDialog.getInstance().queryUserForLabel(link);
		switch (action)
		{
			case OK:
				System.out.println("Set label for link to " + LinkLabelDialog.getInstance().getText());
				CCanvasLinkController.getInstance().setLinkLabel(link.getId(), LinkLabelDialog.getInstance().getText());
				break;
			case CANCEL:
				System.out.println("Cancel setting the link label.");
				break;
		}
	}
}
