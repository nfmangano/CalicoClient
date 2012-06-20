package calico.plugins.iip.components.canvas;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.CIntentionCellFactory;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.controllers.IntentionGraphController;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class CopyCanvasButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;

	private long currentCanvasId;

	public CopyCanvasButton()
	{
		this(0L);
	}

	/**
	 * Invoked via reflection in CanvasStatusBar
	 */
	public CopyCanvasButton(long canvas_uuid)
	{
		try
		{
			this.currentCanvasId = canvas_uuid;

			setImage(CalicoIconManager.getIconImage("intention.copy-canvas"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void actionMouseClicked()
	{
		long newCanvasId = CIntentionCellFactory.getInstance()
				.createNewCell(CCanvasController.getCurrentUUID(), CanvasInputProximity.forPosition(getBounds().getX())).getCanvasId();
		CCanvasLinkController.getInstance().copyCanvas(currentCanvasId, newCanvasId);

		if (CanvasPerspective.getInstance().isActive())
		{
			CCanvasController.loadCanvas(newCanvasId);
		}
	}
}
