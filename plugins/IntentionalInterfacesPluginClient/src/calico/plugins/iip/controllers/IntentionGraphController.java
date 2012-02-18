package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.geom.Point2D;
import java.util.List;

import calico.controllers.CCanvasController;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
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

	public void removeLink(CCanvasLink link)
	{
		arrowsByLinkId.remove(link.getId());
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
			Point2D edgePosition = alignAnchorAtCellEdge(x, y, anchor);
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
			Point2D edgePosition = alignAnchorAtCellEdge(x, y, anchor);
			
			CCanvasLinkController.getInstance().moveLinkAnchor(anchor, edgePosition);
		}	
	}
	
	private Point2D alignAnchorAtCellEdge(double xCell, double yCell, CCanvasLinkAnchor anchor)
	{
		// for now just put the link endpoint in the center of the CIC
		return new Point2D.Double(xCell + 32, yCell + 32);
	}
}
