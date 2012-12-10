package calico.plugins.iip.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingUtilities;

import calico.CalicoDataStore;
import calico.CalicoDraw;
import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.util.IntentionalInterfacesGraphics;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * Represents a canvas thumbnail in the Intention View, both in the plugin's internal model and in the Piccolo component
 * hierarchy (with inner class <code>Shell</code>). The classname <code>CIntentionCell</code> is generally abbreviated
 * "CIC" throughout the documentation.
 * 
 * @author Byron Hawkins
 */
public class CIntentionCell implements CalicoEventListener
{
	public static final String DEFAULT_TITLE = "<default>";
	private static final double MINIMUM_SNAPSHOT_SCALE = 1.0;
	public static final Color COORDINATES_COLOR = Color.blue;
	private static final Insets THUMBNAIL_INSETS = new Insets(2, 2, 2, 2);
	public static final Dimension THUMBNAIL_SIZE = new Dimension(200, 130);
	public static final Font COORDINATES_FONT = new Font("Helvetica", Font.BOLD, THUMBNAIL_SIZE.width / 10);

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

	/**
	 * Identifies this cell.
	 */
	private long uuid;
	/**
	 * Identifies the canvas for which this cell renders thumbnails.
	 */
	private long canvas_uuid;
	/**
	 * Pixel position in the Intention View of the upper left corner of the canvas thumbnail.
	 */
	private Point2D location;
	/**
	 * Title of the canvas, which appears both on the CIC and on the canvas itself (represented there by
	 * <code>CanvasTitlePanel</code>). The title is maintained in this class because it is a feature of this plugin, but
	 * the title is still effectively associated with the canvas.
	 */
	private String title;
	/**
	 * Tag associated with the canvas of this CIC. The tag is maintained in this class because it is a feature of this
	 * plugin, but the tag is still effectively associated with the canvas.
	 */
	private long intentionTypeId = -1L;

	/**
	 * Rendering state flag indicating that the border of the CIC is currently drawn in the highlighted color.
	 */
	private boolean highlighted = false;
	/**
	 * State flag used in the construction process of a canvas. See <code>CIntentionCellFactory</code> for details.
	 */
	private boolean isNew = false;

	/**
	 * Represents the CIC in the Piccolo component hierarchy of the Intention View.
	 */
	private final Shell shell;

	/**
	 * Create a new CIC and add it to the Intention View.
	 */
	public CIntentionCell(long uuid, long canvas_uuid, Point2D location, String title)
	{
		this.uuid = uuid;
		this.canvas_uuid = canvas_uuid;
		this.location = location;
		this.title = title;

		shell = new Shell(location.getX(), location.getY());
		CalicoDraw.addChildToNode(IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT), shell);
		
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CIC_TAG, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CIC_UNTAG, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CIC_SET_TITLE, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CIC_UPDATE_FINISHED, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CIC_TOPOLOGY, this, CalicoEventHandler.PASSIVE_LISTENER);
		 
		
//		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).addChild(shell);
	}

	public boolean isNew()
	{
		return isNew;
	}

	public void setNew(boolean isNew)
	{
		this.isNew = isNew;
	}

	/**
	 * Create and install the thumbnail image.
	 */
	public void initialize()
	{
		shell.updateContents();
	}

	/**
	 * Detach all the resources of the CIC from the Intention View.
	 */
	public void delete()
	{
		shell.delete();
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
			if (CCanvasController.canvasdb.containsKey(canvas_uuid))
				return "Canvas " + CCanvasController.canvasdb.get(canvas_uuid).getIndex();
			else
				return "Canvas ";
		}
		return title;
	}

	/**
	 * Return true if the user has set a title on the canvas associated to this CIC, or false if the canvas uses the
	 * default title.
	 */
	public boolean hasUserTitle()
	{
		return !title.equals(DEFAULT_TITLE);
	}

	public void setTitle(String title)
	{
		this.title = title;
//		shell.titleBar.updateTitle();
	}

	public Long getIntentionTypeId()
	{
		return intentionTypeId;
	}

	public void setIntentionType(long intentionTypeId)
	{
		this.intentionTypeId = intentionTypeId;
	}

	/**
	 * Return true if the user has assigned a tag to the canvas associated to this CIC.
	 */
	public boolean hasIntentionType()
	{
		return intentionTypeId >= 0L;
	}

	/**
	 * Remove any tag that may have been assigned to the canvas associated with this CIC.
	 */
	public void clearIntentionType()
	{
		intentionTypeId = -1L;
	}

	/**
	 * Return true when <code>point</code> contacts the physical coordinate region occupied by the canvas thumbnail of
	 * this CIC in the Intention View. It is assumed that <code>point</code> specifies screen coordinates.
	 */
	public boolean contains(Point2D point)
	{
		PBounds bounds = shell.getGlobalBounds();
		return ((point.getX() > bounds.x) && (point.getY() > bounds.y) && ((point.getX() - bounds.x) < bounds.width) && (point.getY() - bounds.y) < bounds.height);
	}

	/**
	 * Get the pixel position of this CIC within the Intention View.
	 */
	public Point2D getLocation()
	{
		return shell.getBounds().getOrigin();
	}

	/**
	 * Get the bounds of this CIC in screen coordinates.
	 */
	public PBounds getGlobalBounds()
	{
		PBounds bounds = shell.getBounds();
		// bounds.setOrigin(0.0, 0.0);
		return new PBounds(shell.localToGlobal(bounds)); // getBounds();
	}

	/**
	 * Get the center point of this CIC in Intention View coordinates.
	 */
	public Point2D getCenter()
	{
		return shell.thumbnailBounds.getCenter2D();
	}

	/**
	 * Set the location of this CIC in Intention View coordinates.
	 */
	public void setLocation(final double x, final double y)
	{
		SwingUtilities.invokeLater(
				new Runnable() { public void run() { 
					location.setLocation(x, y);
					shell.setX(x);
					shell.setY(y);
					shell.repaint();
				}});


		CalicoDraw.repaint(shell);
//		shell.repaint();
	}

	public Dimension2D getSize()
	{
		return shell.thumbnailBounds.getSize();
	}

	/**
	 * Clone the bounds of this CIC, which occur in Intention View coordinates.
	 */
	public PBounds copyBounds()
	{
		return (PBounds) shell.thumbnailBounds.clone();
	}

	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
		CalicoDraw.repaint(shell);
//		shell.repaint();
	}

	/**
	 * Return true if this CIC can be seen in the present visible area of the Intention View.
	 */
	public boolean isInGraphFootprint()
	{
		return IntentionGraph.getInstance().getLocalBounds(IntentionGraph.Layer.CONTENT).intersects(shell.getBounds());
	}

	/**
	 * Update the canvas thumbnail to reflect its most recent contents.
	 */
	public void contentsChanged()
	{
		shell.canvasSnapshot.contentsChanged();
	}

	/**
	 * Update the iconification state of this CIC, based on the current zoom ratio and whether iconification mode is
	 * enabled. This feature is obsolete.
	 */
	public void updateIconification()
	{
		shell.updateIconification();
		CalicoDraw.repaint(shell);
//		shell.repaint();
	}

	/**
	 * Update the list of users drawn in the upper right corner of this CIC.
	 */
	public void updateUserList()
	{
		shell.userList.updateUsers();
	}

	/**
	 * Return true if the present zoom ratio allows the thumbnail to be drawn. This is used for iconification, which is
	 * obsolete.
	 */
	private boolean scaleAllowsSnapshot()
	{
		return ((!IntentionGraph.getInstance().getIconifyMode()) || (IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale() >= MINIMUM_SNAPSHOT_SCALE));
	}

	private static final int BORDER_WIDTH = 1;

	/**
	 * Represents the CIC in the Piccolo component hierarchy of the Intention View. The size of a CIC is statically
	 * defined as <code>THUMBNAIL_SIZE</code>, so all scaling occurs globally to the Intention View.
	 * 
	 * @author Byron Hawkins
	 */
	private class Shell extends PComposite implements PropertyChangeListener
	{
		private final Color BACKGROUND_COLOR = new Color(0xFF, 0xFF, 0xFF, 0xCC);

		/**
		 * Renders the canvas number in the upper left corner.
		 */
		private final PImage canvasAddress;
		/**
		 * Renders the thumbnail image.
		 */
		private final CanvasSnapshot canvasSnapshot = new CanvasSnapshot();
		/**
		 * Renders the title of the canvas, above the CIC and left justified.
		 */
		private final TitleBar titleBar = new TitleBar();
		/**
		 * Renders the list of users currently viewing the canvas associated with this CIC.
		 */
		private final UserList userList = new UserList();

		/**
		 * State flag indicating that the thumbnail is currently being displayed. This flag supports iconification,
		 * which is obsolete.
		 */
		private boolean showingSnapshot = false;

		/**
		 * Bounds of the thumbnail rectangle, not including the title sitting above the CIC.
		 */
		private PBounds thumbnailBounds = new PBounds();

		/**
		 * Maintains the last zoom ratio, to avoid regenerating the thumbnail image when the Intention View display
		 * changes in any way that does not affect the rendering of this thumbnail (including miniscule zoom changes
		 * that have no net effect).
		 */
		private double lastScale = Double.MIN_VALUE;

		public Shell(double x, double y)
		{
			canvasAddress = new PImage(IntentionalInterfacesGraphics.superimposeCellAddress(
					CalicoIconManager.getIconImage("intention-graph.obscured-intention-cell"), canvas_uuid));
			CalicoDraw.addChildToNode(this, canvasAddress);
//			addChild(canvasAddress);

			CalicoDraw.addChildToNode(this, titleBar);
			CalicoDraw.addChildToNode(this, userList);
//			addChild(titleBar);
//			addChild(userList);

			thumbnailBounds.setRect(x, y, THUMBNAIL_SIZE.width - (CCanvas.ROUNDED_RECTANGLE_OVERFLOW + CCanvas.CELL_MARGIN), THUMBNAIL_SIZE.height
					- (CCanvas.ROUNDED_RECTANGLE_OVERFLOW + CCanvas.CELL_MARGIN));
			CalicoDraw.setNodeBounds(this, thumbnailBounds);
//			setBounds(thumbnailBounds);

			titleBar.setWidth(thumbnailBounds.getWidth());

			updateIconification();

			IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).addPropertyChangeListener(PNode.PROPERTY_TRANSFORM, this);

			CalicoDraw.moveNodeToFront(userList);
//			userList.moveToFront();
			CalicoDraw.repaint(this);
//			repaint();
		}

		void delete()
		{
			IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).removeChild(this);
			IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).removePropertyChangeListener(PNode.PROPERTY_TRANSFORM, this);
		}

		void updateIconification()
		{
			lastScale = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
			if (showingSnapshot != scaleAllowsSnapshot())
			{
				if (showingSnapshot)
				{
					CalicoDraw.removeChildFromNode(this, canvasSnapshot.snapshot);
//					removeChild(canvasSnapshot.snapshot);
					CalicoDraw.addChildToNode(this, canvasAddress);
//					addChild(canvasAddress);
				}
				else
				{
					CalicoDraw.removeChildFromNode(this, canvasAddress);
//					removeChild(canvasAddress);
					CalicoDraw.addChildToNode(this, canvasSnapshot.snapshot);
//					addChild(canvasSnapshot.snapshot);
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

			g.setColor(BACKGROUND_COLOR);
			g.fill(getBounds());

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
			titleBar.setY(getY() - titleBar.HEIGHT);

			userList.setX(getX() + 4);
			userList.setY(getY() + 2);

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
		private final int HEIGHT = THUMBNAIL_SIZE.width / 7;
		private final int LEFT_INSET = 2;
		private final int TEXT_INSET = 1;

		private final int FADE_HEIGHT = HEIGHT;
		private final Color MASK_COLOR = new Color(0xFF, 0xFF, 0xFF, 0xDD);
		private final Color TRANSPARENT = new Color(0xFF, 0xFF, 0xFF, 0x00);
		private final GradientPaint TOP_FADE = new GradientPaint(0f, 0f, TRANSPARENT, 0f, FADE_HEIGHT / 2, MASK_COLOR);
		private final GradientPaint BOTTOM_FADE = new GradientPaint(0f, FADE_HEIGHT / 2, MASK_COLOR, 0f, FADE_HEIGHT, TRANSPARENT);

		private final PText title = new PText();

		public TitleBar()
		{
			// width is arbitrary, it will be immediately changed by the Shell
			setBounds(0, 0, 100, HEIGHT);
			title.setFont(new Font("Helvetica", Font.PLAIN, THUMBNAIL_SIZE.width / 10));

			CalicoDraw.addChildToNode(this, title);
//			addChild(title);
			updateTitle();
		}

		private void updateTitle()
		{
			int index = getSiblingIndex();
			
			String tag = "";
			String titlePrefix = "";
			if (!CIntentionCellController.getInstance().isRootCanvas(canvas_uuid))
				titlePrefix = getSiblingIndex() + ". ";
			
			if (getIntentionTypeId() != -1
					&&  IntentionCanvasController.getInstance().intentionTypeExists(getIntentionTypeId()))
				tag = " (" + IntentionCanvasController.getInstance().getIntentionType(getIntentionTypeId()).getName() + ")";
			
			title.setText(titlePrefix + getTitle() + tag);
			CalicoDraw.repaint(this);
//			repaint();
		}



		@Override
		protected void layoutChildren()
		{
			PBounds bounds = getBounds();
			title.setBounds(bounds.x + LEFT_INSET, bounds.y + TEXT_INSET, bounds.width - LEFT_INSET, bounds.height - (2 * TEXT_INSET));
		}

		@Override
		protected void paint(PPaintContext paintContext)
		{
			Graphics2D g = paintContext.getGraphics();
			int yTitle = (int) (title.getY() - (((FADE_HEIGHT - HEIGHT) / 2)));
			g.translate(title.getX(), yTitle);

			g.setPaint(TOP_FADE);
			g.fillRect(-2, 0, ((int) title.getBounds().width) + 4, FADE_HEIGHT / 2);
			g.setPaint(BOTTOM_FADE);
			g.fillRect(-2, FADE_HEIGHT / 2, ((int) title.getBounds().width) + 4, FADE_HEIGHT);
			g.translate(-title.getX(), -yTitle);

			super.paint(paintContext);
		}
	}
	
	public int getSiblingIndex() {
		long parentUUID = CIntentionCellController.getInstance().getCIntentionCellParent(canvas_uuid);
		long[] siblings = CIntentionCellController.getInstance().getCIntentionCellChildren(parentUUID);
		
		int index = 1;
		for (int i = 0; i < siblings.length; i++)
		{
			if (siblings[i] == canvas_uuid)
				index = i+1;
		}
		return index;
	}
	
	public int getClusterIndex()
	{
		return IntentionGraph.getInstance().getClusterIndex(CIntentionCellController.getInstance().getClusterRootCanvasId(canvas_uuid)) + 1;
	}

	private class UserList extends PText
	{
		public UserList()
		{
			setText("Username"); // template, for sizing
			setFont(new Font("Helvetica", Font.BOLD, THUMBNAIL_SIZE.width / 10));
			setTextPaint(Color.BLUE);
			setBounds(this.getBounds().getBounds());
			setConstrainWidthToTextWidth(true);
			setConstrainHeightToTextHeight(true);

			setText("");
		}

		void updateUsers()
		{
			StringBuilder userListText = new StringBuilder();
			int[] clients = CCanvasController.canvasdb.get(canvas_uuid).getClients();
			for (int i = 0; i < clients.length; i++)
			{
				if (CalicoDataStore.clientInfo.containsKey(clients[i]) && !CalicoDataStore.clientInfo.get(clients[i]).equals(CalicoDataStore.Username))
				{
					userListText.append(CalicoDataStore.clientInfo.get(clients[i]) + "\n");
				}

			}

			if (!getText().equals(userListText.toString()))
			{
				setText(userListText.toString());
				CalicoDraw.repaint(this);
//				repaint();
			}
		}

		@Override
		protected void paint(PPaintContext paintContext)
		{
			Graphics2D g = paintContext.getGraphics();
			g.setColor(Color.white);
			g.fill(getBounds());

			super.paint(paintContext);
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
			CalicoDraw.setNodeBounds(snapshot, shell.thumbnailBounds);
//			snapshot.setBounds(shell.thumbnailBounds);
			isDirty = false;

			CalicoDraw.repaint(snapshot);
//			snapshot.repaint();
		}
	}

	@Override
	public void handleCalicoEvent(int event, CalicoPacket p) {
		
		if (event == IntentionalInterfacesNetworkCommands.CIC_TAG
				|| event == IntentionalInterfacesNetworkCommands.CIC_UNTAG
				|| event == IntentionalInterfacesNetworkCommands.CIC_SET_TITLE
				|| event == IntentionalInterfacesNetworkCommands.CIC_UPDATE_FINISHED
				|| event == IntentionalInterfacesNetworkCommands.CIC_TOPOLOGY)
		{
			shell.titleBar.updateTitle();
		}
		
	}
}
