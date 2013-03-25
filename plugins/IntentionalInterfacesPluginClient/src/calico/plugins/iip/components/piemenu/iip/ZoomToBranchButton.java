package calico.plugins.iip.components.piemenu.iip;

import java.util.List;

import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
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
public class ZoomToBranchButton extends PieMenuButton
{
	public ZoomToBranchButton()
	{
		super(CalicoIconManager.getIconImage("intention-graph.zoom-branch"));
	}

	@Override
	public void onReleased(InputEventInfo event)
	{
		super.onReleased(event);
		
		IntentionGraph.getInstance().zoomToSingleChildBranch();
		
		BubbleMenu.clearMenu();
	}




}
