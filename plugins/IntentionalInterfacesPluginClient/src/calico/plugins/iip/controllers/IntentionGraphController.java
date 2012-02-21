package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.geom.Point2D;
import java.util.List;

import calico.controllers.CCanvasController;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CCanvasLinkAnchor.ArrowEndpointType;
import calico.plugins.iip.components.CCanvasLinkArrow;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.graph.IntentionGraph;

public class IntentionGraphController
{
	public static IntentionGraphController getInstance()
	{
		return INSTANCE;
	}

	public static void initialize()
	{
		INSTANCE = new IntentionGraphController();
	}

	private static IntentionGraphController INSTANCE;

	private final Long2ReferenceArrayMap<CCanvasLinkArrow> arrowsByLinkId = new Long2ReferenceArrayMap<CCanvasLinkArrow>();

	public void addLink(CCanvasLink link)
	{
		CCanvasLinkArrow arrow = new CCanvasLinkArrow(link);
		arrowsByLinkId.put(link.getId(), arrow);
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).addChild(arrow);
		arrow.redraw();
	}

	private void updateAnchorPosition(CCanvasLinkAnchor anchor)
	{
		if (anchor.getArrowEndpointType() == ArrowEndpointType.INTENTION_CELL)
		{
			CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(anchor.getCanvasId());

			Point2D position = alignAnchorAtCellEdge(cell.getLocation().getX(), cell.getLocation().getY(), getOppositePosition(anchor));
			anchor.getPoint().setLocation(position);
		}
	}

	private Point2D getOppositePosition(CCanvasLinkAnchor anchor)
	{
		CCanvasLinkAnchor opposite = anchor.getOpposite();
		switch (opposite.getArrowEndpointType())
		{
			case INTENTION_CELL:
				return CIntentionCellController.getInstance().getCellByCanvasId(opposite.getCanvasId()).getCenter();
			case FLOATING:
				return anchor.getPoint();
			default:
				throw new IllegalArgumentException("Unknown anchor type " + anchor.getArrowEndpointType());
		}
	}

	public void removeLink(CCanvasLink link)
	{
		CCanvasLinkArrow arrow = arrowsByLinkId.remove(link.getId());
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).removeChild(arrow);
	}

	public void contentChanged(long canvas_uuid)
	{
		boolean hasContent = CCanvasController.hasContent(canvas_uuid);
		CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid);

		if (cell == null)
		{
			return;
		}

		cell.contentsChanged();

		if (hasContent != cell.isVisible())
		{
			CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).setVisible(hasContent);
		}
	}

	public void prepareDisplay()
	{
		IntentionGraph.getInstance().fitContents();
	}

	public void updateLinkArrow(CCanvasLink link)
	{
		CCanvasLinkArrow arrow = arrowsByLinkId.get(link.getId());
		arrow.redraw();
	}

	public void localUpdateAttachedArrows(long cellId, double x, double y)
	{
		long canvasId = CIntentionCellController.getInstance().getCellById(cellId).getCanvasId();
		List<Long> anchorIds = CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(canvasId);
		for (long anchorId : anchorIds)
		{
			CCanvasLinkAnchor anchor = CCanvasLinkController.getInstance().getAnchor(anchorId);
			Point2D edgePosition = alignAnchorAtCellEdge(x, y, getOppositePosition(anchor));
			anchor.getPoint().setLocation(edgePosition);
			updateLinkArrow(anchor.getLink());
		}
	}

	public void updateAttachedArrows(long cellId, double x, double y)
	{
		long canvasId = CIntentionCellController.getInstance().getCellById(cellId).getCanvasId();
		List<Long> anchorIds = CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(canvasId);
		for (long anchorId : anchorIds)
		{
			CCanvasLinkAnchor anchor = CCanvasLinkController.getInstance().getAnchor(anchorId);
			Point2D edgePosition = alignAnchorAtCellEdge(x, y, getOppositePosition(anchor));

			CCanvasLinkController.getInstance().moveLinkAnchor(anchor, edgePosition);
		}
	}

	public Point2D getArrowAnchorPosition(long canvas_uuid, long opposite_canvas_uuid)
	{
		CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(opposite_canvas_uuid);
		return getArrowAnchorPosition(canvas_uuid, cell.getCenter());
	}

	public Point2D getArrowAnchorPosition(long canvas_uuid, Point2D opposite)
	{
		return getArrowAnchorPosition(canvas_uuid, opposite.getX(), opposite.getY());
	}

	public Point2D getArrowAnchorPosition(long canvas_uuid, double xOpposite, double yOpposite)
	{
		CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid);
		return alignAnchorAtCellEdge(cell.getLocation().getX(), cell.getLocation().getY(), xOpposite, yOpposite);
	}

	private Point2D alignAnchorAtCellEdge(double xCell, double yCell, Point2D opposite)
	{
		return alignAnchorAtCellEdge(xCell, yCell, opposite.getX(), opposite.getY());
	}

	private Point2D alignAnchorAtCellEdge(double xCell, double yCell, double xOpposite, double yOpposite)
	{
		// for now just put the link endpoint in the center of the CIC
		return new Point2D.Double(xCell + 32, yCell + 32);
	}
}
