package calico.plugins.iip.components.graph;

import calico.CalicoDataStore;
import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.plugins.iip.controllers.CanvasPerspectiveController;
import calico.plugins.iip.controllers.IntentionPerspectiveController;

public class ShowIntentionGraphButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiated via reflection in CanvasStatusBar
	 */
	public ShowIntentionGraphButton(long canvas_uuid)
	{
		try
		{
			// TODO: it may not work to use the client's icon manager
			setImage(CalicoIconManager.getIconImage("intention.to-intention-graph"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void actionMouseClicked()
	{
		CalicoDataStore.calicoObj.getContentPane().removeAll();
		CalicoDataStore.calicoObj.getContentPane().add(IntentionGraph.getInstance().getComponent());
		CalicoDataStore.calicoObj.pack();
		CalicoDataStore.calicoObj.setVisible(true);
		CalicoDataStore.calicoObj.repaint();
		CalicoDataStore.isViewingGrid = false;
	}
}
