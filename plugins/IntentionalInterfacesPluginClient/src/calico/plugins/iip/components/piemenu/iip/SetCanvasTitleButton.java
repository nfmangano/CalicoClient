package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.canvas.CanvasTitleDialog;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

/**
 * Bubble menu button to enter a canvas. This features is obsolete, because a canvas is now entered by tap.
 * 
 * @author Byron Hawkins
 */
public class SetCanvasTitleButton extends PieMenuButton
{
	public SetCanvasTitleButton()
	{
		super(CalicoIconManager.getIconImage("intention-graph.set-canvas-title"));
	}

	@Override
	public void onReleased(InputEventInfo event)
	{
		super.onReleased(event);

		long canvasId = CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId();
		
		CanvasTitleDialog.Action action = CanvasTitleDialog.getInstance().queryUserForLabel(
				CIntentionCellController.getInstance().getCellByCanvasId(canvasId));
		
		CIntentionCellController.getInstance().setCellTitle(CIntentionCellInputHandler.getInstance().getActiveCell(),
				CanvasTitleDialog.getInstance().getText(), false);
		
		calico.components.bubblemenu.BubbleMenu.clearMenu();
		
//		CCanvasController
//				.loadCanvas(CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId());
	}
}
