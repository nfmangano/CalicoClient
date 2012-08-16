package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.canvas.CanvasInputProximity;

/**
 * Manages this plugin's role in the 2-phase process of creating a new canvas, which consumers request via
 * <code>createNewCell()</code>. First a new canvas is created by request to the <code>CCanvasController.Factory</code>.
 * When the server responds with the new canvas, this class continues by creating and returning a new CIC.
 * 
 * @author Byron Hawkins
 */
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
		return createNewCell(0L, CanvasInputProximity.NONE);
	}

	public CIntentionCell createNewCell(long originatingCanvasId, CanvasInputProximity proximity)
	{
		PendingCell pendingCell = new PendingCell();

		synchronized (pendingCellsByCanvasId)
		{
			long canvasId = CCanvasController.Factory.getInstance().createNewCanvas(originatingCanvasId).uuid;
			pendingCellsByCanvasId.put(canvasId, pendingCell);

			if (originatingCanvasId > 0L)
			{
				IntentionCanvasController.getInstance().canvasCreatedLocally(canvasId, originatingCanvasId, proximity);
			}
		}

		return pendingCell.waitForCell();
	}

	public void cellCreated(CIntentionCell cell)
	{
		synchronized (pendingCellsByCanvasId)
		{
			PendingCell pendingCell = pendingCellsByCanvasId.get(cell.getCanvasId());
			if (pendingCell != null)
			{
				cell.setNew(true);
				pendingCellsByCanvasId.remove(cell.getCanvasId());
				pendingCell.cellArrived(cell);
			}
		}
	}

	/**
	 * Synchronization device to wait for the new canvas to be created on the server.
	 * 
	 * @author Byron Hawkins
	 */
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
