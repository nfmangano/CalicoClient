package calico.plugins.iip.components.canvas;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.plugins.iip.controllers.CanvasPerspectiveController;

public class CanvasIntentionToolBarButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private final long canvas_uuid;
	
	private final CanvasIntentionToolBar toolbar;

	/**
	 * Instantiated via reflection in CanvasStatusBar
	 */
	public CanvasIntentionToolBarButton(long canvas_uuid)
	{
		this.canvas_uuid = canvas_uuid;
		
		toolbar = new CanvasIntentionToolBar(canvas_uuid);
		CCanvasController.canvasdb.get(canvas_uuid).getCamera().addChild(toolbar.toolbar);
		
		try
		{
			// TODO: it may not work to use the client's icon manager
			setImage(CalicoIconManager.getIconImage("intention.canvas-menubar"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		CanvasPerspectiveController.getInstance().canvasIntentionToolBarCreated(toolbar);
	}

	public void actionMouseClicked()
	{
		toolbar.toolbar.setVisible(!toolbar.toolbar.getVisible());
	}
}
