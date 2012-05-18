package calico.plugins.iip.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.util.IntentionalInterfacesGraphics;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CIntentionCell
{
	public static final String DEFAULT_TITLE = "<default>";
	private static final double MINIMUM_SNAPSHOT_SCALE = 1.0;
	public static final Font COORDINATES_FONT = new Font("Helvetica", Font.BOLD, 12);
	public static final Color COORDINATES_COLOR = Color.blue;
	private static final Insets THUMBNAIL_INSETS = new Insets(2, 2, 2, 2);
	private static final Dimension THUMBNAIL_SIZE = new Dimension(100, 60);

	private enum BorderColor
	{
		PLAIN(Color.black),
		HIGHLIGHTED(new Color(0xFFFF30));

		Color color;

		private BorderColor(Color color)
		{
			this.color = color;
		}
	}

	private long uuid;
	private long canvas_uuid;
	private Point2D location;
	private String title;
	private long intentionTypeId = -1L;

	private boolean highlighted = false;

	private final Shell shell;

	public CIntentionCell(long uuid, long canvas_uuid, Point2D location, String title)
	{
		this.uuid = uuid;
		this.canvas_uuid = canvas_uuid;
		this.location = location;
		this.title = title;

		shell = new Shell(location.getX(), location.getY());

		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).addChild(shell);
	}

	public void initialize()
	{
		shell.updateContents();
	}

	private Color currentBorderColor()
	{
		if (highlighted)
		{
			return BorderColor.HIGHLIGHTED.color;
		}
		else
		{
			return BorderColor.PLAIN.color;
		}
	}

	public long getId()
	{
		return uuid;
	}

	public long getCanvasId()
	{
		return canvas_uuid;
	}

	public String getTitle()
	{
		if (title.equals(DEFAULT_TITLE))
		{
			return "Canvas " + CCanvasController.canvasdb.get(canvas_uuid).getIndex();
		}
		return title;
	}

	public boolean hasUserTitle()
	{
		return !title.equals(DEFAULT_TITLE);
	}

	public void setTitle(String title)
	{
		this.title = title;
		shell.titleBar.updateTitle();
	}

	public Long getIntentionTypeId()
	{
		return intentionTypeId;
	}

	public void setIntentionType(long intentionTypeId)
	{
		this.intentionTypeId = intentionTypeId;
	}

	public boolean hasIntentionType()
	{
		return intentionTypeId >= 0L;
	}

	public void clearIntentionType()
	{
		intentionTypeId = -1L;
	}

	public boolean contains(Point2D point)
	{
		PBounds bounds = shell.getGlobalBounds();
		return ((point.getX() > bounds.x) && (point.getY() > bounds.y) && ((point.getX() - bounds.x) < bounds.width) && (point.getY() - bounds.y) < bounds.height);
	}

	public Point2D getLocation()
	{
		return shell.getBounds().getOrigin();
	}

	public Point2D getCenter()
	{
		return shell.thumbnailBounds.getCenter2D();
	}

	public void setLocation(double x, double y)
	{
		location.setLocation(x, y);
		shell.setX(x);
		shell.setY(y);

		shell.repaint();
	}

	public Dimension2D getSize()
	{
		return shell.thumbnailBounds.getSize();
	}

	public PBounds copyBounds()
	{
		return (PBounds) shell.thumbnailBounds.clone();
	}

	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
		shell.repaint();
	}

	public boolean isInGraphFootprint()
	{
		return IntentionGraph.getInstance().getLocalBounds(IntentionGraph.Layer.CONTENT).intersects(shell.getBounds());
	}

	public void contentsChanged()
	{
		shell.canvasSnapshot.contentsChanged();
	}

	public void updateIconification()
	{
		shell.updateIconification();
		shell.repaint();
	}

	private boolean scaleAllowsSnapshot()
	{
		return ((!IntentionGraph.getInstance().getIconifyMode()) || (IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale() >= MINIMUM_SNAPSHOT_SCALE));
	}

	private static final int BORDER_WIDTH = 1;

	private class Shell extends PComposite implements PropertyChangeListener
	{
		private final PImage canvasAddress;
		private final CanvasSnapshot canvasSnapshot = new CanvasSnapshot();
		private final TitleBar titleBar = new TitleBar();

		private boolean showingSnapshot = false;

		private PBounds thumbnailBounds = new PBounds();

		private double lastScale = Double.MIN_VALUE;
		boolean updateIconification = false;

		public Shell(double x, double y)
		{
			canvasAddress = new PImage(IntentionalInterfacesGraphics.superimposeCellAddress(
					CalicoIconManager.getIconImage("intention-graph.obscured-intention-cell"), canvas_uuid));
			addChild(canvasAddress);

			addChild(titleBar);

			thumbnailBounds.setRect(x, y, THUMBNAIL_SIZE.width - (CCanvas.ROUNDED_RECTANGLE_OVERFLOW + CCanvas.CELL_MARGIN), THUMBNAIL_SIZE.height
					- (CCanvas.ROUNDED_RECTANGLE_OVERFLOW + CCanvas.CELL_MARGIN));
			setBounds(thumbnailBounds);

			titleBar.setWidth(thumbnailBounds.getWidth());

			updateIconification();
			repaint();

			IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).addPropertyChangeListener(PNode.PROPERTY_TRANSFORM, this);
		}

		void updateIconification()
		{
			lastScale = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
			if (showingSnapshot != scaleAllowsSnapshot())
			{
				if (showingSnapshot)
				{
					removeChild(canvasSnapshot.snapshot);
					addChild(canvasAddress);
				}
				else
				{
					removeChild(canvasAddress);
					addChild(canvasSnapshot.snapshot);
				}

				showingSnapshot = !showingSnapshot;
			}
		}

		void updateContents()
		{
			if (IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale() != lastScale)
			{
				updateIconification();
			}

			if (canvasSnapshot.isDirty)
			{
				canvasSnapshot.contentsChanged();
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent event)
		{
			updateContents();
		}

		@Override
		protected void paint(PPaintContext paintContext)
		{
			super.paint(paintContext);

			Graphics2D g = paintContext.getGraphics();
			Color c = g.getColor();

			g.setColor(currentBorderColor());
			g.translate(thumbnailBounds.x, thumbnailBounds.y);
			g.drawRoundRect(0, 0, ((int) thumbnailBounds.width) - 1, ((int) thumbnailBounds.height) - 1, 10, 10);
			IntentionalInterfacesGraphics.superimposeCellAddressInCorner(g, canvas_uuid, thumbnailBounds.width - (2 * BORDER_WIDTH), COORDINATES_FONT,
					COORDINATES_COLOR);

			g.translate(-thumbnailBounds.x, -thumbnailBounds.y);
			g.setColor(c);
		}

		@Override
		protected void layoutChildren()
		{
			titleBar.setX(getX());
			titleBar.setY(getY() - 20);

			thumbnailBounds.setOrigin(getX(), getY()); 

			if (showingSnapshot)
			{
				canvasSnapshot.snapshot.setBounds(thumbnailBounds.x + BORDER_WIDTH, thumbnailBounds.y + BORDER_WIDTH, thumbnailBounds.width
						- (2 * BORDER_WIDTH), thumbnailBounds.height - (2 * BORDER_WIDTH));
			}
			else
			{
				canvasAddress.setBounds(thumbnailBounds.x + BORDER_WIDTH, thumbnailBounds.y + BORDER_WIDTH, thumbnailBounds.width - (2 * BORDER_WIDTH),
						thumbnailBounds.height - (2 * BORDER_WIDTH));
			}
		}
	}

	private class TitleBar extends PComposite
	{
		private final int HEIGHT = 20;
		private final int LEFT_INSET = 2;
		private final int TEXT_INSET = 1;

		private final PText title = new PText();

		public TitleBar()
		{
			// width is arbitrary, it will be immediately changed by the Shell
			setBounds(0, 0, 100, HEIGHT);

			addChild(title);
			updateTitle();
		}

		private void updateTitle()
		{
			title.setText(getTitle());
			repaint();
		}

		@Override
		protected void layoutChildren()
		{
			PBounds bounds = getBounds();
			title.setBounds(bounds.x + LEFT_INSET, bounds.y + TEXT_INSET, bounds.width - LEFT_INSET, bounds.height - (2 * TEXT_INSET));
		}
	}

	private class CanvasSnapshot
	{
		private final PImage snapshot = new PImage();

		private boolean isDirty = true;

		boolean isOnScreen()
		{
			return (isInGraphFootprint() && scaleAllowsSnapshot());
		}

		void contentsChanged()
		{
			if (isInGraphFootprint() && shell.showingSnapshot)
			{
				updateSnapshot();
			}
			else
			{
				isDirty = true;
			}
		}

		private void updateSnapshot()
		{
			long start = System.currentTimeMillis();

			snapshot.setImage(IntentionalInterfacesGraphics.createCanvasThumbnail(canvas_uuid, THUMBNAIL_INSETS));
			snapshot.setBounds(shell.thumbnailBounds);
			isDirty = false;

			snapshot.repaint();
		}
	}
}
