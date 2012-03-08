package calico.plugins.iip.components.menus.buttons;

import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.canvas.CanvasIntentionToolBarItem;
import calico.plugins.iip.controllers.CCanvasLinkController;

public class NewPerspectiveButton extends CanvasIntentionToolBarItem
{
	public NewPerspectiveButton()
	{
		super("intention.new-perspective");
	}

	@Override
	protected void onClick()
	{
		CCanvasLinkController.getInstance().createLinkToEmptyCanvas(canvas_uuid, CCanvasLink.LinkType.NEW_PERSPECTIVE);
	}
}
