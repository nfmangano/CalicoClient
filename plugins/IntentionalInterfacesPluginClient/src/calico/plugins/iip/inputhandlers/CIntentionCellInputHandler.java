package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import calico.components.piemenu.PieMenu;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.piemenu.iip.CreateNewAlternativeLinkButton;
import calico.plugins.iip.components.piemenu.iip.CreateNewIdeaLinkButton;
import calico.plugins.iip.components.piemenu.iip.CreateNewPerspectiveLinkButton;
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

	private final CreateNewIdeaLinkButton newIdeaButton = new CreateNewIdeaLinkButton();
	private final CreateNewAlternativeLinkButton newAlternativeButton = new CreateNewAlternativeLinkButton();
	private final CreateNewPerspectiveLinkButton newPerspectiveButton = new CreateNewPerspectiveLinkButton();

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

	private void moveCurrentCell(Point destination, boolean local)
	{
		double xMouseDelta = (destination.x - mouseDragAnchor.x) / IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
		double yMouseDelta = (destination.y - mouseDragAnchor.y) / IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
		CIntentionCellController.getInstance().moveCell(currentCellId, cellDragAnchor.getX() + xMouseDelta, cellDragAnchor.getY() + yMouseDelta, local);
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
		if (state == State.DRAG)
		{
			moveCurrentCell(event.getGlobalPoint(), false);
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
						PieMenu.displayPieMenu(point, newIdeaButton, newAlternativeButton, newPerspectiveButton);
					}
				}
			}
		}
	}
}
