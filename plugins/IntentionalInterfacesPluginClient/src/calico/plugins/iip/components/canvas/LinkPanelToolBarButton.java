package calico.plugins.iip.components.canvas;

import calico.components.menus.CanvasMenuButton;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class LinkPanelToolBarButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;

	private final long canvas_uuid;

	/**
	 * Instantiated via reflection in CanvasStatusBar
	 */
	public LinkPanelToolBarButton(long canvas_uuid)
	{
		this.canvas_uuid = canvas_uuid;

		try
		{
			setImage(CalicoIconManager.getIconImage("intention.link-panel"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void actionMouseClicked()
	{
		IntentionCanvasController.getInstance().toggleLinkPanelVisibility();
	}
}
