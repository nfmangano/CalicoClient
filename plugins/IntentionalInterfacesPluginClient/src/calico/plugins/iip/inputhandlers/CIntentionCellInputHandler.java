package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Timer;

import calico.components.bubblemenu.BubbleMenu;
import calico.components.menus.ContextMenu;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.piemenu.PieMenuTimerTask;
import calico.plugins.iip.components.piemenu.iip.CreateLinkButton;
import calico.plugins.iip.components.piemenu.iip.DeleteCanvasButton;
import calico.plugins.iip.components.piemenu.iip.ZoomToClusterButton;
import calico.plugins.iip.controllers.CIntentionCellController;
import edu.umd.cs.piccolo.util.PBounds;

public class CIntentionCellInputHandler extends CalicoAbstractInputHandler implements ContextMenu.Listener
{
	public static CIntentionCellInputHandler getInstance()
	{
		return INSTANCE;
	}

	private static final CIntentionCellInputHandler INSTANCE = new CIntentionCellInputHandler();

	private static final double DRAG_THRESHOLD = 20.0;
	private static final int BUBBLE_MENU_TYPE_ID = BubbleMenu.registerType(new BubbleMenuComponentType());

	private enum State
	{
		IDLE,
		ACTIVATED,
		DRAG,
		MENU;
	}

	private long currentCellId;

	private State state = State.IDLE;
	private final Object stateLock = new Object();

	private Point mouseDragAnchor;
	private Point2D cellDragAnchor;

	private final BubbleMenuTimer bubbleMenuTimer = new BubbleMenuTimer();

	private final DeleteCanvasButton deleteCanvasButton = new DeleteCanvasButton();
	private final CreateLinkButton linkButton = new CreateLinkButton();
	private final ZoomToClusterButton zoomToClusterButton = new ZoomToClusterButton();

	private CIntentionCellInputHandler()
	{
		BubbleMenu.addListener(this);
	}

	public void setCurrentCellId(long currentCellId)
	{
		this.currentCellId = currentCellId;

		CIntentionCellController.getInstance().getCellById(currentCellId).setHighlighted(true);
	}

	public long getActiveCell()
	{
		if (state == State.IDLE)
		{
			return -1L;
		}

		return currentCellId;
	}

	private void moveCurrentCell(Point destination, boolean local)
	{
		double xMouseDelta = (destination.x - mouseDragAnchor.x) / IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
		double yMouseDelta = (destination.y - mouseDragAnchor.y) / IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();

		if (local)
		{
			CIntentionCellController.getInstance().moveCellLocal(currentCellId, cellDragAnchor.getX() + xMouseDelta, cellDragAnchor.getY() + yMouseDelta);
		}
		else
		{
			CIntentionCellController.getInstance().moveCell(currentCellId, cellDragAnchor.getX() + xMouseDelta, cellDragAnchor.getY() + yMouseDelta);
		}
	}

	@Override
	public void actionDragged(InputEventInfo event)
	{
		synchronized (stateLock)
		{
			switch (state)
			{
				case ACTIVATED:
					if (event.getGlobalPoint().distance(mouseDragAnchor) >= DRAG_THRESHOLD)
					{
						state = State.DRAG;
					}
					else
					{
						break;
					}
				case DRAG:
					moveCurrentCell(event.getGlobalPoint(), true);
			}
		}
	}

	@Override
	public void actionPressed(InputEventInfo event)
	{
		if (event.isLeftButtonPressed())
		{
			synchronized (stateLock)
			{
				state = State.ACTIVATED;
			}

			mouseDragAnchor = event.getGlobalPoint();
			cellDragAnchor = CIntentionCellController.getInstance().getCellById(currentCellId).getLocation();

			Point2D point = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS).globalToLocal(event.getGlobalPoint());
			bubbleMenuTimer.start(new Point((int) point.getX(), (int) point.getY()));
		}
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		CIntentionCell cell = CIntentionCellController.getInstance().getCellById(currentCellId);

		synchronized (stateLock)
		{
			switch (state)
			{
				case DRAG:
					moveCurrentCell(event.getGlobalPoint(), false);
					break;
				case ACTIVATED:
					if (event.getGlobalPoint().distance(mouseDragAnchor) < DRAG_THRESHOLD)
					{
						CCanvasController.loadCanvas(cell.getCanvasId());
					}
					break;
			}
			
			if (state != State.MENU)
			{
				state = State.IDLE;
				cell.setHighlighted(false);
			}
		}
	}

	@Override
	public void menuCleared(ContextMenu menu)
	{
		if ((state == State.MENU) && (menu == ContextMenu.BUBBLE_MENU))
		{
			state = State.IDLE;
			CIntentionCellController.getInstance().getCellById(currentCellId).setHighlighted(false);
		}
	}

	@Override
	public void menuDisplayed(ContextMenu menu)
	{
	}

	private class BubbleMenuTimer extends Timer
	{
		private Point point;

		void start(Point point)
		{
			this.point = point;

			schedule(new Task(), 200L);
		}

		private class Task extends PieMenuTimerTask
		{
			@Override
			public void run()
			{
				synchronized (stateLock)
				{
					if (state == State.ACTIVATED)
					{
						startAnimation(IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS), point);
					}
				}
			}

			@Override
			protected void animationCompleted()
			{
				synchronized (stateLock)
				{
					if (state == State.ACTIVATED)
					{
						state = State.MENU;

						if (CCanvasController.canvasdb.size() > 1)
						{
							BubbleMenu.displayBubbleMenu(currentCellId, true, BUBBLE_MENU_TYPE_ID, deleteCanvasButton, linkButton, zoomToClusterButton);
						}
						else
						{
							BubbleMenu.displayBubbleMenu(currentCellId, true, BUBBLE_MENU_TYPE_ID, linkButton, zoomToClusterButton);
						}
					}
				}
			}
		}
	}

	private static class BubbleMenuComponentType implements BubbleMenu.ComponentType
	{
		@Override
		public PBounds getBounds(long uuid)
		{
			return CIntentionCellController.getInstance().getCellById(uuid).getGlobalBounds();
		}

		@Override
		public void highlight(boolean b, long uuid)
		{
			CIntentionCellController.getInstance().getCellById(uuid).setHighlighted(b);
		}

		@Override
		public int getButtonPosition(String buttonClassname)
		{
			if (buttonClassname.equals(DeleteCanvasButton.class.getName()))
			{
				return 1;
			}
			if (buttonClassname.equals(CreateLinkButton.class.getName()))
			{
				return 2;
			}
			if (buttonClassname.equals(ZoomToClusterButton.class.getName()))
			{
				return 3;
			}

			return 0;
		}
	}
}
