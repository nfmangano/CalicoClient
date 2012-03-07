package calico.plugins.iip.controllers;

import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.plugins.iip.components.CIntentionCell;

public class IntentionalInterfacesCanvasContributor implements CCanvas.ContentContributor
{
	public static IntentionalInterfacesCanvasContributor getInstance()
	{
		return INSTANCE;
	}

	public static void initialize()
	{
		INSTANCE = new IntentionalInterfacesCanvasContributor();
	}

	private static IntentionalInterfacesCanvasContributor INSTANCE;

	private IntentionalInterfacesCanvasContributor()
	{
		CCanvasController.addContentContributor(this);
	}

	public void notifyContentChanged(long canvasId)
	{
		CCanvasController.notifyContentChanged(this, canvasId);
	}

	@Override
	public boolean hasContent(long canvas_uuid)
	{
		if (CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).isInUse())
		{
			return true;
		}

		return CCanvasLinkController.getInstance().hasLinks(canvas_uuid);
	}

	@Override
	public void contentChanged(long canvas_uuid)
	{
		CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid);
		if (cell == null)
		{
			return;
		}

		if ((!CCanvasController.canvasdb.get(canvas_uuid).isEmpty()) && !cell.isInUse())
		{
			CIntentionCellController.getInstance().setInUse(cell.getId(), true);
		}

		IntentionGraphController.getInstance().contentChanged(canvas_uuid);
	}

	@Override
	public void clearContent(long canvas_uuid)
	{
		CIntentionCellController.getInstance().setInUse(CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).getId(), false);
		CCanvasLinkController.getInstance().clearLinks(canvas_uuid);
	}
}
