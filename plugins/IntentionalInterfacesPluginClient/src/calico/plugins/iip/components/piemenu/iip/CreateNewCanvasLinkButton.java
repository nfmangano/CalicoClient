package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

/**
 * Bubble menu button to create a new canvas with a link to the selected canvas. This features is obsolete.
 * 
 * @author Byron Hawkins
 */
public class CreateNewCanvasLinkButton extends PieMenuButton
{
	public CreateNewCanvasLinkButton()
	{
		super(CalicoIconManager.getIconImage("intention.new-canvas"));
	}

	@Override
	public void onReleased(InputEventInfo event)
	{
		super.onReleased(event);

		long activeCanvasId = CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId();
		CCanvasLinkController.getInstance().createLinkToEmptyCanvas(activeCanvasId);
	}
}
