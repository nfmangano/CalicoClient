package calico.plugins.iip.components.menus;

import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import calico.components.bubblemenu.BubbleMenu;
import calico.components.menus.CanvasMenuButton;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * Simple button to show the tag panel. This feature is obsolete.
 * 
 * @author Byron Hawkins
 */
public class ZoomToCenterRingMenuButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;



	/**
	 * Instantiated via reflection in CanvasStatusBar
	 */
	public ZoomToCenterRingMenuButton()
	{


		try
		{
			setImage(CalicoIconManager.getIconImage("intention-graph.zoom-ring-menu"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

//		CanvasPerspectiveController.getInstance().canvasIntentionToolBarCreated(toolbar);
	}

	public void actionMouseClicked()
	{
		super.actionMouseClicked();
		
		SwingUtilities.invokeLater(
				new Runnable() { public void run() { 
					IntentionGraph.getInstance().zoomToInnerRing();
				}});
		
		if (BubbleMenu.isBubbleMenuActive())
			BubbleMenu.clearMenu();
	}
}
