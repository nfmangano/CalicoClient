package calico.plugins.iip.components;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import calico.controllers.CCanvasController;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.util.IntentionalInterfacesGraphics;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CIntentionCell
{
	private static final double MINIMUM_SNAPSHOT_SCALE = 2.0;
	
	long uuid;
	long canvas_uuid;
	Point2D location;

	private final PComposite shell = new PComposite();
	private final PImage canvasAddress;
	private final CanvasSnapshot canvasSnapshot = new CanvasSnapshot();

	public CIntentionCell(long uuid, long canvas_uuid, double x, double y)
	{
		this.uuid = uuid;
		this.canvas_uuid = canvas_uuid;
		this.location = new Point2D.Double(x, y);

		canvasAddress = new PImage(IntentionalInterfacesGraphics.superimposeCellAddress(
				CalicoIconManager.getIconImage("intention-graph.obscured-intention-cell"), canvas_uuid));
		shell.addChild(canvasAddress);
		shell.setBounds(x, y, canvasAddress.getWidth(), canvasAddress.getHeight());
		canvasAddress.setBounds(shell.getBounds());

		IntentionGraph.getInstance().getLayer().addChild(shell);
		IntentionGraph.getInstance().getLayer().addPropertyChangeListener(PNode.PROPERTY_BOUNDS, canvasSnapshot);
	}

	public long getId()
	{
		return uuid;
	}

	public long getCanvasId()
	{
		return canvas_uuid;
	}

	public boolean contains(Point point)
	{
		PBounds bounds = shell.getGlobalBounds();
		return ((point.x > bounds.x) && (point.y > bounds.y) && ((point.x - bounds.x) < bounds.width) && (point.y - bounds.y) < bounds.height);
	}

	public Point2D getLocation()
	{
		return shell.getBounds().getOrigin();
	}

	public void setLocation(double x, double y)
	{
		System.out.println("Move cell " + CCanvasController.canvasdb.get(canvas_uuid).getGridCoordTxt() + " to " + x + ", " + y);

		location.setLocation(x, y);
		shell.setBounds(x, y, shell.getBounds().getWidth(), shell.getBounds().getHeight());
		canvasAddress.setBounds(x, y, shell.getBounds().getWidth(), shell.getBounds().getHeight());

		shell.repaint();
	}

	public boolean isVisible()
	{
		return shell.getVisible();
	}

	public void setVisible(boolean b)
	{
		shell.setVisible(b);

		System.out.println((b ? "Showing " : "Hiding ") + " a CIC: " + CIntentionCellController.getInstance().listVisibleCellAddresses());
	}
	
	public void contentsChanged()
	{
		canvasSnapshot.contentsChanged();
	}
	
	private class CanvasSnapshot implements PropertyChangeListener
	{
		private final PImage snapshot = new PImage();
		
		private boolean isDirty = false;
		private boolean active = false;
		
		boolean isOnScreen()
		{
			return (IntentionGraph.getInstance().getBounds().intersects(shell.getBounds()) && (IntentionGraph.getInstance().getLayer().getScale() >= MINIMUM_SNAPSHOT_SCALE));
		}
		
		void contentsChanged()
		{
			if (isOnScreen())
			{
				updateSnapshot();
			}
			else
			{
				isDirty = true;
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent event)
		{
			if (active != isOnScreen())
			{
				if (active)
				{
					shell.removeChild(snapshot);
					shell.addChild(canvasAddress);
				}
				else
				{
					shell.removeChild(canvasAddress);
					shell.addChild(snapshot);
				}
				
				active = !active;
			}
			
			if (isDirty && active)
			{
				updateSnapshot();
			}
		}
		
		private void updateSnapshot()
		{
			PBounds bounds = snapshot.getBounds();
			snapshot.setImage(CCanvasController.canvasdb.get(canvas_uuid).getContentCamera().toImage());
			snapshot.setBounds(bounds);
			isDirty = false;
		}
	}
}
