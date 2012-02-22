package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

public class CreateNewIdeaLinkButton extends PieMenuButton
{
	public CreateNewIdeaLinkButton()
	{
		super(CCanvasLink.LinkType.NEW_IDEA.image);
	}

	@Override
	public void onClick(InputEventInfo event)
	{
		System.out.println("Start creating a new idea arrow from here");

		CreateIntentionArrowPhase.INSTANCE.startCreate(
				CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()), event.getGlobalPoint(),
				CCanvasLink.LinkType.NEW_IDEA);
	}
}
