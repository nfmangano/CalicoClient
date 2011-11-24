package calico.components.piemenu.groups;

import calico.Calico;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class GroupCirclifyButton extends PieMenuButton
{
	
	long uuid;
	
	public GroupCirclifyButton(long u)
	{
		super("group.circlify");
		uuid = u;
	}
	
	public void onClick(InputEventInfo ev)
	{
		CGroupController.circlify(uuid);
		ev.stop();
		
		Calico.logger.debug("CLICKED GROUP circlify BUTTON");
	}
}
