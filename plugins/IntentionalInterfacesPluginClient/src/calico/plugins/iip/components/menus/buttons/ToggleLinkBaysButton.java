package calico.plugins.iip.components.menus.buttons;

import calico.plugins.iip.components.canvas.CanvasIntentionToolBarItem;
import calico.plugins.iip.controllers.CanvasPerspectiveController;

public class ToggleLinkBaysButton extends CanvasIntentionToolBarItem
{
	private boolean baysVisible = false;

	public ToggleLinkBaysButton()
	{
		super("intention.toggle-link-bays");
	}
	
	@Override
	protected void reset()
	{
		baysVisible = false;
	}

	@Override
	protected void onClick()
	{
		baysVisible = !baysVisible;
		CanvasPerspectiveController.getInstance().getIncomingLinkBay().setVisible(baysVisible);
		CanvasPerspectiveController.getInstance().getOutgoingLinkBay().setVisible(baysVisible);
	}
}
