package calico.plugins.iip.components.canvas;

import calico.components.menus.CanvasMenuButton;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.perspectives.IntentionalInterfacesPerspective;

/**
 * Simple button which changes the current view to the Intention View.
 *
 * @author Byron Hawkins
 */
public class ShowIntentionGraphButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private final long canvasId;

	/**
	 * Instantiated via reflection in CanvasStatusBar
	 */
	public ShowIntentionGraphButton(long canvasId)
	{
		this.canvasId = canvasId;
		
		try
		{
			setImage(CalicoIconManager.getIconImage("intention.to-intention-graph"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void actionMouseClicked()
	{
		IntentionalInterfacesPerspective.getInstance().displayPerspective(canvasId);
	}
}
