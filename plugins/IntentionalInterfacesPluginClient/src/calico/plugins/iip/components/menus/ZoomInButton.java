package calico.plugins.iip.components.menus;

import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

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
public class ZoomInButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;



	/**
	 * Instantiated via reflection in CanvasStatusBar
	 */
	public ZoomInButton()
	{


		try
		{
			setImage(CalicoIconManager.getIconImage("intention-graph.zoom-in"));
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
		if (scale >= 9.0)
		{
			scale = 10.0;
		}
		else if (scale <= 0.9)
		{
			scale += 0.1;
		}
		else if (scale < 0.95)
		{
			scale = 1.5;
		}
		else
		{
			scale += 1.0;
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
