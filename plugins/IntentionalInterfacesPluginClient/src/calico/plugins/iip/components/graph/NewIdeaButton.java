package calico.plugins.iip.components.graph;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionGraphController;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class NewIdeaButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;

	public NewIdeaButton()
	{
		this(0L);
	}
	
	/**
	 * Invoked via reflection in CanvasStatusBar
	 */
	public NewIdeaButton(long canvas_uuid)
	{
		try
		{
			setImage(CalicoIconManager.getIconImage("intention.new-idea"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void actionMouseClicked()
	{
		long newIdeaCanvas = IntentionGraphController.getInstance().getNearestEmptyCanvas();
		CIntentionCellController.getInstance().setInUse(CIntentionCellController.getInstance().getCellByCanvasId(newIdeaCanvas).getId(), true);
		
		if (CanvasPerspective.getInstance().isActive())
		{
			CCanvasController.loadCanvas(newIdeaCanvas);
		}
	}
}
