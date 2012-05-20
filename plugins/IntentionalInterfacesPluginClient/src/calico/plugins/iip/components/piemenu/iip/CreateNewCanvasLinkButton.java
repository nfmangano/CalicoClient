package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.controllers.CCanvasLinkController;
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
		long activeCanvasId = CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId();
		CCanvasLinkController.getInstance().createLinkToEmptyCanvas(activeCanvasId);
	}
}
