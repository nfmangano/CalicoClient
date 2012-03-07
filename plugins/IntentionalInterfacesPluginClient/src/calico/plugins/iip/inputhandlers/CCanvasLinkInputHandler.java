package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Timer;

import calico.components.menus.ContextMenu;
import calico.components.piemenu.PieMenu;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLink.LinkType;
import calico.plugins.iip.components.CCanvasLinkArrow;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.piemenu.DeleteLinkButton;
import calico.plugins.iip.components.piemenu.PieMenuTimerTask;
import calico.plugins.iip.components.piemenu.SetLinkLabelButton;
import calico.plugins.iip.components.piemenu.iip.CreateIntentionArrowPhase;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.IntentionGraphController;

public class CCanvasLinkInputHandler extends CalicoAbstractInputHandler implements ContextMenu.Listener
{
	public static CCanvasLinkInputHandler getInstance()
	{
		return INSTANCE;
	}

	private static final CCanvasLinkInputHandler INSTANCE = new CCanvasLinkInputHandler();

	private static final double MOVE_THRESHOLD = 5.0;

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

	private final DeleteLinkButton deleteLinkButton = new DeleteLinkButton();
	private final SetLinkLabelButton setLinkLabelButton = new SetLinkLabelButton();

	private boolean canMoveCurrentLink;
	private boolean isNearestSideA;

	private CCanvasLinkInputHandler()
	{
		PieMenu.addListener(this);
	}

	public void setCurrentLinkId(long currentLinkId, Point point)
	{
		this.currentLinkId = currentLinkId;

		CCanvasLink link = CCanvasLinkController.getInstance().getLinkById(currentLinkId);
		isNearestSideA = CCanvasLinkController.getInstance().isNearestSideA(currentLinkId, point);
		canMoveCurrentLink = (link.getLinkType() != LinkType.DESIGN_INSIDE) || !isNearestSideA;

		deleteLinkButton.setContext(link);
		setLinkLabelButton.setContext(link);

		IntentionGraphController.getInstance().getArrowByLinkId(currentLinkId).setHighlighted(true);
	}

	public long getActiveLink()
	{
		if (state == State.IDLE)
		{
			return -1L;
		}

		return currentLinkId;
	}

	@Override
	public void actionDragged(InputEventInfo event)
	{
		synchronized (stateLock)
		{
			if (state == State.ACTIVATED)
			{
				state = State.DRAG;
				CreateIntentionArrowPhase.getInstance().startMove(CCanvasLinkController.getInstance().getLinkById(currentLinkId), isNearestSideA);
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
		}
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		state = State.IDLE;
		IntentionGraphController.getInstance().getArrowByLinkId(currentLinkId).setHighlighted(false);
	}

	@Override
	public void menuCleared(ContextMenu menu)
	{
		if ((state == State.PIE) && (menu == ContextMenu.PIE_MENU))
		{
			state = State.IDLE;

			CCanvasLinkArrow arrow = IntentionGraphController.getInstance().getArrowByLinkId(currentLinkId);
			if (arrow != null)
			{
				arrow.setHighlighted(false);
			}
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
				if (state == State.PIE)
				{
					PieMenu.displayPieMenu(point, setLinkLabelButton, deleteLinkButton);
				}
			}
		}
	}
}
