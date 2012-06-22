package calico.plugins.iip.components.canvas;

import java.awt.Color;
import java.awt.Point;

import javax.swing.SwingUtilities;

import calico.Calico;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.IntentionPanelLayout;
import calico.plugins.iip.components.canvas.CanvasTitleDialog.Action;
import calico.plugins.iip.controllers.CIntentionCellController;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CanvasTitlePanel implements StickyItem
{
	public static CanvasTitlePanel getInstance()
	{
		return INSTANCE;
	}

	private static CanvasTitlePanel INSTANCE = new CanvasTitlePanel();

	public static final double PANEL_COMPONENT_INSET = 5.0;

	public static final double ROW_HEIGHT = 30.0;
	public static final double ROW_TEXT_INSET = 1.0;

	private final PanelNode panel;

	private final long uuid;
	private long canvas_uuid;

	private IntentionPanelLayout layout;

	private boolean initialized = false;

	private CanvasTitlePanel()
	{
		uuid = Calico.uuid();
		this.canvas_uuid = 0L;

		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());

		panel = new PanelNode();

		panel.setPaint(Color.white);
		CalicoInputManager.registerStickyItem(this);

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

	private void updatePanelBounds()
	{
		double width = panel.calculateWidth();
		double height = panel.calculateHeight();
		layout.updateBounds(panel, width, height);

		panel.repaint();
	}

	public void setLayout(IntentionPanelLayout layout)
	{
		this.layout = layout;
	}

	private class PanelNode extends PComposite
	{
		private final PText text = new PText();

		public PanelNode()
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

		double calculateWidth()
		{
			return text.getBounds().width + (2 * PANEL_COMPONENT_INSET);
		}

		double calculateHeight()
		{
			return text.getBounds().height;
		}

		void refresh()
		{
			if (canvas_uuid == 0L)
			{
				return;
			}
			
			text.setText(CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).getTitle());
		}

		@Override
		protected void layoutChildren()
		{
			if (!initialized)
			{
				return;
			}

			PBounds bounds = getBounds();

			text.recomputeLayout();
			PBounds textBounds = text.getBounds();
			text.setBounds(bounds.x + PANEL_COMPONENT_INSET, bounds.y + ROW_TEXT_INSET, textBounds.width, textBounds.getHeight());
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
