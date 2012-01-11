package calico.components.piemenu.groups;

import calico.Calico;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class GroupDeleteButton extends PieMenuButton
{
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_CREATE | PieMenuButton.SHOWON_SCRAP_MENU;
	
	long uuidToBeDeleted;
	
	public GroupDeleteButton(long uuid)
	{
		super("group.delete");
		uuidToBeDeleted = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		super.onClick(ev);
		CGroupController.delete(uuidToBeDeleted);
		ev.stop();
		
		Calico.logger.debug("CLICKED GROUP DELETE BUTTON");
	}
}
