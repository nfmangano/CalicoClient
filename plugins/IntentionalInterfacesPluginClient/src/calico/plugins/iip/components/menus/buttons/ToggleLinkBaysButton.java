package calico.plugins.iip.components.menus.buttons;

import calico.plugins.iip.components.canvas.CanvasIntentionToolBarItem;
import calico.plugins.iip.controllers.IntentionCanvasController;

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
		IntentionCanvasController.getInstance().getIncomingLinkBay().setVisible(baysVisible);
		IntentionCanvasController.getInstance().getOutgoingLinkBay().setVisible(baysVisible);
	}
}
