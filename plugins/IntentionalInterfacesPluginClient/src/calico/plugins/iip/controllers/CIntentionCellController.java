package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoInputManager;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;
import calico.plugins.iip.inputhandlers.IntentionGraphInputHandler;

public class CIntentionCellController
{
	public static CIntentionCellController getInstance()
	{
		return INSTANCE;
	}

	public static void initialize()
	{
		INSTANCE = new CIntentionCellController();
	}

	private static CIntentionCellController INSTANCE;

	private static Long2ReferenceArrayMap<CIntentionCell> cells = new Long2ReferenceArrayMap<CIntentionCell>();
	private static Long2ReferenceArrayMap<CIntentionCell> cellsByCanvasId = new Long2ReferenceArrayMap<CIntentionCell>();

	public long getCellAt(Point point)
	{
		for (CIntentionCell cell : cells.values())
		{
			if (cell.contains(point))
			{
				return cell.getId();
			}
		}
		return -1L;
	}

	public void addCell(CIntentionCell cell)
	{
		cells.put(cell.getId(), cell);
		cellsByCanvasId.put(cell.getCanvasId(), cell);

		cell.display(true);

		CalicoInputManager.addCustomInputHandler(cell.getId(), CIntentionCellInputHandler.getInstance());

		System.out.println("Added a CIC: " + listCellAddresses());
	}

	public CIntentionCell getCellById(long uuid)
	{
		return cells.get(uuid);
	}

	public CIntentionCell getCellByCanvasId(long canvas_uuid)
	{
		return cellsByCanvasId.get(canvas_uuid);
	}

	public void removeCellById(long uuid)
	{
		CIntentionCell cell = cells.remove(uuid);

		cell.display(false);

		System.out.println("Removed a CIC: " + listCellAddresses());
	}

	private String listCellAddresses()
	{
		StringBuilder buffer = new StringBuilder("{");
		List<CIntentionCell> orderedCells = new ArrayList<CIntentionCell>(cells.values());
		Collections.sort(orderedCells, new CellAddressSorter());

		for (CIntentionCell cell : orderedCells)
		{
			buffer.append(CCanvasController.canvasdb.get(cell.getCanvasId()).getGridCoordTxt());
			buffer.append(", ");
		}
		if (buffer.length() > 0)
		{
			buffer.setLength(buffer.length() - 2);
		}
		buffer.append("}");

		return buffer.toString();
	}

	private static class CellAddressSorter implements Comparator<CIntentionCell>
	{
		public int compare(CIntentionCell first, CIntentionCell second)
		{
			return CCanvasController.canvasdb.get(first.getCanvasId()).getGridCoordTxt()
					.compareTo(CCanvasController.canvasdb.get(second.getCanvasId()).getGridCoordTxt());
		}
	}
}
