package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;

import calico.CalicoOptions;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.PressAndHoldAction;
import calico.inputhandlers.CalicoAbstractInputHandler.MenuTimer;
import calico.plugins.iip.components.canvas.CanvasTitleDialog;
import calico.plugins.iip.components.canvas.CanvasTitleDialog.Action;
import calico.plugins.iip.components.graph.CIntentionTopology;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.graph.CIntentionTopology.Cluster;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.utils.Ticker;
import edu.umd.cs.piccolo.PLayer;

/**
 * Custom <code>CalicoInputManager</code>handler for events related to open space in the Intention View. The main Calico
 * event handling mechanism will determine whether input relates to the view by calling
 * <code>IntentionalInterfacesPerspective.getEventTarget()</code>. When that method returns the Intention View, the
 * associated input event will be sent here.
 * 
 * The only supported operation is to pan the Intention View by dragging it.
 * 
 * @author Byron Hawkins
 */
public class IntentionGraphInputHandler extends CalicoAbstractInputHandler implements PressAndHoldAction
{
	private enum State
	{
		IDLE,
		PAN;
	}

	long lastAction = 0;
	private State state = State.IDLE;
	private Point lastMouse, mouseDown, mouseUp;

	@Override
	public void actionPressed(InputEventInfo event)
	{
		lastAction = 0;
		state = State.PAN;
		lastMouse = event.getPoint();
		mouseDown = event.getPoint();
		
		long clusterIdWithTitleTextAtPoint = IntentionGraph.getInstance().getClusterWithTitleTextAtPoint(getLastPoint());
		
		if (clusterIdWithTitleTextAtPoint != 0l)
		{
			PLayer layer = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS);
			MenuTimer menuTimer = new CalicoAbstractInputHandler.MenuTimer(this, 0l, 100l, CalicoOptions.core.max_hold_distance, 1000,
					mouseDown, 0l, layer);
			Ticker.scheduleIn(250, menuTimer);
		}

	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		lastAction = 1;
		state = State.IDLE;
		Point2D local = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).globalToLocal(new Point(event.getPoint()));
		long clusterId = IntentionGraph.getInstance().getClusterAt(local);
		
		long clusterIdWithWallTextAtPoint = IntentionGraph.getInstance().getClusterWithWallTextAtPoint(getLastPoint());
		

		if (IntentionGraph.getInstance().getFocus() == IntentionGraph.Focus.CLUSTER
				&& IntentionGraph.getInstance().getClusterInFocus(this).createCanvasIconContainsPoint(event.getPoint()))
		{
			IntentionGraph.getInstance().createNewClusterCanvas();
		}
		else if (clusterIdWithWallTextAtPoint > 0l)
		{
			IntentionGraph.getInstance().setFocusToWall();
		}
		else if (clusterId > 0)
			IntentionGraph.getInstance().setFocusToCluster(clusterId, true);
//			IntentionGraph.getInstance().zoomToCluster(clusterId);
		else
			IntentionGraph.getInstance().setFocusToWall(true);
//			IntentionGraph.getInstance().fitContents();
		lastMouse = event.getPoint();
		mouseUp = event.getPoint();
	}

	@Override
	public void actionDragged(InputEventInfo event)
	{
		lastMouse = event.getPoint();
		
		/*
		
		double xMouseDelta = event.getGlobalPoint().x - lastMouse.x;
		double yMouseDelta = event.getGlobalPoint().y - lastMouse.y;

		double scale = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();

		xMouseDelta /= scale;
		yMouseDelta /= scale;

		

		IntentionGraph.getInstance().translate(xMouseDelta, yMouseDelta);
		*/
	}

	@Override
	public double getDraggedDistance() {
		return mouseDown.distance(lastMouse);
	}

	@Override
	public long getLastAction() {
		return lastAction;
	}

	@Override
	public Point getLastPoint() {
		return lastMouse;
	}

	@Override
	public Point getMouseDown() {
		return mouseDown;
	}

	@Override
	public Point getMouseUp() {
		return mouseUp;
	}

	@Override
	public void openMenu(long arg0, long arg1, Point arg2) {
		
	}

	@Override
	public void pressAndHoldAbortedEarly() {
		
	}

	@Override
	public void pressAndHoldCompleted() {
		long clusterIdWithTitleAtPoint = IntentionGraph.getInstance().getClusterWithTitleTextAtPoint(getLastPoint());
		
		if (clusterIdWithTitleAtPoint != 0l)
		{
			calico.plugins.iip.components.canvas.CanvasTitlePanel.setCanvasTitleText(clusterIdWithTitleAtPoint);
		}
		
	}
	

}
