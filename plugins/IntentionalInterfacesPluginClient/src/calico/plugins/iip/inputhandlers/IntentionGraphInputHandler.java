package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;

import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.IntentionGraph;

public class IntentionGraphInputHandler extends CalicoAbstractInputHandler
{
	private enum State
	{
		IDLE,
		PAN;
	}

	private State state = State.IDLE;
	private Point lastMouse;

	@Override
	public void actionPressed(InputEventInfo event)
	{
		state = State.PAN;
		lastMouse = event.getGlobalPoint();
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		state = State.IDLE;
	}

	@Override
	public void actionDragged(InputEventInfo event)
	{
		System.out.println("Drag the graph");
		
		double xMouseDelta = event.getGlobalPoint().x - lastMouse.x;
		double yMouseDelta = event.getGlobalPoint().y - lastMouse.y;
		
		double scale = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
		
		xMouseDelta /= scale;
		yMouseDelta /= scale;
		
		lastMouse = event.getGlobalPoint();

		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).translate(xMouseDelta, yMouseDelta);
	}
}
