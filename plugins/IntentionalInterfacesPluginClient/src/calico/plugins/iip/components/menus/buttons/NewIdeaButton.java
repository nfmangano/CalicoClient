package calico.plugins.iip.components.menus.buttons;

import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.canvas.CanvasIntentionToolBarItem;
import calico.plugins.iip.controllers.CCanvasLinkController;

public class NewIdeaButton extends CanvasIntentionToolBarItem
{
	public NewIdeaButton()
	{
		super("intention.new-idea");
	}

	@Override
	protected void onClick()
	{
		CCanvasLinkController.getInstance().createLinkToEmptyCanvas(canvas_uuid, CCanvasLink.LinkType.NEW_IDEA);
	}
}
