package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

public class CreateCanvasCopyButton extends PieMenuButton
{
	public CreateCanvasCopyButton()
	{
		super(CalicoIconManager.getIconImage("intention.copy-canvas"));
	}

	@Override
	public void onClick(InputEventInfo event)
	{
		long activeCanvasId = CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId();
		long newCanvas = CCanvasLinkController.getInstance().createLinkToEmptyCanvas(activeCanvasId, true);
		CCanvasLinkController.getInstance().copyCanvas(CCanvasController.getCurrentUUID(), newCanvas);

		// CreateIntentionArrowPhase.INSTANCE.startCreate(
		// CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()),
		// event.getGlobalPoint(),
		// CreateIntentionArrowPhase.NewLinkMode.LINK_TO_COPY);
	}
}
