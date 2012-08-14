package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

/**
 * Bubble menu button to copy a canvas. This features is obsolete.
 *
 * @author Byron Hawkins
 */
public class CreateCanvasCopyButton extends PieMenuButton
{
	public CreateCanvasCopyButton()
	{
		super(CalicoIconManager.getIconImage("intention.copy-canvas"));
	}

	@Override
	public void onReleased(InputEventInfo event)
	{
		super.onReleased(event);
		
		long activeCanvasId = CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId();
		long newCanvas = CCanvasLinkController.getInstance().createLinkToEmptyCanvas(activeCanvasId);
		IntentionCanvasController.getInstance().copyCanvas(activeCanvasId, newCanvas);
	}
}
