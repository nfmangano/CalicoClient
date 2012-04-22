package calico.plugins.iip.components.canvas;

import calico.components.menus.CanvasMenuButton;
import calico.components.piemenu.PieMenuButton;
import calico.plugins.iip.components.piemenu.canvas.CreateDesignInsideLinkButton;
import calico.plugins.iip.controllers.IntentionCanvasController;
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

		// CanvasPerspectiveController.getInstance().canvasIntentionToolBarCreated(toolbar);
	}

	public void actionMouseClicked()
	{
		boolean newVisibility = !CanvasIntentionToolBar.getInstance().isVisible();

		CanvasIntentionToolBar.getInstance().setVisible(newVisibility);
		IntentionCanvasController.getInstance().toggleTagVisibility();

		if (newVisibility)
		{
			CreateDesignInsideLinkButton.SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
		}
		else
		{
			CreateDesignInsideLinkButton.SHOWON = 0;
		}
	}
}
