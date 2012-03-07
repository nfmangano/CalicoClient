package calico.plugins.iip.components.piemenu.iip;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import calico.components.arrow.AbstractArrow;
import calico.components.arrow.AbstractArrowAnchorPoint;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionGraphController;

public class CreateIntentionArrowPhase implements MouseListener, MouseMotionListener
{
	public static CreateIntentionArrowPhase getInstance()
	{
		return INSTANCE;
	}

	private static final double DRAG_THRESHOLD = 3.0;
	
	static final CreateIntentionArrowPhase INSTANCE = new CreateIntentionArrowPhase();

	private enum Mode
	{
		MOVE_ANCHOR_A,
		MOVE_ANCHOR_B,
		CREATE;
	}

	private boolean dragInitiated;
	private Mode mode;
	private CCanvasLink link;
	private CIntentionCell fromCell;
	private CIntentionCell toCell;
	private Point2D anchorPoint;
	private CCanvasLink.LinkType type;

	private final TransitoryArrow arrow = new TransitoryArrow();

	private boolean onSelf;

	public CreateIntentionArrowPhase()
	{
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).addChild(arrow);
		arrow.setVisible(false);
	}

	public void startMove(CCanvasLink link, boolean moveA)
	{
		this.mode = moveA ? Mode.MOVE_ANCHOR_A : Mode.MOVE_ANCHOR_B;
		this.link = link;
		this.fromCell = CIntentionCellController.getInstance().getCellByCanvasId(link.getAnchorA().getCanvasId());
		this.toCell = CIntentionCellController.getInstance().getCellByCanvasId(link.getAnchorB().getCanvasId());
		this.anchorPoint = moveA ? link.getAnchorB().getPoint() : link.getAnchorA().getPoint();
		this.type = link.getLinkType();
		this.onSelf = false;

		IntentionGraphController.getInstance().getArrowByLinkId(link.getId()).setVisible(false);

		startPhase();
		startDrag();
	}

	void startCreate(CIntentionCell fromCell, Point anchorPoint, CCanvasLink.LinkType type)
	{
		this.mode = Mode.CREATE;
		this.link = null;
		this.fromCell = fromCell;
		this.toCell = null;
		this.anchorPoint = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT)
				.globalToLocal(new Point2D.Double(anchorPoint.getX(), anchorPoint.getY()));
		this.type = type;
		this.onSelf = false;
		this.dragInitiated = false;

		System.out.println("Start creating " + type + " arrow from cell #" + fromCell.getCanvasId() + " at " + fromCell.getLocation() + " with anchor point "
				+ anchorPoint);

		startPhase();
	}

	private void startPhase()
	{
		IntentionGraph.getInstance().addMouseListener(this);
		IntentionGraph.getInstance().addMouseMotionListener(this);

		if (fromCell != null)
		{
			fromCell.setHighlighted(true);
		}
		if (toCell != null)
		{
			toCell.setHighlighted(true);
		}
	}

	private void startDrag()
	{
		dragInitiated = true;

		moveTransitoryArrow(anchorPoint, true);

		arrow.setVisible(true);
	}

	private void moveTransitoryArrow(Point2D point, boolean fixedSide)
	{
		if (fixedSide)
		{
			switch (mode)
			{
				case CREATE:
				case MOVE_ANCHOR_B:
					arrow.a.getPoint().setLocation(point);
					break;
				case MOVE_ANCHOR_A:
					arrow.b.getPoint().setLocation(point);
					break;
				default:
					throw new IllegalArgumentException("Unknown mode " + mode);
			}
		}
		else
		{
			switch (mode)
			{
				case CREATE:
				case MOVE_ANCHOR_B:
					arrow.b.getPoint().setLocation(point);
					break;
				case MOVE_ANCHOR_A:
					arrow.a.getPoint().setLocation(point);
					break;
				default:
					throw new IllegalArgumentException("Unknown mode " + mode);
			}
		}

		arrow.redraw(true);
	}

	private void terminatePhase(Point terminationPoint)
	{
		IntentionGraph.getInstance().removeMouseListener(this);
		IntentionGraph.getInstance().removeMouseMotionListener(this);

		if (fromCell != null)
		{
			fromCell.setHighlighted(false);
		}
		if (toCell != null)
		{
			toCell.setHighlighted(false);
		}

		if (dragInitiated)
		{
			arrow.setVisible(false);

			if (onSelf)
			{
				System.out.println("Cancelling arrow creation because the arrow is pointing to the source cell");
				return;
			}
		}

		Point2D graphPosition = getGraphPosition(terminationPoint);
		if (mode == Mode.CREATE)
		{
			createLink(graphPosition);
		}
		else
		{
			moveLink(graphPosition);
		}
	}

	private void moveLink(Point2D graphPosition)
	{
		CCanvasLinkAnchor anchor = (mode == Mode.MOVE_ANCHOR_A) ? link.getAnchorA() : link.getAnchorB();
		if (getTransitoryCell() == null)
		{
			CCanvasLinkController.getInstance().orphanLink(anchor, graphPosition.getX(), graphPosition.getY());
		}
		else
		{
			long canvasId = (mode == Mode.MOVE_ANCHOR_A) ? fromCell.getCanvasId() : toCell.getCanvasId();
			CCanvasLinkController.getInstance().moveLink(anchor, canvasId);
		}

		IntentionGraphController.getInstance().getArrowByLinkId(link.getId()).setVisible(true);
	}

	private void createLink(Point2D graphPosition)
	{
		if (dragInitiated)
		{
			if (toCell == null)
			{
				CCanvasLinkController.getInstance().createLinkToEmptyCanvas(fromCell.getCanvasId(), type, graphPosition.getX(), graphPosition.getY());
			}
			else
			{
				CCanvasLinkController.getInstance().createLink(fromCell.getCanvasId(), toCell.getCanvasId(), type);
			}
		}
		else
		{
			CCanvasLinkController.getInstance().createLinkToEmptyCanvas(fromCell.getCanvasId(), type);
		}

		System.out.println("Create " + type + " arrow from cell #" + fromCell.getCanvasId() + " at " + fromCell.getLocation() + ", with anchor point "
				+ anchorPoint + ", to " + ((toCell == null) ? "the canvas" : "cell #" + toCell.getCanvasId()) + " at " + graphPosition);
	}

	private Point2D getGraphPosition(Point2D point)
	{
		return IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).globalToLocal(new Point2D.Double(point.getX(), point.getY()));
	}

	private CIntentionCell getAnchorCell()
	{
		switch (mode)
		{
			case MOVE_ANCHOR_A:
				return toCell;
			case CREATE:
			case MOVE_ANCHOR_B:
				return fromCell;
			default:
				throw new IllegalArgumentException("Unknown mode " + mode);
		}
	}

	private CIntentionCell getTransitoryCell()
	{
		switch (mode)
		{
			case MOVE_ANCHOR_A:
				return fromCell;
			case CREATE:
			case MOVE_ANCHOR_B:
				return toCell;
			default:
				throw new IllegalArgumentException("Unknown mode " + mode);
		}
	}

	private void setTransitoryCell(CIntentionCell cell)
	{
		switch (mode)
		{
			case MOVE_ANCHOR_A:
				fromCell = cell;
				break;
			case CREATE:
			case MOVE_ANCHOR_B:
				toCell = cell;
				break;
			default:
				throw new IllegalArgumentException("Unknown mode " + mode);
		}
	}

	private boolean dragThresholdCrossed(Point point)
	{
		return anchorPoint.distance(point) >= DRAG_THRESHOLD;
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		if (!dragInitiated)
		{
			if (dragThresholdCrossed(event.getPoint()))
			{
				startDrag();
			}
			else
			{
				return;
			}
		}

		Point2D graphPosition = getGraphPosition(event.getPoint());
		System.out.println("Drag arrowhead to " + graphPosition);

		moveTransitoryArrow(graphPosition, false);

		long newCellId = CIntentionCellController.getInstance().getCellAt(event.getPoint());

		onSelf = ((getAnchorCell() != null) && (newCellId == getAnchorCell().getId()));

		CIntentionCell newCell;
		if ((newCellId < 0L) || onSelf)
		{
			newCell = null;
		}
		else
		{
			newCell = CIntentionCellController.getInstance().getCellById(newCellId);
		}
		if ((getTransitoryCell() != null) && (newCell != getTransitoryCell()))
		{
			getTransitoryCell().setHighlighted(false);
		}
		setTransitoryCell(newCell);
		if (getTransitoryCell() != null)
		{
			getTransitoryCell().setHighlighted(true);
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

	private class TransitoryArrow extends AbstractArrow<TransitoryAnchor>
	{
		final TransitoryAnchor a;
		final TransitoryAnchor b;

		public TransitoryArrow()
		{
			super(Color.black, TYPE_NORM_HEAD_B);

			setAnchorA(a = new TransitoryAnchor());
			setAnchorB(b = new TransitoryAnchor());
		}
	}

	private class TransitoryAnchor extends AbstractArrowAnchorPoint
	{
	}
}
