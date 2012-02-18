package calico.plugins.iip.components.piemenu.iip;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CIntentionCellController;

class CreateIntentionArrowPhase implements MouseListener, MouseMotionListener
{
	static final CreateIntentionArrowPhase INSTANCE = new CreateIntentionArrowPhase();

	private CIntentionCell fromCell;
	private CIntentionCell toCell;
	private Point2D anchorPoint;
	private CCanvasLink.LinkType type;

	private boolean onSelf;

	void startPhase(CIntentionCell fromCell, Point anchorPoint, CCanvasLink.LinkType type)
	{
		this.fromCell = fromCell;
		this.toCell = null;
		this.anchorPoint = getGraphPosition(anchorPoint);
		this.type = type;
		this.onSelf = false;

		IntentionGraph.getInstance().addMouseListener(this);
		IntentionGraph.getInstance().addMouseMotionListener(this);

		fromCell.setHighlighted(true);

		System.out.println("Start creating " + type + " arrow from cell #" + fromCell.getCanvasId() + " at " + fromCell.getLocation() + " with anchor point "
				+ anchorPoint);
	}

	private void terminatePhase(Point terminationPoint)
	{
		IntentionGraph.getInstance().removeMouseListener(this);
		IntentionGraph.getInstance().removeMouseMotionListener(this);

		fromCell.setHighlighted(false);

		if (onSelf)
		{
			System.out.println("Cancelling arrow creation because the arrow is pointing to the source cell");
			return;
		}
		
		if (toCell != null)
		{
			toCell.setHighlighted(false);
		}

		Point2D graphPosition = getGraphPosition(terminationPoint);
		System.out.println("Create " + type + " arrow from cell #" + fromCell.getCanvasId() + " at " + fromCell.getLocation() + ", with anchor point "
				+ anchorPoint + ", to " + ((toCell == null) ? "the canvas" : "cell #" + toCell.getCanvasId()) + " at " + graphPosition);
	}

	private Point2D getGraphPosition(Point point)
	{
		return IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).globalToLocal(point);
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		Point2D graphPosition = getGraphPosition(event.getPoint());
		System.out.println("Drag arrowhead to " + graphPosition);

		long newToCellId = CIntentionCellController.getInstance().getCellAt(event.getPoint());

		onSelf = (newToCellId == fromCell.getId());

		CIntentionCell newToCell;
		if ((newToCellId < 0L) || (newToCellId == fromCell.getId()))
		{
			newToCell = null;
		}
		else
		{
			newToCell = CIntentionCellController.getInstance().getCellById(newToCellId);
		}
		if ((toCell != null) && (newToCell != toCell))
		{
			toCell.setHighlighted(false);
		}
		toCell = newToCell;
		if (toCell != null)
		{
			toCell.setHighlighted(true);
		}
	}

	@Override
	public void mouseMoved(MouseEvent event)
	{
	}

	@Override
	public void mouseClicked(MouseEvent event)
	{
	}

	@Override
	public void mouseEntered(MouseEvent event)
	{
	}

	@Override
	public void mouseExited(MouseEvent event)
	{
		terminatePhase(event.getPoint());
	}

	@Override
	public void mousePressed(MouseEvent event)
	{
	}

	@Override
	public void mouseReleased(MouseEvent event)
	{
		terminatePhase(event.getPoint());
	}
}
