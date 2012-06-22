package calico.perspectives;

import java.awt.Point;
import java.awt.event.MouseListener;

import calico.CalicoDraw;
import calico.controllers.CArrowController;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.inputhandlers.InputEventInfo;
import edu.umd.cs.piccolo.PNode;

public class CanvasPerspective extends CalicoPerspective
{
	private static final CanvasPerspective INSTANCE = new CanvasPerspective();

	public static CanvasPerspective getInstance()
	{
		return INSTANCE;
	}
	
	@Override
	protected void displayPerspective(long contextCanvasId)
	{
		CCanvasController.loadCanvas(contextCanvasId);
	}
	
	protected boolean showBubbleMenu(PNode bubbleHighlighter, PNode bubbleContainer)
	{
		//CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera().addChild(bubbleHighlighter);
		//CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera().addChild(bubbleContainer);
		CalicoDraw.addChildToNode(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera(), bubbleHighlighter);
		CalicoDraw.addChildToNode(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera(), bubbleContainer);
		return true;
	}

	protected void drawPieMenu(PNode pieCrust)
	{
		//CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera().addChild(pieCrust);
		CalicoDraw.addChildToNode(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera(), pieCrust);
		CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).repaint();
	}

	protected boolean hasPhasicPieMenuActions()
	{
		return true;
	}

	protected boolean processToolEvent(InputEventInfo event)
	{
		if (CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).isPointOnMenuBar(event.getGlobalPoint()))
		{
			if (event.getAction() == InputEventInfo.ACTION_PRESSED || event.getAction() == InputEventInfo.ACTION_RELEASED)
				CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).clickMenuBar(event, event.getGlobalPoint());

			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * What UUID arrow is located at the requested X,Y coordinate
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private long getArrowAtPoint(int x, int y)
	{
		long[] arrowlist = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getChildArrows();
		if (arrowlist.length > 0)
		{
			for (int i = 0; i < arrowlist.length; i++)
			{
				if (CArrowController.arrows.get(arrowlist[i]).containsMousePoint(new Point(x, y)))
				{
					return arrowlist[i];
				}// if contained
			}// for groups

		}// if grplist>0
		return 0L;
	}

	@Override
	protected long getEventTarget(InputEventInfo event)
	{
		// check for arrows
		long arrowAtPoint = getArrowAtPoint(event.getX(), event.getY());
		if (arrowAtPoint != 0L)
		{
			return arrowAtPoint;
		}

		// Check to see if any groups fit in to the point
		long smallestGroupUUID = CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), event.getPoint());
		if (smallestGroupUUID != 0L)
		{
			// we found the smallest group that contains the coord. So run her action listener
			return smallestGroupUUID;
		}

		// Set a default, if all else fails, we go to the canvas
		return CCanvasController.getCurrentUUID();
	}

	protected void addMouseListener(MouseListener listener)
	{
		CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).addMouseListener(listener);
	}

	protected void removeMouseListener(MouseListener listener)
	{
		CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).removeMouseListener(listener);
	}

	@Override
	protected boolean isNavigationPerspective()
	{
		return false;
	}
}
