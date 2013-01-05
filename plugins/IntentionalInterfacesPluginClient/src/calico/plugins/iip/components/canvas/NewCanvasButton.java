package calico.plugins.iip.components.canvas;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.CIntentionCellFactory;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;

/**
 * Simple button for copying a canvas, delegating to <code>CIntentionCellFactory</code> to create a new canvas/CIC pair.
 * 
 * @author Byron Hawkins
 */
public class NewCanvasButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;

	private long currentCanvasId;

	public NewCanvasButton()
	{
		this(0L);
	}

	/**
	 * Invoked via reflection in CanvasStatusBar
	 */
	public NewCanvasButton(long canvas_uuid)
	{
		try
		{
			this.currentCanvasId = canvas_uuid;

			setImage(CalicoIconManager.getIconImage("intention.new-canvas"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (CIntentionCellController.getInstance().isRootCanvas(canvas_uuid))
			this.setTransparency(.5f);
	}

	public void actionMouseClicked()
	{
		long currentCell = CCanvasController.getCurrentUUID();
		long parentCanvasId = calico.plugins.iip.controllers.IntentionCanvasController.getParentCanvasId(currentCell);
		
		if (parentCanvasId == 0)
			return;
		
		long newCanvasId = CIntentionCellFactory.getInstance()
				.createNewCell(CCanvasController.getCurrentUUID(), CanvasInputProximity.forPosition(getBounds().getX())).getCanvasId();
		
//		IntentionCanvasController.getInstance().collapseLikeIntentionTypes();
		CCanvasLinkController.getInstance().createLink(/*parentCanvasId*/ CIntentionCellController.getInstance().getClusterRootCanvasId(currentCell), newCanvasId);
		
		if (CanvasPerspective.getInstance().isActive())
		{
			CCanvasController.loadCanvas(newCanvasId);
		}
		

	}
}
