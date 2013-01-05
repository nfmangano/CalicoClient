package calico.plugins.iip.components.menus.buttons;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.components.canvas.CanvasInputProximity;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.CIntentionCellFactory;
import calico.plugins.iip.iconsets.CalicoIconManager;

/**
 * Simple button to create a new canvas in its own cluster. The layout will automatically put a canvas with no incoming
 * arrows into its own cluster, so the button simply requests a new canvas.
 * 
 * @author Byron Hawkins
 */
public class NewClusterCanvasButton extends CanvasMenuButton
{
	public NewClusterCanvasButton()
	{
		setImage(CalicoIconManager.getIconImage("intention.new-canvas"));
		
		if (IntentionGraph.getInstance().getFocus() == IntentionGraph.Focus.WALL)
			setTransparency(.7f);
	}

	@Override
	public void actionMouseClicked()
	{
		if (IntentionGraph.getInstance().getFocus() == IntentionGraph.Focus.WALL)
			return;
		
		long clusterRoot = IntentionGraph.getInstance().getClusterInFocus();

		long newCanvasId = CIntentionCellFactory.getInstance()
				.createNewCell(CCanvasController.getCurrentUUID(), CanvasInputProximity.forPosition(getBounds().getX())).getCanvasId();
		
		CCanvasLinkController.getInstance().createLink(clusterRoot /*CIntentionCellController.getInstance().getClusterRootCanvasId(currentCell)*/, newCanvasId);
		
		
		IntentionGraph.getInstance().createClusterIfNoEmptyClusterExists(clusterRoot);
	}


}
