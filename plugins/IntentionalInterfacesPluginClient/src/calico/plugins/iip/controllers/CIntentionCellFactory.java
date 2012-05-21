package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.plugins.iip.components.CIntentionCell;

public class CIntentionCellFactory
{
	private static final CIntentionCellFactory INSTANCE = new CIntentionCellFactory();

	public static CIntentionCellFactory getInstance()
	{
		return INSTANCE;
	}

	private final Long2ReferenceOpenHashMap<PendingCell> pendingCellsByCanvasId = new Long2ReferenceOpenHashMap<PendingCell>();

	public CIntentionCell createNewCell()
	{
		PendingCell pendingCell = new PendingCell();
		
		synchronized (pendingCellsByCanvasId)
		{
			long canvasId = CCanvasController.Factory.getInstance().createNewCanvas().uuid;
			pendingCellsByCanvasId.put(canvasId, pendingCell);
		}

		return pendingCell.waitForCell();
	}

	public void cellCreated(CIntentionCell cell)
	{
		synchronized (pendingCellsByCanvasId)
		{
			PendingCell pendingCell = pendingCellsByCanvasId.get(cell.getCanvasId());
			boolean install;
			if (pendingCell != null)
			{
				cell.setNew(true);
				pendingCellsByCanvasId.remove(cell.getCanvasId());
				pendingCell.cellArrived(cell);
			}
		}
	}

	private class PendingCell
	{
		private CIntentionCell cell = null;

		synchronized void cellArrived(CIntentionCell cell)
		{
			this.cell = cell;
			notify();
		}

		synchronized CIntentionCell waitForCell()
		{
			while (cell == null)
			{
				try
				{
					wait();
				}
				catch (InterruptedException ok)
				{
				}
			}

			return cell;
		}
	}
}
