package calico.plugins.iip.components.piemenu.iip;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import calico.components.arrow.AbstractArrow;
import calico.components.arrow.AbstractArrowAnchorPoint;
import calico.controllers.CCanvasController;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CCanvasLinkArrow;
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

	static final CreateIntentionArrowPhase INSTANCE = new CreateIntentionArrowPhase();

	public enum MoveLinkEndpointMode
	{
		MOVE_ANCHOR_A,
		MOVE_ANCHOR_B;
	}

	public enum NewLinkMode
	{
		LINK_TO_COPY,
		LINK_TO_BLANK,
		LINK_EXISTING;
	}

	private enum Mode
	{
		MOVE_ANCHOR_A,
		MOVE_ANCHOR_B,
		LINK_TO_COPY,
		LINK_TO_BLANK,
		LINK_EXISTING;
	}

	private boolean dragInitiated;
	private Mode mode;
	private CCanvasLink link;
	private CIntentionCell fromCell;
	private CIntentionCell toCell;
	private Point2D anchorPoint;
	private Point2D dragStartPoint;
	private boolean copy;

	private final TransitoryArrow arrow = new TransitoryArrow();

	private boolean onSelf;

	public CreateIntentionArrowPhase()
	{
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).addChild(arrow);
		arrow.setVisible(false);
	}

	public void startMove(CCanvasLink link, MoveLinkEndpointMode moveMode, Point dragStartPoint)
	{
		this.mode = (moveMode == MoveLinkEndpointMode.MOVE_ANCHOR_A) ? Mode.MOVE_ANCHOR_A : Mode.MOVE_ANCHOR_B;
		this.link = link;
		this.fromCell = CIntentionCellController.getInstance().getCellByCanvasId(link.getAnchorA().getCanvasId());
		this.toCell = CIntentionCellController.getInstance().getCellByCanvasId(link.getAnchorB().getCanvasId());
		this.anchorPoint = (mode == Mode.MOVE_ANCHOR_A) ? link.getAnchorB().getPoint() : link.getAnchorA().getPoint();
		this.dragStartPoint = dragStartPoint;
		this.onSelf = false;

		IntentionGraphController.getInstance().getArrowByLinkId(link.getId()).setVisible(false);

		startPhase();
		startDrag();
	}

	void startCreate(CIntentionCell fromCell, Point dragStartPoint, NewLinkMode mode)
	{
		this.mode = (mode == NewLinkMode.LINK_EXISTING) ? Mode.LINK_EXISTING : (mode == NewLinkMode.LINK_TO_BLANK) ? Mode.LINK_TO_BLANK : Mode.LINK_TO_COPY;
		this.link = null;
		this.fromCell = fromCell;
		this.toCell = null;
		this.anchorPoint = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT)
				.globalToLocal(new Point2D.Double(dragStartPoint.getX(), dragStartPoint.getY()));
		this.dragStartPoint = dragStartPoint;
		this.onSelf = false;
		this.dragInitiated = false;

		System.out.println("Start creating arrow from cell #" + fromCell.getCanvasId() + " at " + fromCell.getLocation() + " with anchor point " + anchorPoint);

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
				case LINK_EXISTING:
				case LINK_TO_BLANK:
				case LINK_TO_COPY:
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
				case LINK_EXISTING:
				case LINK_TO_BLANK:
				case LINK_TO_COPY:
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
		switch (mode)
		{
			case LINK_EXISTING:
			case LINK_TO_BLANK:
			case LINK_TO_COPY:
				createLink(graphPosition);
				break;
			case MOVE_ANCHOR_A:
			case MOVE_ANCHOR_B:
				moveLink(graphPosition);
				break;
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

		CCanvasLinkArrow arrow = IntentionGraphController.getInstance().getArrowByLinkId(link.getId());
		arrow.setVisible(true);
		arrow.setHighlighted(false);
	}

	private void createLink(Point2D graphPosition)
	{
		if (dragInitiated)
		{
			if (toCell == null)
			{
				if (mode == Mode.LINK_EXISTING)
				{
					CCanvasLinkController.getInstance().createOrphanedLink(fromCell.getCanvasId(), graphPosition.getX(), graphPosition.getY());
				}
				else
				{
					CCanvasLinkController.getInstance().createLinkToEmptyCanvas(fromCell.getCanvasId(), graphPosition.getX(), graphPosition.getY(),
							(mode == Mode.LINK_TO_COPY));
				}
			}
			else
			{
				CCanvasLinkController.getInstance().createLink(fromCell.getCanvasId(), toCell.getCanvasId());
			}
		}
		else
		{
			long newCanvasId = CCanvasLinkController.getInstance().createLinkToEmptyCanvas(fromCell.getCanvasId());
			if (copy)
			{
				CCanvasLinkController.getInstance().copyCanvas(fromCell.getCanvasId(), newCanvasId);
			}
		}

		System.out.println("Create arrow from cell #" + fromCell.getCanvasId() + " at " + fromCell.getLocation() + ", with anchor point " + anchorPoint
				+ ", to " + ((toCell == null) ? "the canvas" : "cell #" + toCell.getCanvasId()) + " at " + graphPosition);
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
			case LINK_EXISTING:
			case LINK_TO_BLANK:
			case LINK_TO_COPY:
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
			case LINK_EXISTING:
			case LINK_TO_BLANK:
			case LINK_TO_COPY:
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
			case LINK_EXISTING:
			case MOVE_ANCHOR_B:
				toCell = cell;
				break;
			case LINK_TO_BLANK:
			case LINK_TO_COPY:
				if (cell != null)
				{
					throw new IllegalArgumentException("Can't set the transitory cell for mode " + mode);
				}
				else
				{
					break;
				}
			default:
				throw new IllegalArgumentException("Unknown mode " + mode);
		}
	}

	private boolean canLinkTo(CIntentionCell cell)
	{
		if ((cell == null) || onSelf || (mode == Mode.LINK_TO_BLANK) || (mode == Mode.LINK_TO_COPY))
		{
			return false;
		}

		if (mode != Mode.MOVE_ANCHOR_A)
		{
			for (Long anchorId : CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(cell.getCanvasId()))
			{
				CCanvasLinkAnchor anchor = CCanvasLinkController.getInstance().getAnchor(anchorId);
				if (anchor.getLink().getAnchorB() == anchor)
				{
					return false;
				}
			}
		}

		CIntentionCell anchorA = (mode == Mode.MOVE_ANCHOR_A) ? cell : getAnchorCell();
		CIntentionCell target = (mode == Mode.MOVE_ANCHOR_A) ? getAnchorCell() : cell;
		if (isParent(target, anchorA.getCanvasId()))
		{
			return false;
		}

		return true;
	}

	private boolean isParent(CIntentionCell target, long canvasIdOfAnchorA)
	{
		CCanvasLinkAnchor incomingAnchor = null;
		for (Long anchorId : CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(canvasIdOfAnchorA))
		{
			CCanvasLinkAnchor anchor = CCanvasLinkController.getInstance().getAnchor(anchorId);
			if (anchor.getLink().getAnchorB() == anchor)
			{
				incomingAnchor = anchor;
				break;
			}
		}

		if (incomingAnchor == null)
		{
			return false;
		}

		if (incomingAnchor.getOpposite().getCanvasId() == target.getCanvasId())
		{
			System.out.println("Cycle detected on canvas id " + target.getCanvasId());
			return true;
		}

		return isParent(target, incomingAnchor.getOpposite().getCanvasId());
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		if (!dragInitiated)
		{
			startDrag();
		}

		Point2D graphPosition = getGraphPosition(event.getPoint());

		moveTransitoryArrow(graphPosition, false);

		long newCellId = CIntentionCellController.getInstance().getCellAt(event.getPoint());
		CIntentionCell newCell = CIntentionCellController.getInstance().getCellById(newCellId);

		// kind of risky to require `onSelf to be set before calling canLinkTo()
		onSelf = ((getAnchorCell() != null) && (newCellId == getAnchorCell().getId()));

		if (!canLinkTo(newCell))
		{
			newCell = null;
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

		switch (mode)
		{
			case MOVE_ANCHOR_A:
			case MOVE_ANCHOR_B:
			case LINK_EXISTING:
				if (getTransitoryCell() == null)
				{
					arrow.setColor(CCanvasLinkArrow.FLOATING_COLOR);
					break;
				}
			case LINK_TO_BLANK:
			case LINK_TO_COPY:
				arrow.setColor(CCanvasLinkArrow.NORMAL_COLOR);
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
