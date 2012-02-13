package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CIntentionCellController;

public class CIntentionCellInputHandler extends CalicoAbstractInputHandler
{
	public static CIntentionCellInputHandler getInstance()
	{
		return INSTANCE;
	}

	private static final CIntentionCellInputHandler INSTANCE = new CIntentionCellInputHandler();

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

	public void setCurrentCellId(long currentCellId)
	{
		this.currentCellId = currentCellId;
	}

	public long getActiveCell()
	{
		if (state == State.IDLE)
		{
			return -1L;
		}

		return currentCellId;
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
					double xMouseDelta = event.getGlobalPoint().x - mouseDragAnchor.x;
					double yMouseDelta = event.getGlobalPoint().y - mouseDragAnchor.y;
					CIntentionCellController.getInstance().getCellById(currentCellId)
							.setLocation(cellDragAnchor.getX() + xMouseDelta, cellDragAnchor.getY() + yMouseDelta);
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

			pieMenuTimer.start(event.getGlobalPoint());

			mouseDragAnchor = event.getGlobalPoint();
			cellDragAnchor = CIntentionCellController.getInstance().getCellById(currentCellId).getLocation();

			// CalicoInputManager.rerouteEvent(this.canvas_uid, e); ???
		}
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		if (state == State.DRAG)
		{
			double xMouseDelta = event.getGlobalPoint().x - mouseDragAnchor.x;
			double yMouseDelta = event.getGlobalPoint().y - mouseDragAnchor.y;
			CIntentionCellController.getInstance().moveCell(CIntentionCellController.getInstance().getCellById(currentCellId).getCanvasId(),
					cellDragAnchor.getX() + xMouseDelta, cellDragAnchor.getY() + yMouseDelta);
		}

		state = State.IDLE;
	}

	private class PieMenuTimer extends Timer
	{
		private Point point;

		void start(Point point)
		{
			this.point = point;

			schedule(new Task(), 200L);
		}

		private class Task extends TimerTask
		{
			@Override
			public void run()
			{
				synchronized (stateLock)
				{
					if (state == State.ACTIVATED)
					{
						state = State.PIE;
						PieMenu.displayPieMenu(point, new PieMenuButton[0]);
					}
				}
			}
		}
	}
}
