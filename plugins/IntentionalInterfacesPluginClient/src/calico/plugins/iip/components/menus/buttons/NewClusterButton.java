package calico.plugins.iip.components.menus.buttons;

import calico.components.menus.CanvasMenuButton;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.CIntentionCellFactory;
import calico.plugins.iip.iconsets.CalicoIconManager;

/**
 * Simple button to create a new canvas in its own cluster. The layout will automatically put a canvas with no incoming
 * arrows into its own cluster, so the button simply requests a new canvas.
 * 
 * @author Byron Hawkins
 */
public class NewClusterButton extends CanvasMenuButton
{
	public NewClusterButton()
	{
		setImage(CalicoIconManager.getIconImage("intention-graph.new-cluster"));
	}

	@Override
	public void actionMouseClicked()
	{
		CIntentionCellFactory.getInstance().createNewCell();
	}
}
