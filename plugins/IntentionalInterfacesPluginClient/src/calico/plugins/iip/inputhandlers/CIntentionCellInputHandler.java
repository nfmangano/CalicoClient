package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Timer;

import calico.components.menus.ContextMenu;
import calico.components.piemenu.PieMenu;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.piemenu.PieMenuTimerTask;
import calico.plugins.iip.components.piemenu.iip.CreateNewAlternativeLinkButton;
import calico.plugins.iip.components.piemenu.iip.CreateNewPerspectiveLinkButton;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;

public class CIntentionCellInputHandler extends CalicoAbstractInputHandler implements ContextMenu.Listener
{
	public static CIntentionCellInputHandler getInstance()
	{
		return INSTANCE;
	}

	private static final CIntentionCellInputHandler INSTANCE = new CIntentionCellInputHandler();

	private static final double CLICK_DRIFT_LIMIT = 5.0;

	private enum State
	{
		IDLE,
		ACTIVATED,
		DRAG,
		PIE;
	}

	private long currentCellId;

	private State state = State.IDLE;
	private final Object stateLock = new Object();

	private final PieMenuTimer pieMenuTimer = new PieMenuTimer();

	private Point mouseDragAnchor;
	private Point2D cellDragAnchor;

	private final CreateNewAlternativeLinkButton newAlternativeButton = new CreateNewAlternativeLinkButton();
	private final CreateNewPerspectiveLinkButton newPerspectiveButton = new CreateNewPerspectiveLinkButton();

	private CIntentionCellInputHandler()
	{
		PieMenu.addListener(this);
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
					state = State.DRAG;
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

			Point2D point = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS).globalToLocal(event.getGlobalPoint());
			pieMenuTimer.start(new Point((int) point.getX(), (int) point.getY()));

			mouseDragAnchor = event.getGlobalPoint();
			cellDragAnchor = CIntentionCellController.getInstance().getCellById(currentCellId).getLocation();

			// CalicoInputManager.rerouteEvent(this.canvas_uid, e); ???
		}
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		CIntentionCellController.getInstance().getCellById(currentCellId).setHighlighted(false);

		switch (state)
		{
			case DRAG:
				moveCurrentCell(event.getGlobalPoint(), false);
				break;
			case ACTIVATED:
				if (event.getGlobalPoint().distance(mouseDragAnchor) < CLICK_DRIFT_LIMIT)
				{
					CCanvasController.loadCanvas(CIntentionCellController.getInstance().getCellById(currentCellId).getCanvasId());
				}
				break;
		}

		state = State.IDLE;
	}

	@Override
	public void menuCleared(ContextMenu menu)
	{
		if ((state == State.PIE) && (menu == ContextMenu.PIE_MENU))
		{
			state = State.IDLE;
			CIntentionCellController.getInstance().getCellById(currentCellId).setHighlighted(false);
		}
	}

	@Override
	public void menuDisplayed(ContextMenu menu)
	{
	}

	private class PieMenuTimer extends Timer
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
						state = State.PIE;
						startAnimation(IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS), point);
					}
				}
			}

			@Override
			protected void animationCompleted()
			{
				synchronized (stateLock)
				{
					if (state == State.PIE)
					{
						PieMenu.displayPieMenu(point, newAlternativeButton, newPerspectiveButton);
					}
				}
			}
		}
	}
}
