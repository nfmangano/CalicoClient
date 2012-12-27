package calico.plugins.iip.components.piemenu;

import calico.components.piemenu.PieMenuButton;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;

/**
 * Simple button for deleting a link. The layout is reconstructed from the set of CICs and arrows on every iteration, so
 * it does not need to be specifically updated.
 * 
 * @author Byron Hawkins
 */
public class DeleteLinkButton extends PieMenuButton
{
	private CCanvasLink link = null;

	public DeleteLinkButton()
	{
		// "" creates a big red X
		super(CalicoIconManager.getIconImage(""));
	}

	public void setContext(CCanvasLink link)
	{
		this.link = link;
	}

	@Override
	public void onClick()
	{
		if (link == null)
		{
			System.out.println("Warning: delete link button displayed without having been prepared with a link!");
			return;
		}

		long targetCanvas = link.getAnchorB().getCanvasId();
		long root = CIntentionCellController.getInstance().getClusterRootCanvasId(targetCanvas);
		
		
		System.out.println("Delete the link from canvas #" + link.getAnchorA().getCanvasId() + " to canvas #" + link.getAnchorB().getCanvasId());
		CCanvasLinkController.getInstance().deleteLink(link.getId(), false);
		CCanvasLinkController.getInstance().createLink(root, targetCanvas);
		
	}
}
