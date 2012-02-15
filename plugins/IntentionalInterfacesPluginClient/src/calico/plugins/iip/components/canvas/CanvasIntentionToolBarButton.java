package calico.plugins.iip.components.canvas;

import calico.components.menus.CanvasMenuButton;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class CanvasIntentionToolBarButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private final long canvas_uuid;
	
	/**
	 * Instantiated via reflection in CanvasStatusBar
	 */
	public CanvasIntentionToolBarButton(long canvas_uuid)
	{
		this.canvas_uuid = canvas_uuid;
		
		try
		{
			setImage(CalicoIconManager.getIconImage("intention.canvas-menubar"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
//		CanvasPerspectiveController.getInstance().canvasIntentionToolBarCreated(toolbar);
	}

	public void actionMouseClicked()
	{
		CanvasIntentionToolBar.getInstance().setVisible(!CanvasIntentionToolBar.getInstance().isVisible());
	}
}
