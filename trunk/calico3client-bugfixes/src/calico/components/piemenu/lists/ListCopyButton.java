package calico.components.piemenu.lists;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class ListCopyButton extends PieMenuButton
{
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
	private long list_uuid = 0L;
	
	public ListCopyButton(long uuid)
	{
		super("group.copy");

		list_uuid = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		ev.stop();

		CGroupController.setCopyUUID(list_uuid);
		//System.out.println("CLICKED GROUP DROP BUTTON");
		//CGroupController.drop(group_uuid);
	}
}
