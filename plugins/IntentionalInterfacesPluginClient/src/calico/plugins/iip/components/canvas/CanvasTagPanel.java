package calico.plugins.iip.components.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import calico.Calico;
import calico.CalicoDataStore;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.CIntentionType;
import calico.plugins.iip.components.IntentionPanelLayout;
import calico.plugins.iip.components.canvas.CanvasTitleDialog.Action;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CanvasTagPanel implements StickyItem
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

	private final Image addButtonImage;
	private final Image removeButtonImage;
	private final Image editButtonImage;
	private final Image paletteButtonImage;

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

		addButtonImage = CalicoIconManager.getIconImage("intention.add-button");
		removeButtonImage = CalicoIconManager.getIconImage("intention.remove-button");
		editButtonImage = CalicoIconManager.getIconImage("intention.edit-button");
		paletteButtonImage = CalicoIconManager.getIconImage("intention.palette-button");
		IntentionTypeRowEditMode.RENAME.image = editButtonImage;
		IntentionTypeRowEditMode.SET_COLOR.image = paletteButtonImage;
		IntentionTypeRowEditMode.REMOVE.image = removeButtonImage;

		panel = new PanelNode();

		panel.setPaint(Color.white);
		panel.setVisible(visible = false);

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

	public boolean isVisible()
	{
		return panel.getVisible();
	}

	public void setVisible(boolean b)
	{
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
			panel.repaint();
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
			panel.getParent().removeChild(panel);
		}
		refresh();
		CCanvasController.canvasdb.get(canvas_uuid).getCamera().addChild(panel);
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
		panel.setVisible(true);
		panel.repaint();
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
			panel.repaint();
		}
	}

	public void setLayout(IntentionPanelLayout layout)
	{
		this.layout = layout;
	}

	private class TitleRow extends PComposite
	{
		private final PText text = new PText();

		public TitleRow()
		{
			text.setConstrainWidthToTextWidth(true);
			text.setConstrainHeightToTextHeight(true);
			text.setFont(text.getFont().deriveFont(20f));

			addChild(text);
		}

		void tap(Point point)
		{
			CanvasTitleDialog.Action action = CanvasTitleDialog.getInstance().queryUserForLabel(
					CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid));

			if (action == Action.OK)
			{
				CIntentionCellController.getInstance().setCellTitle(CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).getId(),
						CanvasTitleDialog.getInstance().getText(), false);
			}
		}

		double getMaxWidth()
		{
			return text.getBounds().width + (2 * PANEL_COMPONENT_INSET);
		}

		void refresh()
		{
			text.setText(CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).getTitle());
		}

		@Override
		protected void layoutChildren()
		{
			PBounds bounds = getBounds();

			text.recomputeLayout();
			PBounds textBounds = text.getBounds();
			text.setBounds(bounds.x + PANEL_COMPONENT_INSET, bounds.y + ROW_TEXT_INSET, textBounds.width, textBounds.getHeight());
		}
	}

	private enum IntentionTypeRowEditMode
	{
		NONE,
		RENAME,
		SET_COLOR,
		REMOVE;

		Image image;
	}

	private class IntentionTypeRow extends PComposite
	{
		private final CIntentionType type;
		private final PText label;
		private final PImage editButton = new PImage(removeButtonImage);

		private boolean selected = false;
		private IntentionTypeRowEditMode editMode = IntentionTypeRowEditMode.NONE;

		public IntentionTypeRow(CIntentionType type)
		{
			this.type = type;
			label = new PText(type.getName());
			label.setConstrainWidthToTextWidth(true);
			label.setConstrainHeightToTextHeight(true);
			label.setFont(label.getFont().deriveFont(20f));

			addChild(label);
			addChild(editButton);

			editButton.setVisible(false);
		}

		void tap(Point point)
		{
			if (editButton.getVisible() && editButton.getBounds().contains(point))
			{
				switch (editMode)
				{
					case RENAME:
					{
						IntentionTypeNameDialog.Action action = IntentionTypeNameDialog.getInstance().queryUserForName(type);
						if (action == IntentionTypeNameDialog.Action.OK)
						{
							IntentionCanvasController.getInstance().renameIntentionType(type.getId(), IntentionTypeNameDialog.getInstance().getText());
						}
					}
						break;
					case SET_COLOR:
					{
						ColorPaletteDialog.Action action = ColorPaletteDialog.getInstance().queryUserForColor(type);
						if (action == ColorPaletteDialog.Action.OK)
						{
							IntentionCanvasController.getInstance().setIntentionTypeColorIndex(type.getId(), ColorPaletteDialog.getInstance().getColorIndex());
						}
					}
						break;
					case REMOVE:
						int count = CIntentionCellController.getInstance().countIntentionTypeUsage(type.getId());
						if (count > 0)
						{
							int userOption = JOptionPane.showConfirmDialog(CalicoDataStore.calicoObj, "<html>The intention tag '" + type.getName()
									+ "' is currently assigned to " + count + " whiteboards.<br>Are you sure you want to delete it?</html>",
									"Warning - intention tag in use", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

							if (userOption != JOptionPane.YES_OPTION)
							{
								break;
							}
						}

						IntentionCanvasController.getInstance().removeIntentionType(type.getId());
						break;
				}

				panel.activateIntentionRowEditMode(IntentionTypeRowEditMode.NONE);
			}
			else
			{
				CIntentionCellController.getInstance().toggleCellIntentionType(CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).getId(),
						type.getId(), !selected, false);
			}
		}

		void activateEditMode(IntentionTypeRowEditMode mode)
		{
			if (editMode == mode)
			{
				mode = IntentionTypeRowEditMode.NONE;
			}

			this.editMode = mode;

			if (mode == IntentionTypeRowEditMode.NONE)
			{
				editButton.setVisible(false);
			}
			else
			{
				editButton.setImage(mode.image);
				editButton.setVisible(true);
			}
		}

		double getMaxWidth()
		{
			return label.getBounds().width + editButton.getBounds().width + (PANEL_COMPONENT_INSET * 3);
		}

		void setSelected(boolean b)
		{
			selected = b;

			repaint();
		}

		@Override
		protected void layoutChildren()
		{
			PBounds rowBounds = getBounds();
			PBounds labelBounds = label.getBounds();
			PBounds buttonBounds = editButton.getBounds();

			label.setBounds(rowBounds.x + PANEL_COMPONENT_INSET, rowBounds.y + ROW_TEXT_INSET, labelBounds.width, ROW_HEIGHT - (2 * ROW_TEXT_INSET));
			editButton.setBounds((rowBounds.x + rowBounds.width) - (buttonBounds.width + PANEL_COMPONENT_INSET), rowBounds.y
					+ ((rowBounds.height - buttonBounds.height) / 2.0), buttonBounds.width, buttonBounds.height);
		}

		@Override
		protected void paint(PPaintContext paintContext)
		{
			if (selected)
			{
				PBounds bounds = getBounds();
				Graphics2D g = paintContext.getGraphics();
				Color c = g.getColor();
				g.setColor(type.getColor());
				g.fillRect((int) bounds.x, (int) bounds.y, (int) bounds.width, (int) bounds.height);
				g.setColor(c);
			}

			super.paint(paintContext);
		}
	}

	private class MetaButton extends PComposite
	{
		private final PImage icon;

		public MetaButton(Image image)
		{
			icon = new PImage(image);
			addChild(icon);
		}

		@Override
		protected void layoutChildren()
		{
			PBounds bounds = getBounds();
			icon.centerBoundsOnPoint(bounds.x + (bounds.width / 2.0), bounds.y + (bounds.height / 2.0));
		}
	}

	private class MetaRow extends PComposite
	{
		private final MetaButton addButton = new MetaButton(addButtonImage);
		private final MetaButton removeButton = new MetaButton(removeButtonImage);
		private final MetaButton editButton = new MetaButton(editButtonImage);
		private final MetaButton colorButton = new MetaButton(paletteButtonImage);

		public MetaRow()
		{
			addChild(addButton);
			addChild(removeButton);
			addChild(editButton);
			addChild(colorButton);
		}

		void tap(Point point)
		{
			if (point.x < removeButton.getBoundsReference().x)
			{
				IntentionTypeNameDialog.Action action = IntentionTypeNameDialog.getInstance().queryUserForName(null);
				if (action == IntentionTypeNameDialog.Action.OK)
				{
					IntentionCanvasController.getInstance().addIntentionType(IntentionTypeNameDialog.getInstance().getText());
				}
			}
			else if (point.x < editButton.getBoundsReference().x)
			{
				panel.activateIntentionRowEditMode(IntentionTypeRowEditMode.REMOVE);
			}
			else if (point.x < colorButton.getBoundsReference().x)
			{
				panel.activateIntentionRowEditMode(IntentionTypeRowEditMode.RENAME);
			}
			else if (point.x < (colorButton.getBoundsReference().x + colorButton.getBoundsReference().width))
			{
				panel.activateIntentionRowEditMode(IntentionTypeRowEditMode.SET_COLOR);
			}
		}

		@Override
		protected void layoutChildren()
		{
			PBounds rowBounds = getBounds();
			double buttonWidth = (rowBounds.getBounds().width / 4.0);

			double x = rowBounds.x;
			addButton.setBounds(x, rowBounds.y, buttonWidth, ROW_HEIGHT);
			removeButton.setBounds(x += buttonWidth, rowBounds.y, buttonWidth, ROW_HEIGHT);
			editButton.setBounds(x += buttonWidth, rowBounds.y, buttonWidth, ROW_HEIGHT);
			colorButton.setBounds(x += buttonWidth, rowBounds.y, buttonWidth, ROW_HEIGHT);
		}
	}

	private class PanelNode extends PComposite
	{
		private final TitleRow titleRow = new TitleRow();
		private final List<IntentionTypeRow> typeRows = new ArrayList<IntentionTypeRow>();
		private final MetaRow metaRow = new MetaRow();

		private PPath border;

		public PanelNode()
		{
			addChild(titleRow);
			addChild(metaRow);
		}

		void tap(Point point)
		{
			if (titleRow.getBoundsReference().contains(point))
			{
				titleRow.tap(point);
			}
			else if (metaRow.getBoundsReference().contains(point))
			{
				metaRow.tap(point);
			}
			else
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
		}

		void activateIntentionRowEditMode(IntentionTypeRowEditMode mode)
		{
			for (IntentionTypeRow row : typeRows)
			{
				row.activateEditMode(mode);
			}
		}

		double calculateWidth()
		{
			double width = titleRow.getMaxWidth();
			for (IntentionTypeRow row : typeRows)
			{
				double rowWidth = row.getMaxWidth();
				if (rowWidth > width)
				{
					width = rowWidth;
				}
			}
			return width;
		}

		double calculateHeight()
		{
			return (typeRows.size() + 2) * ROW_HEIGHT;
		}

		void updateIntentionTypes()
		{
			for (IntentionTypeRow row : typeRows)
			{
				removeChild(row);
			}
			typeRows.clear();

			for (CIntentionType type : IntentionCanvasController.getInstance().getActiveIntentionTypes())
			{
				IntentionTypeRow row = new IntentionTypeRow(type);
				addChild(row);
				typeRows.add(row);
			}

			if (border != null)
			{
				removeChild(border);
			}
			border = new PPath(new Rectangle2D.Double(0, 0, calculateWidth(), calculateHeight()));
			border.setStrokePaint(Color.black);
			border.setStroke(new BasicStroke(1f));
			addChild(border);
		}

		void refresh()
		{
			if (canvas_uuid == 0L)
			{
				return;
			}

			titleRow.refresh();

			CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid);
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
			titleRow.setBounds(bounds.x, y, bounds.width, ROW_HEIGHT);
			for (IntentionTypeRow row : typeRows)
			{
				row.setBounds(bounds.x, y += ROW_HEIGHT, bounds.width, ROW_HEIGHT);
			}
			metaRow.setBounds(bounds.x, y += ROW_HEIGHT, bounds.width, ROW_HEIGHT);

			border.setBounds(bounds);
		}
	}

	private enum InputState
	{
		IDLE,
		PRESSED
	}

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
					panel.tap(event.getPoint());
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
}
