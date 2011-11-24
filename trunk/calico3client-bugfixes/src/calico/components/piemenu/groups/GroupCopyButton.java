package calico.components.piemenu.groups;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class GroupCopyButton extends PieMenuButton
{
	public static int SHOWON = 0; //PieMenuButton.SHOWON_SCRAP_MENU;
	private long group_uuid = 0L;
	
	public GroupCopyButton(long uuid)
	{
		super("grid.canvas.copy");

		group_uuid = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		ev.stop();

		CGroupController.setCopyUUID(group_uuid);
		//System.out.println("CLICKED GROUP DROP BUTTON");
		//CGroupController.drop(group_uuid);
	}
}
