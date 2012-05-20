package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

public class EnterCanvasButton extends PieMenuButton
{
	public EnterCanvasButton()
	{
		super(CalicoIconManager.getIconImage("intention.enter-canvas"));
	}

	@Override
	public void onClick(InputEventInfo event)
	{
		CCanvasController
				.loadCanvas(CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId());
	}
}
