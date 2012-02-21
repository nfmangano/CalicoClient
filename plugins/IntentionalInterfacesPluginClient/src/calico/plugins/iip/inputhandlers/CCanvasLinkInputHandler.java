package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import calico.components.menus.ContextMenu;
import calico.components.piemenu.PieMenu;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.piemenu.DeleteLinkButton;
import calico.plugins.iip.components.piemenu.iip.CreateNewAlternativeLinkButton;
import calico.plugins.iip.components.piemenu.iip.CreateNewIdeaLinkButton;
import calico.plugins.iip.components.piemenu.iip.CreateNewPerspectiveLinkButton;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;

public class CCanvasLinkInputHandler extends CalicoAbstractInputHandler implements ContextMenu.Listener
{
	public static CCanvasLinkInputHandler getInstance()
	{
		return INSTANCE;
	}

	private static final CCanvasLinkInputHandler INSTANCE = new CCanvasLinkInputHandler();

	private enum State
	{
		IDLE,
		ACTIVATED,
		DRAG,
		PIE;
	}

	private long currentLinkId;

	private State state = State.IDLE;
	private final Object stateLock = new Object();

	private final PieMenuTimer pieMenuTimer = new PieMenuTimer();

	private Point mouseDragAnchor;
	// private Point2D cellDragAnchor;

	private final DeleteLinkButton deleteLinkButton = new DeleteLinkButton();

	private CCanvasLinkInputHandler()
	{
		PieMenu.addListener(this);
	}

	public void setCurrentLinkId(long currentLinkId)
	{
		this.currentLinkId = currentLinkId;

		deleteLinkButton.setContext(CCanvasLinkController.getInstance().getLinkById(currentLinkId));
	}

	public long getActiveLink()
	{
		if (state == State.IDLE)
		{
			return -1L;
		}

		return currentLinkId;
	}

	private void moveCurrentLink(Point destination, boolean local)
	{
		double xMouseDelta = (destination.x - mouseDragAnchor.x) / IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
		double yMouseDelta = (destination.y - mouseDragAnchor.y) / IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
		// something like this:
		// CCanvasLinkController.getInstance().moveLink(currentLinkId, cellDragAnchor.getX() + xMouseDelta,
		// cellDragAnchor.getY() + yMouseDelta, local);
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
					moveCurrentLink(event.getGlobalPoint(), true);
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

			// don't know how to anchor the link just now...
			// cellDragAnchor = CIntentionCellController.getInstance().getCellById(currentLinkId).getLocation();

			// CalicoInputManager.rerouteEvent(this.canvas_uid, e); ???
		}
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		if (state == State.DRAG)
		{
			moveCurrentLink(event.getGlobalPoint(), false);
		}

		state = State.IDLE;
	}

	@Override
	public void menuCleared(ContextMenu menu)
	{
		if ((state == State.PIE) && (menu == ContextMenu.PIE_MENU))
		{
			state = State.IDLE;
		}
	}

	@Override
	public void menuDisplayed(ContextMenu menu, Point2D position)
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
						PieMenu.displayPieMenu(point, deleteLinkButton);
					}
				}
			}
		}
	}
}
