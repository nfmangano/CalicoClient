package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

public class CreateNewCanvasLinkButton extends PieMenuButton
{
	public CreateNewCanvasLinkButton()
	{
		super(CalicoIconManager.getIconImage("intention.new-canvas"));
	}

	@Override
	public void onClick(InputEventInfo event)
	{
		CreateIntentionArrowPhase.INSTANCE.startCreate(
				CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()), event.getGlobalPoint(),
				CreateIntentionArrowPhase.NewLinkMode.LINK_TO_BLANK);
	}
}
