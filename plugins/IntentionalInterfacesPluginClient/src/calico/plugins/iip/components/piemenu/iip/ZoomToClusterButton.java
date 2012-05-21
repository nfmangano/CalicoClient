package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

public class ZoomToClusterButton extends PieMenuButton
{
	public ZoomToClusterButton()
	{
		super(CalicoIconManager.getIconImage("intention.zoom-to-cluster"));
	}

	@Override
	public void onClick(InputEventInfo event)
	{
		long activeCanvasId = CIntentionCellController.getInstance().getCellById(CIntentionCellInputHandler.getInstance().getActiveCell()).getCanvasId();
		IntentionGraph.getInstance().zoomToCluster(activeCanvasId);
	}
}
