package calico.plugins.iip.components.menus.buttons;

import calico.plugins.iip.components.canvas.CanvasIntentionToolBarItem;
import calico.plugins.iip.controllers.IntentionCanvasController;

public class ToggleLinkBaysButton extends CanvasIntentionToolBarItem
{
	public ToggleLinkBaysButton()
	{
		super("intention.toggle-link-bays");
	}
	
	@Override
	protected void onClick()
	{
		IntentionCanvasController.getInstance().toggleLinkVisibility();
	}
}
