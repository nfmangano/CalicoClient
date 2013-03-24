package calico.plugins.iip.components.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import calico.Calico;
import calico.CalicoDraw;
import calico.controllers.CCanvasController;
import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.CIntentionType;
import calico.plugins.iip.components.IntentionPanelLayout;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * Panel in the Canvas View containing all the available tags in a list. If the current canvas has been tagged, that
 * tag's row in the panel is highlighted with the tag color as a solid background. When the user clicks on a tag, the
 * panel disappears and the tag is assigned to the canvas via <code>CIntentionCellController</code>.
 * 
 * There is only one instance of the tag panel, and it is moved from canvas to canvas as the user traverses their
 * design.
 * 
 * When tags are added or removed, this tag panel expects to be notified with a call to
 * <code>updateIntentionTypes()</code> so that it can make the visual changes.
 * 
 * Earlier versions of this panel allowed the user to edit the tags in various ways.
 * 
 * @author Byron Hawkins
 */
public class CanvasTagPanel implements StickyItem, PropertyChangeListener, CalicoEventListener
{
	public static CanvasTagPanel getInstance()
	{
		return INSTANCE;
	}

	private static CanvasTagPanel INSTANCE = new CanvasTagPanel();

	public static final double PANEL_COMPONENT_INSET = 5.0;

	public static final double ROW_HEIGHT = 30.0;
	public static final double ROW_TEXT_INSET = 1.0;

	private final PanelNode panel;

	private final long uuid;
	private long canvas_uuid;

	private boolean visible;
	private IntentionPanelLayout layout;

	private boolean initialized = false;

	private CanvasTagPanel()
	{
		uuid = Calico.uuid();
		this.canvas_uuid = 0L;

		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());

		panel = new PanelNode();

		panel.setPaint(Color.white);
		panel.setVisible(visible = false);
		
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CLINK_CREATE, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CLINK_MOVE_ANCHOR, this, CalicoEventHandler.PASSIVE_LISTENER);

		initialized = true;
	}

	@Override
	public long getUUID()
	{
		return uuid;
	}

	@Override
	public boolean containsPoint(Point p)
	{
		return panel.getBounds().contains(p);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		if (visible && event.getPropertyName().equals(PNode.PROPERTY_CHILDREN))
		{
			updatePanelBounds();
			CalicoDraw.repaint(panel);
//			panel.repaint();
		}
	}

	public boolean isVisible()
	{
		return panel.getVisible();
	}

	public void setVisible(boolean b)
	{
		// if the originating canvas is in the inner circle, don't display this

		
		if (visible == b)
		{
			return;
		}

		visible = b;

		if (b)
		{
			refresh();
		}
		else
		{
			panel.setVisible(false);
		}

		if (b)
		{
			CalicoInputManager.registerStickyItem(this);
			CalicoDraw.repaint(panel);
//			panel.repaint();
		}
		else
		{
			CalicoInputManager.unregisterStickyItem(this);
		}
	}

	public void moveTo(long canvas_uuid)
	{
		this.canvas_uuid = canvas_uuid;

		if (panel.getParent() != null)
		{
			panel.getParent().removePropertyChangeListener(this);
			panel.getParent().removeChild(panel);
		}
		refresh();
		CalicoDraw.addChildToNode(CCanvasController.canvasdb.get(canvas_uuid).getCamera(), panel);
		CCanvasController.canvasdb.get(canvas_uuid).getCamera().addChild(panel);
		CCanvasController.canvasdb.get(canvas_uuid).getCamera().addPropertyChangeListener(this);
	}

	public void refresh()
	{
		if (!visible)
		{
			return;
		}

		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable() {
				public void run()
				{
					refresh();
				}
			});
			return;
		}

		panel.refresh();
		updatePanelBounds();
		CalicoDraw.setVisible(panel, true);
//		panel.setVisible(true);
		CalicoDraw.repaint(panel);
//		panel.repaint();
	}

	public void updateIntentionTypes()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable() {
				public void run()
				{
					updateIntentionTypes();
				}
			});
			return;
		}

		panel.updateIntentionTypes();
		panel.refresh();
		updatePanelBounds();
	}

	private void updatePanelBounds()
	{
		double width = panel.calculateWidth();
		double height = panel.calculateHeight();
		layout.updateBounds(panel, width, height);

		if (visible)
		{
			CalicoDraw.repaint(panel);
//			panel.repaint();
		}
	}

	public void setLayout(IntentionPanelLayout layout)
	{
		this.layout = layout;
	}

	private enum IntentionTypeRowEditMode
	{
		NONE,
		RENAME,
		SET_COLOR,
		REMOVE;

		Image image;
	}

	/**
	 * Represents one tag row panel's Piccolo component hierarchy. Paints the selection highlight in
	 * <code>paint()</code>.
	 * 
	 * @author Byron Hawkins
	 */
	private class IntentionTypeRow extends PComposite
	{
		private final CIntentionType type;
		private final PText label;
		private final PText labelTag;
		private final PImage arrowIcon =
				new PImage(CalicoIconManager.getIconImage(
						"intention.bullet-point"));
		private final double arrowIconWidth = ROW_HEIGHT - (2 * ROW_TEXT_INSET);
		private final Color tagColor;

		private boolean selected = false;

		public IntentionTypeRow(CIntentionType type)
		{
			this.type = type;
			label = new PText(type.getDescription() + " ");
			label.setConstrainWidthToTextWidth(true);
			label.setConstrainHeightToTextHeight(true);
			label.setFont(label.getFont().deriveFont(20f));
			
			labelTag = new PText(" ["+type.getName()+"] ");
			labelTag.setConstrainWidthToTextWidth(true);
			labelTag.setConstrainHeightToTextHeight(true);
			labelTag.setFont(label.getFont().deriveFont(20f));
			
			tagColor = type.getColor();

			CalicoDraw.addChildToNode(this, label);
			CalicoDraw.addChildToNode(this, labelTag);
			CalicoDraw.addChildToNode(this, arrowIcon);
			
//			addChild(label);
		}

		void tap(Point point)
		{
			IntentionCanvasController.getInstance().showTagPanel(false);
			CIntentionCellController.getInstance().toggleCellIntentionType(CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).getId(),
					type.getId(), !selected, false);
			IntentionCanvasController.getInstance().linkCanvasToOriginatingContext();
//			IntentionCanvasController.getInstance().collapseLikeIntentionTypes();
			
//			CanvasTitlePanel.getInstance().refresh();
		}

		double getMaxWidth()
		{
			return arrowIconWidth + label.getBounds().width + labelTag.getBounds().width + (PANEL_COMPONENT_INSET * 3);
		}

		void setSelected(boolean b)
		{
			selected = b;

			CalicoDraw.repaint(this);
//			repaint();
		}

		@Override
		protected void layoutChildren()
		{
			PBounds rowBounds = getBounds();
			PBounds labelBounds = label.getBounds();

			label.setBounds(rowBounds.x + arrowIconWidth + PANEL_COMPONENT_INSET, rowBounds.y + ROW_TEXT_INSET, label.getWidth(), ROW_HEIGHT - (2 * ROW_TEXT_INSET));
			labelTag.setBounds(rowBounds.x + arrowIconWidth + label.getWidth() + PANEL_COMPONENT_INSET, rowBounds.y + ROW_TEXT_INSET, labelTag.getWidth(), ROW_HEIGHT - (2 * ROW_TEXT_INSET));
			arrowIcon.setBounds(rowBounds.x, rowBounds.y + ROW_TEXT_INSET, arrowIconWidth, arrowIconWidth);
		}

		@Override
		protected void paint(PPaintContext paintContext)
		{
			Graphics2D g = paintContext.getGraphics();
			Color c = g.getColor();
			g.setColor(tagColor);
			Rectangle labelTagBounds = new Rectangle((int)labelTag.getX(), (int)labelTag.getY(), (int)labelTag.getWidth(), (int)labelTag.getHeight());
			g.fillRect(labelTagBounds.x, labelTagBounds.y, labelTagBounds.width, labelTagBounds.height);
			g.setColor(c);
			
			if (selected)
			{
				PBounds bounds = getBounds();
				c = g.getColor();
				g.setColor(type.getColor());
				g.fillRect((int) bounds.x, (int) bounds.y, (int) bounds.width, (int) bounds.height);
				g.setColor(c);
			}

			super.paint(paintContext);
		}
	}

	/**
	 * Represents the panel in the Piccolo component hierarchy. Automatically sizes to fit on
	 * <code>updateIntentionTypes(). Paints its own outline with rounded corners in <code>paint()</code>.
	 * 
	 * @author Byron Hawkins
	 */
	private class PanelNode extends PComposite
	{
		private final List<IntentionTypeRow> typeRows = new ArrayList<IntentionTypeRow>();
		private final PText titleCaptionP1 = new PText("In relation (");
		private final PText titleCaptionP2 = new PText(") to " + "" + ", this canvas is: ");
		private final PImage arrowIcon =
				new PImage(CalicoIconManager.getIconImage(
						"intention.link-canvas"));
		private final double arrowIconWidth = ROW_HEIGHT - (2 * ROW_TEXT_INSET);
		
		public PanelNode()
		{
			titleCaptionP1.setFont(titleCaptionP2.getFont().deriveFont(20f));
			titleCaptionP2.setFont(titleCaptionP2.getFont().deriveFont(20f));
		}

		void tap(Point point)
		{
			for (IntentionTypeRow row : typeRows)
			{
				if (row.getBoundsReference().contains(point))
				{
					row.tap(point);
					break;
				}
			}
		}

		double calculateWidth()
		{
			double width = titleCaptionP1.getWidth() + arrowIconWidth + titleCaptionP2.getWidth();
			for (IntentionTypeRow row : typeRows)
			{
				double rowWidth = row.getMaxWidth() + (PANEL_COMPONENT_INSET * 3);
				if (rowWidth > width)
				{
					width = rowWidth;
				}
			}
			return width;
		}

		double calculateHeight()
		{
			return typeRows.size() * ROW_HEIGHT + ROW_HEIGHT;
		}

		void updateIntentionTypes()
		{
			for (IntentionTypeRow row : typeRows)
			{
				CalicoDraw.removeNodeFromParent(row);
//				removeChild(row);
			}
			typeRows.clear();
			
			CalicoDraw.addChildToNode(this, titleCaptionP1);
			CalicoDraw.addChildToNode(this, arrowIcon);
			CalicoDraw.addChildToNode(this, titleCaptionP2);

			for (CIntentionType type : IntentionCanvasController.getInstance().getActiveIntentionTypes())
			{
				IntentionTypeRow row = new IntentionTypeRow(type);
				CalicoDraw.addChildToNode(this, row);
//				addChild(row);
				typeRows.add(row);
			}

			CalicoDraw.repaint(this);
//			repaint();
		}

		void refresh()
		{
			if (canvas_uuid == 0L)
			{
				return;
			}
			
			/**
			 * In this rather obtuse method call, we use an inline if-statement---
			 * 		if there is an originating canvas, use the name of that canvas
			 * 		if there isn't an originating canvas (in which case this won't even be seen...), assign an empty string
			 */
			titleCaptionP2.setText(") to " + 
					((IntentionCanvasController.getInstance().getCurrentOriginatingCanvasId() != 0l)?				
					CIntentionCellController.getInstance().getCellByCanvasId(
							IntentionCanvasController.getInstance().getCurrentOriginatingCanvasId()).getTitle()
					:
					""
							) + ", this canvas is: ");
			titleCaptionP2.recomputeLayout();
			layoutChildren();

			CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid);
			if (cell != null)
				for (IntentionTypeRow row : typeRows)
				{
					row.setSelected(cell.getIntentionTypeId() == row.type.getId());
				}
		}

		@Override
		protected void layoutChildren()
		{
			if (!initialized)
			{
				return;
			}
			

			

			PBounds bounds = panel.getBounds();
			double y = bounds.y;
			titleCaptionP1.setBounds(bounds.x, y, titleCaptionP1.getWidth(), ROW_HEIGHT);
			arrowIcon.setBounds(bounds.x + titleCaptionP1.getWidth(), y, arrowIconWidth, arrowIconWidth);
			titleCaptionP2.setBounds(bounds.x + titleCaptionP1.getWidth() + arrowIconWidth, y, titleCaptionP2.getWidth(), ROW_HEIGHT);
			y += ROW_HEIGHT;			
			for (IntentionTypeRow row : typeRows)
			{
				row.setBounds(bounds.x, y, bounds.width, ROW_HEIGHT);
				y += ROW_HEIGHT;
			}
		}

		@Override
		protected void paint(PPaintContext paintContext)
		{
			super.paint(paintContext);

			Graphics2D g = paintContext.getGraphics();
//			Color c = g.getColor();

			PBounds bounds = getBounds();
//			g.setColor(Color.black);
			g.translate(bounds.x, bounds.y);
//			g.drawRoundRect(0, 0, ((int) bounds.width) - 1, ((int) bounds.height) - 1, 14, 14);

			g.translate(-bounds.x, -bounds.y);
//			g.setColor(c);
		}
	}

	/**
	 * Input only processes tap events, so only the pressed state is tracked.
	 * 
	 * @author Byron Hawkins
	 */
	private enum InputState
	{
		IDLE,
		PRESSED
	}

	/**
	 * Recognizes a tap as a press which is held for less than the <code>tapDuration</code> and does not include a drag
	 * beyond the <code>dragThreshold</code>. The <code>state</code> is voluntarily locked for reading and writing under
	 * <code>stateLock</code>.
	 * 
	 * @author Byron Hawkins
	 */
	private class InputHandler extends CalicoAbstractInputHandler
	{
		private final Object stateLock = new Object();

		private final long tapDuration = 500L;
		private final double dragThreshold = 10.0;

		private InputState state = InputState.IDLE;
		private long pressTime = 0L;
		private Point pressAnchor;

		@Override
		public void actionReleased(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				if ((state == InputState.PRESSED) && ((System.currentTimeMillis() - pressTime) < tapDuration))
				{
					panel.tap(event.getGlobalPoint());
				}
				state = InputState.IDLE;
			}

			pressTime = 0L;

			CalicoInputManager.unlockHandlerIfMatch(uuid);
		}

		@Override
		public void actionDragged(InputEventInfo event)
		{
			if (pressAnchor.distance(event.getGlobalPoint()) < dragThreshold)
			{
				// not a drag, completely ignore this event
				return;
			}

			synchronized (stateLock)
			{
				if (state == InputState.PRESSED)
				{
					state = InputState.IDLE;
					pressTime = 0L;
				}
			}
		}

		@Override
		public void actionPressed(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				state = InputState.PRESSED;

				pressTime = System.currentTimeMillis();
				pressAnchor = event.getGlobalPoint();
			}
		}
	}

	@Override
	public void handleCalicoEvent(int event, CalicoPacket p) {
		
		if (event == IntentionalInterfacesNetworkCommands.CLINK_CREATE
				|| event == IntentionalInterfacesNetworkCommands.CLINK_MOVE_ANCHOR)
		{
			refresh();
		}
		
	}
	
}
