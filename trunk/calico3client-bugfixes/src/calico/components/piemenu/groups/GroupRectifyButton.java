package calico.components.piemenu.groups;

import calico.Calico;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class GroupRectifyButton extends PieMenuButton
{
	
	long uuid;
	
	public GroupRectifyButton(long u)
	{
		super("group.rectify");
		uuid = u;
	}
	
	public void onClick(InputEventInfo ev)
	{
		CGroupController.rectify(uuid);
		ev.stop();
		
		Calico.logger.debug("CLICKED GROUP RECTIFY BUTTON");
	}
}
