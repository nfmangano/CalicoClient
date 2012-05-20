package calico.plugins.iip.components.piemenu.iip;

import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

public class DeleteCanvasButton extends PieMenuButton
{
	public DeleteCanvasButton()
	{
		super(CalicoIconManager.getIconImage("intention.delete-canvas"));
	}

	@Override
	public void onClick(InputEventInfo event)
	{
		CIntentionCellController.getInstance().deleteCanvas(
				CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId());
		
		BubbleMenu.clearMenu();
	}
}
