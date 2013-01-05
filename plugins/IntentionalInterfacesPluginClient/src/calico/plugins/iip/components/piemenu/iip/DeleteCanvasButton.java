package calico.plugins.iip.components.piemenu.iip;

import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionGraphController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

/**
 * Bubble menu button to delete a canvas. The layout always rebuilds itself on the basis of canvases and arrows, so the
 * only action taken by this button is to delete the canvas.
 * 
 * @author Byron Hawkins
 */
public class DeleteCanvasButton extends PieMenuButton
{
	public DeleteCanvasButton()
	{
		super(CalicoIconManager.getIconImage("intention.delete-canvas"));
	}

	@Override
	public void onReleased(InputEventInfo event)
	{
		super.onReleased(event);
		
		long activeCanvasId = CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId();
		IntentionGraph.getInstance().deleteCanvasAndRemoveExtraClusters(activeCanvasId);
		
		BubbleMenu.clearMenu();
	}




}
