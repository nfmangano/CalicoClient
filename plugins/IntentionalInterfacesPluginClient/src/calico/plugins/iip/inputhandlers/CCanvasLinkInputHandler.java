package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Timer;

import calico.components.menus.ContextMenu;
import calico.components.piemenu.PieMenu;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkArrow;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.piemenu.DeleteLinkButton;
import calico.plugins.iip.components.piemenu.PieMenuTimerTask;
import calico.plugins.iip.components.piemenu.SetLinkLabelButton;
import calico.plugins.iip.components.piemenu.iip.CreateIntentionArrowPhase;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionGraphController;

/**
 * Custom <code>CalicoInputManager</code>handler for events related to arrows in the Intention View. The main Calico
 * event handling mechanism will determine whether input relates to an arrow by calling
 * <code>IntentionalInterfacesPerspective.getEventTarget()</code>. When that method returns an arrow, the associated
 * input event will be sent here.
 * 
 * The only supported operation is press&hold on an arrow to obtain a pie menu for it. An arrow is highlighted on
 * press, and the menu appears after the timer's duration of 200ms expires.
 * 
 * Moving arrows by dragging either endpoint has been supported in past versions.
 * 
 * @author Byron Hawkins
 */
public class CCanvasLinkInputHandler extends CalicoAbstractInputHandler implements ContextMenu.Listener
{
	public static CCanvasLinkInputHandler getInstance()
	{
		return INSTANCE;
	}

	private static final CCanvasLinkInputHandler INSTANCE = new CCanvasLinkInputHandler();

	private static final double MOVE_THRESHOLD = 10.0;

	private enum State
	{
		IDLE,
		ACTIVATED,
		DRAG,
		PIE;
	}

	/**
	 * Identifies the currently pressed link.
	 */
	private long currentLinkId;

	/**
	 * State token, protected voluntarily under <code>stateLock</code>.
	 */
	private State state = State.IDLE;
	/**
	 * Voluntary lock for <code>state</code>.
	 */
	private final Object stateLock = new Object();

	/**
	 * Governs the press&hold delay for the pie menu.
	 */
	private final PieMenuTimer pieMenuTimer = new PieMenuTimer();

	/**
	 * Delete button in the pie menu for arrows.
	 */
	private final DeleteLinkButton deleteLinkButton = new DeleteLinkButton();
	/**
	 * Button for setting link labels, which appears in the pie menu for arrows.
	 */
	private final SetLinkLabelButton setLinkLabelButton = new SetLinkLabelButton();

	/**
	 * Identifies the pixel position of the mouse at the time drag was initiated. Obsolete.
	 */
	private Point mouseDragAnchor;

	/**
	 * State flag indicating whether the currently active press occurred nearest the head or tail of the arrow. This
	 * flag has no meaning at times when no arrow input sequence is in progress.
	 */
	private boolean isNearestSideA;

	private CCanvasLinkInputHandler()
	{
		PieMenu.addListener(this);
	}

	/**
	 * Activate the input sequence for the arrow representing <code>currentLinkId</code>, with initial mouse contact at
	 * <code>point</code>.
	 */
	public void setCurrentLinkId(long currentLinkId, Point point)
	{
		this.currentLinkId = currentLinkId;

		CCanvasLink link = CCanvasLinkController.getInstance().getLinkById(currentLinkId);
		isNearestSideA = CCanvasLinkController.getInstance().isNearestSideA(currentLinkId, point);

		deleteLinkButton.setContext(link);
		setLinkLabelButton.setContext(link);

		IntentionGraphController.getInstance().getArrowByLinkId(currentLinkId).setHighlighted(true);
	}

	/**
	 * Get the link that the current input sequence is operating on, <code>-1L</code> if no arrow input sequence is in
	 * progress.
	 */
	public long getActiveLink()
	{
		if (state == State.IDLE)
		{
			return -1L;
		}

		return currentLinkId;
	}

	@Override
	public void actionDragged(InputEventInfo ev)
	{
		// no moving arrows anymore
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

			Point2D point = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS).globalToLocal(event.getGlobalPoint());
			pieMenuTimer.start(new Point((int) point.getX(), (int) point.getY()));
		}
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		synchronized (stateLock)
		{
			state = State.IDLE;
		}
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

	/**
	 * Displays the pie menu after press is held for 200ms.
	 * 
	 * @author Byron Hawkins
	 */
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
					//getActiveLink;
					
					long anchorACanvas = CCanvasLinkController.getInstance().getLinkById(getActiveLink()).getAnchorA().getCanvasId();;
					boolean isAnchorACanvasRootCanvas = CIntentionCellController.getInstance().isRootCanvas(anchorACanvas);
					
					if (!isAnchorACanvasRootCanvas)
						PieMenu.displayPieMenu(point, setLinkLabelButton, deleteLinkButton);
					else
						PieMenu.displayPieMenu(point, setLinkLabelButton);
				}
			}
		}
	}
}
