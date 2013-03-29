package calico.plugins.iip.components.menus;

import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import calico.components.menus.CanvasMenuButton;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;

/**
 * Simple button to show the tag panel. This feature is obsolete.
 * 
 * @author Byron Hawkins
 */
public class ZoomOutButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiated via reflection in CanvasStatusBar
	 */
	public ZoomOutButton()
	{

		try
		{
			setImage(CalicoIconManager.getIconImage("intention-graph.zoom-out"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

//		CanvasPerspectiveController.getInstance().canvasIntentionToolBarCreated(toolbar);
	}

	public void actionMouseClicked()
	{
		final Point2D centerOriginal = new Point2D.Double(IntentionGraph.getInstance().getGlobalCoordinatesForVisibleBounds().getCenterX(),
				IntentionGraph.getInstance().getGlobalCoordinatesForVisibleBounds().getCenterY());
		double scale = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
		
		if (IntentionGraph.getInstance().getDefaultScale() == IntentionGraph.getInstance().getScale())
		{
			IntentionGraph.getInstance().setFocusToCluster(IntentionGraph.getInstance().getClusterInFocus(), false);
			return;
		}
		
		if (scale <= 0.2)
		{
			scale = 0.1;
		}
		else if (scale <= 1.0)
		{
			scale -= 0.1;
		}
		else if (scale < 1.5)
		{
			scale = 0.9;
		}
		else
		{
			scale -= 1.0;
		}
		
		IntentionGraph.getInstance().setScale(scale);
		SwingUtilities.invokeLater(
				new Runnable() { public void run() {
					final Point2D center =  new Point2D.Double(IntentionGraph.getInstance().getGlobalCoordinatesForVisibleBounds().getCenterX(),
							IntentionGraph.getInstance().getGlobalCoordinatesForVisibleBounds().getCenterY());

					IntentionGraph.getInstance().translate(center.getX() - centerOriginal.getX(), 
							center.getY() - centerOriginal.getY());
					IntentionGraph.getInstance().repaint(); 
				}});
	}
}
