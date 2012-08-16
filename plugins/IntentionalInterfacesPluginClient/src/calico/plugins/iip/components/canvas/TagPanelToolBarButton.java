package calico.plugins.iip.components.canvas;

import calico.components.menus.CanvasMenuButton;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;

/**
 * Simple button to show the tag panel. This feature is obsolete.
 * 
 * @author Byron Hawkins
 */
public class TagPanelToolBarButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;

	private final long canvas_uuid;

	/**
	 * Instantiated via reflection in CanvasStatusBar
	 */
	public TagPanelToolBarButton(long canvas_uuid)
	{
		this.canvas_uuid = canvas_uuid;

		try
		{
			setImage(CalicoIconManager.getIconImage("intention.tag-panel"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// CanvasPerspectiveController.getInstance().canvasIntentionToolBarCreated(toolbar);
	}

	public void actionMouseClicked()
	{
		IntentionCanvasController.getInstance().toggleTagPanelVisibility();
	}
}
