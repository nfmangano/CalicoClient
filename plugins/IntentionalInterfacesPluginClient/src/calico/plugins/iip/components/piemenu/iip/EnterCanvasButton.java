package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

/**
 * Bubble menu button to enter a canvas. This features is obsolete, because a canvas is now entered by tap.
 * 
 * @author Byron Hawkins
 */
public class EnterCanvasButton extends PieMenuButton
{
	public EnterCanvasButton()
	{
		super(CalicoIconManager.getIconImage("intention.enter-canvas"));
	}

	@Override
	public void onReleased(InputEventInfo event)
	{
		super.onReleased(event);

		CCanvasController
				.loadCanvas(CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId());
	}
}
