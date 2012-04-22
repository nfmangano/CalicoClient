package calico.plugins.iip.components.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.swing.JTextField;

import calico.Calico;
import calico.components.CCanvas;
import calico.components.piemenu.PieMenu;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.CIntentionType;
import calico.plugins.iip.components.IntentionPanelLayout;
import calico.plugins.iip.components.piemenu.DeleteLinkButton;
import calico.plugins.iip.components.piemenu.PieMenuTimerTask;
import calico.plugins.iip.components.piemenu.SetLinkLabelButton;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PComposite;
import edu.umd.cs.piccolox.pswing.PSwing;

public class CanvasTagPanel implements StickyItem
{
	public static CanvasTagPanel getInstance()
	{
		return INSTANCE;
	}

	private static CanvasTagPanel INSTANCE = new CanvasTagPanel();

	public static final double PANEL_INSET_X = 100.0;
	public static final double PANEL_INSET_Y = 50.0;
	public static final double PANEL_COMPONENT_INSET = 3.0;

	public static final double ROW_HEIGHT = 20.0;
	public static final double ROW_TEXT_INSET = 1.0;

	private final PanelNode panel;

	private final Image addButtonImage;
	private final Image removeButtonImage;
	private final Image paletteButtonImage;

	private final long uuid;
	private long canvas_uuid;

	private boolean visible;
	private IntentionPanelLayout layout;
	private final List<CCanvasLinkToken> tokens = new ArrayList<CCanvasLinkToken>();

	private final DeleteLinkButton deleteLinkButton = new DeleteLinkButton();
	private final SetLinkLabelButton setLinkLabelButton = new SetLinkLabelButton();

	private boolean initialized = false;

	private CanvasTagPanel()
	{
		uuid = Calico.uuid();
		this.canvas_uuid = 0L;

		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());

		addButtonImage = CalicoIconManager.getIconImage("intention.add-button");
		removeButtonImage = CalicoIconManager.getIconImage("intention.remove-button");
		paletteButtonImage = CalicoIconManager.getIconImage("intention.palette-button");
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

		panel.refresh();
		panel.setVisible(true);
		panel.repaint();
	}

	public void updateIntentionTypes()
	{
		panel.updateIntentionTypes();
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

	private class TitleRow extends PComposite implements KeyListener
	{
		private final PSwing textFieldWrapper;
		private final JTextField textField = new JTextField();
		private final PText text;

		public TitleRow()
		{
			textFieldWrapper = new PSwing(textField);
			text = new PText();
			text.setConstrainWidthToTextWidth(true);
			text.setConstrainHeightToTextHeight(true);

			cancelEdit();
			textField.addKeyListener(this);

			addChild(textFieldWrapper);
			addChild(text);
		}
		
		void tap(Point point)
		{
			if (text.getVisible())
			{
				edit();
			}
		}

		double getMaxWidth()
		{
			return Math.max((double) textField.getSize().width, text.getBounds().width) + (2 * PANEL_COMPONENT_INSET);
		}

		void refresh()
		{
			text.setText(CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).getTitle());
		}

		void edit()
		{
			text.setVisible(false);

			textField.setText(text.getText());
			textField.setEnabled(true);
			textFieldWrapper.setVisible(true);
		}

		private void acceptNewTitle()
		{
			text.setText(textField.getText());

			cancelEdit();
		}

		private void cancelEdit()
		{
			text.setVisible(true);
			textFieldWrapper.setVisible(false);
			textField.setEnabled(false);
		}

		@Override
		protected void layoutChildren()
		{
			PBounds bounds = getBounds();

			text.recomputeLayout();
			PBounds textBounds = text.getBounds();
			text.setBounds(bounds.x + PANEL_COMPONENT_INSET, bounds.y + ROW_TEXT_INSET, textBounds.width, textBounds.getHeight());

			Dimension textFieldSize = textField.getPreferredSize();
			textField.setBounds(0, 0, textFieldSize.width, textFieldSize.height);
			textFieldWrapper.setBounds(bounds.x + PANEL_COMPONENT_INSET, (ROW_HEIGHT - textFieldSize.height) / 2.0, textFieldSize.width, textFieldSize.height);
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_ENTER:
					acceptNewTitle();
					break;
				case KeyEvent.VK_ESCAPE:
					cancelEdit();
					break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
		}
	}

	private class IntentionTypeRow extends PComposite
	{
		private final CIntentionType type;
		private final PText label;
		private final PImage removeButton = new PImage(removeButtonImage);

		private boolean selected = false;

		public IntentionTypeRow(CIntentionType type)
		{
			this.type = type;
			label = new PText(type.getName());
			label.setConstrainWidthToTextWidth(true);
			label.setConstrainHeightToTextHeight(true);

			addChild(label);
			addChild(removeButton);

			removeButton.setVisible(false);
		}
		
		void tap(Point point)
		{
			if (removeButton.getVisible() && removeButton.getBounds().contains(point))
			{
				System.out.println("Remove " + type.getName());
				panel.toggleIntentionTypesRemovable();
			}
			else
			{
				setSelected(!selected);
			}
		}

		void toggleRemovable()
		{
			removeButton.setVisible(!removeButton.getVisible());
		}

		double getMaxWidth()
		{
			return label.getBounds().width + removeButton.getBounds().width + (PANEL_COMPONENT_INSET * 3);
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
			PBounds buttonBounds = removeButton.getBounds();

			label.setBounds(rowBounds.x + PANEL_COMPONENT_INSET, rowBounds.y + ROW_TEXT_INSET, labelBounds.width, ROW_HEIGHT - (2 * ROW_TEXT_INSET));
			removeButton.setBounds((rowBounds.x + rowBounds.width) - (buttonBounds.width + PANEL_COMPONENT_INSET), rowBounds.y
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
		private final MetaButton colorButton = new MetaButton(paletteButtonImage);

		public MetaRow()
		{
			addChild(addButton);
			addChild(removeButton);
			addChild(colorButton);
		}

		void tap(Point point)
		{
			if (point.x < (addButton.getBoundsReference().x + addButton.getBoundsReference().width))
			{
				System.out.println("add intention type");
			}
			else if (point.x < (removeButton.getBoundsReference().x + removeButton.getBoundsReference().width))
			{
				panel.toggleIntentionTypesRemovable();
			}
			else if (point.x < (colorButton.getBoundsReference().x + colorButton.getBoundsReference().width))
			{
				System.out.println("color intention type");
			}
		}

		@Override
		protected void layoutChildren()
		{
			PBounds rowBounds = getBounds();
			double buttonWidth = (rowBounds.getBounds().width / 3.0);

			double x = rowBounds.x;
			addButton.setBounds(x, rowBounds.y, buttonWidth, ROW_HEIGHT);
			removeButton.setBounds(x += buttonWidth, rowBounds.y, buttonWidth, ROW_HEIGHT);
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
			updateIntentionTypes();
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
		
		void toggleIntentionTypesRemovable()
		{
			for (IntentionTypeRow row : typeRows)
			{
				row.toggleRemovable();
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
			titleRow.refresh();

			CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid);
			for (IntentionTypeRow row : typeRows)
			{
				row.setSelected(cell.hasIntentionType(row.type));
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
		PRESSED,
		PIE
	}

	private class InputHandler extends CalicoAbstractInputHandler
	{
		private final Object stateLock = new Object();

		private final long tapDuration = 500L;

		private InputState state = InputState.IDLE;
		private long pressTime = 0L;

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
			synchronized (stateLock)
			{
				state = InputState.IDLE;
			}

			pressTime = 0L;
		}

		@Override
		public void actionPressed(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				state = InputState.PRESSED;

				pressTime = System.currentTimeMillis();
			}
		}
	}
}
