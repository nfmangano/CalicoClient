package calico.plugins.iip.controllers;

import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.plugins.iip.components.CIntentionCell;

/**
 * Implements this plugin's interface point to the <code>CCanvas</code> structure.
 * 
 * @author Byron Hawkins
 */
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

	/**
	 * Notify the <code>CCanvasController</code> that some change has been made to <code>canvasId</code>.
	 */
	public void notifyContentChanged(long canvasId)
	{
		CCanvasController.notifyContentChanged(this, canvasId);
	}

	@Override
	public void contentChanged(long canvas_uuid)
	{
		CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid);
		if (cell == null)
		{
			return;
		}

		IntentionGraphController.getInstance().contentChanged(canvas_uuid);
	}

	@Override
	public void clearContent(long canvas_uuid)
	{
		CCanvasLinkController.getInstance().clearLinks(canvas_uuid);

		long cellId = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).getId();
		CIntentionCellController.getInstance().clearCell(cellId);
	}
}
