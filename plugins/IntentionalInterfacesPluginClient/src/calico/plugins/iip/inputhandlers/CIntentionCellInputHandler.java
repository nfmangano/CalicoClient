package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Timer;

import javax.swing.SwingUtilities;

import calico.CalicoDraw;
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
import calico.plugins.iip.components.piemenu.iip.SetCanvasTitleButton;
import calico.plugins.iip.components.piemenu.iip.ZoomToClusterButton;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionGraphController;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * Custom <code>CalicoInputManager</code>handler for events related to CICs in the Intention View. The main Calico event
 * handling mechanism will determine whether input relates to a CIC by calling
 * <code>IntentionalInterfacesPerspective.getEventTarget()</code>. When that method returns a CIC, the associated input
 * event will be sent here.
 * 
 * The current behavior is to enter a canvas on tap, show a bubble menu for the CIC on press&hold, and move the CIC when
 * dragged more than 20 pixels.
 * 
 * @author Byron Hawkins
 */
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

	/**
	 * Identifies the CIC which input is currently operating on, if any.
	 */
	private long currentCellId;

	/**
	 * State token, voluntarily protected under the <code>stateLock</code>.
	 */
	private State state = State.IDLE;
	/**
	 * Voluntary lock for the <code>state</code>.
	 */
	private final Object stateLock = new Object();

	/**
	 * Keeps the initial mouse position at the moment a drag was initiated. The value has no meaning when no drag is in
	 * progress.
	 */
	private Point mouseDragAnchor;
	/**
	 * Keeps the initial CIC position at the moment a drag was initiated. The value has no meaning when no drag is in
	 * progress.
	 */
	private Point2D cellDragAnchor;

	/**
	 * Time governing the display delay for the bubble menu.
	 */
	private final BubbleMenuTimer bubbleMenuTimer = new BubbleMenuTimer();

	/**
	 * Simple button to delete a canvas and its associated CIC.
	 */
	private final DeleteCanvasButton deleteCanvasButton = new DeleteCanvasButton();
	/**
	 * Button for initiating the arrow creation phase, which is governed by <code>CIntentionArrowPhase</code>.
	 */
	private final CreateLinkButton linkButton = new CreateLinkButton();
	/**
	 * Simple button to zoom and pan the Intention View such that the cluster containing the selected CIC fits neatly in
	 * the Intention View.
	 */
	private final ZoomToClusterButton zoomToClusterButton = new ZoomToClusterButton();
	/**
	 * Opens a dialog to set the name of the canvas
	 */
	private final SetCanvasTitleButton setCanvasTitleButton = new SetCanvasTitleButton();

	private CIntentionCellInputHandler()
	{
		BubbleMenu.addListener(this);
	}

	/**
	 * Initiate the input sequence on <code>currentCellId</code>. The sequence will terminate on input release.
	 */
	public void setCurrentCellId(long currentCellId)
	{
		this.currentCellId = currentCellId;

		CIntentionCellController.getInstance().getCellById(currentCellId).setHighlighted(true);
	}

	/**
	 * Get the CIC which is currently the subject of input, or <code>-1L</code> if no input sequence is presently active
	 * on a CIC.
	 */
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
						CIntentionCellController.getInstance().getCellById(currentCellId).moveToFront();
					}
					else
					{
						break;
					}
				case DRAG:
					CIntentionCell cell = CIntentionCellController.getInstance().getCellById(currentCellId);
					long potentialTargetCell = CIntentionCellController.getInstance().getCellAt(event.getPoint(), currentCellId);
					long targetCluster = IntentionGraph.getInstance().getClusterAt(event.getPoint());
					long originalRoot = CIntentionCellController.getInstance().getClusterRootCanvasId(cell.getCanvasId());
					if (potentialTargetCell > 0l
							&& potentialTargetCell != currentCellId
							&& !CIntentionCellController.getInstance().isParent(
									CIntentionCellController.getInstance().getCellById(potentialTargetCell).getCanvasId(), cell.getCanvasId()))
					{
						CIntentionCellController.getInstance().getCellById(currentCellId).setCellIcon(CIntentionCell.CellIconType.CREATE_LINK);
					}
					else if (CIntentionCellController.getInstance().getCIntentionCellParent(cell.getCanvasId()) != originalRoot
							&& IntentionGraph.getInstance().ringContainsPoint(originalRoot, event.getGlobalPoint(), 0))
					{
						CIntentionCellController.getInstance().getCellById(currentCellId).setCellIcon(CIntentionCell.CellIconType.DELETE_LINK);
					}
					else
					{
						CIntentionCellController.getInstance().getCellById(currentCellId).setCellIcon(CIntentionCell.CellIconType.NONE);
					}
					
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
			if (CIntentionCellController.getInstance().getCellById(currentCellId) == null)
				return;
			cellDragAnchor = CIntentionCellController.getInstance().getCellById(currentCellId).getLocation();

			Point2D point = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS).globalToLocal(event.getGlobalPoint());
			bubbleMenuTimer.start(new Point((int) point.getX(), (int) point.getY()));
		}
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		CIntentionCell cell = CIntentionCellController.getInstance().getCellById(currentCellId);
		cell.setCellIcon(CIntentionCell.CellIconType.NONE);

		synchronized (stateLock)
		{
			switch (state)
			{
				case DRAG:
					moveCurrentCell(event.getGlobalPoint(), false);
					Point2D local = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).globalToLocal(new Point(event.getPoint()));
					final long clusterId = IntentionGraph.getInstance().getClusterAt(local);
					long potentialTargetCell = CIntentionCellController.getInstance().getCellAt(event.getPoint(), currentCellId);
					long originalRoot = CIntentionCellController.getInstance().getClusterRootCanvasId(cell.getCanvasId());
					if (potentialTargetCell > 0l
							&& !CIntentionCellController.getInstance().isParent(
									CIntentionCellController.getInstance().getCellById(potentialTargetCell).getCanvasId(), cell.getCanvasId()))
					{
						long targetCanvasId = CIntentionCellController.getInstance().getCellById(potentialTargetCell).getCanvasId();
						CCanvasLinkController.getInstance().createLink(targetCanvasId, cell.getCanvasId());
					}
					else if (clusterId == originalRoot
							&& CIntentionCellController.getInstance().getCIntentionCellParent(cell.getCanvasId()) != originalRoot
							&& IntentionGraph.getInstance().ringContainsPoint(originalRoot, event.getPoint(), 0))
					{
						CCanvasLinkController.getInstance().createLink(originalRoot, cell.getCanvasId());
					}
					else if (clusterId != 0l && clusterId != originalRoot)
					{
						
						int numChildrenInOriginal = IntentionGraph.getInstance().getNumBaseClusterChildren(originalRoot);
						int numChildrenInTarget = IntentionGraph.getInstance().getNumBaseClusterChildren(clusterId);
						CCanvasLinkController.getInstance().createLink(clusterId, cell.getCanvasId());
						if (numChildrenInOriginal == 1)
							IntentionGraph.getInstance().removeExtraCluster(originalRoot);
						if (numChildrenInTarget == 0 && numChildrenInOriginal > 1)
							SwingUtilities.invokeLater(
									new Runnable() { public void run() { 
										IntentionGraph.getInstance().createClusterIfNoEmptyClusterExists(clusterId);
									}});
							
					}
					break;
				case ACTIVATED:
					if (event.getGlobalPoint().distance(mouseDragAnchor) < DRAG_THRESHOLD
							&& cell.getVisible())
					{
						CCanvasController.loadCanvas(cell.getCanvasId());
					}
					else
					{
						Point2D localC = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).globalToLocal(new Point(event.getPoint()));
						long clusterIdC = IntentionGraph.getInstance().getClusterAt(localC);
						if (clusterIdC > 0)
							IntentionGraph.getInstance().setFocusToCluster(clusterIdC, true);
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

	/**
	 * Show the bubble menu after a press&hold delay of 200ms, unless the input is released or dragged more than 20
	 * pixels before the timer expires.
	 * 
	 * @author Byron Hawkins
	 */
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
						
						boolean isRootCanvas = CIntentionCellController.getInstance().isRootCanvas(CIntentionCellController.getInstance().getCellById(getActiveCell()).getCanvasId());

						if (CCanvasController.canvasdb.size() > 1
								&& !isRootCanvas)
						{
							BubbleMenu.displayBubbleMenu(currentCellId, true, BUBBLE_MENU_TYPE_ID, deleteCanvasButton, /*linkButton,*/ setCanvasTitleButton /*, zoomToClusterButton*/);
						}
						else
						{
							BubbleMenu.displayBubbleMenu(currentCellId, true, BUBBLE_MENU_TYPE_ID, /*linkButton,*/ setCanvasTitleButton /*xxxx, zoomToClusterButton*/);
						}
					}
				}
			}
		}
	}

	/**
	 * Integration point for a CIC with the bubble menu.
	 * 
	 * @author Byron Hawkins
	 */
	private static class BubbleMenuComponentType implements BubbleMenu.ComponentType
	{
		@Override
		public PBounds getBounds(long uuid)
		{
			PBounds local = CIntentionCellController.getInstance().getCellById(uuid).getGlobalBounds();
			if (CCanvasController.exists(CCanvasController.getCurrentUUID()))	
				return new PBounds(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer().globalToLocal(local));
			else
				return local;
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
			if (buttonClassname.equals(SetCanvasTitleButton.class.getName()))
			{
				return 3;
			}
			
			return 0;
		}
	}
}
