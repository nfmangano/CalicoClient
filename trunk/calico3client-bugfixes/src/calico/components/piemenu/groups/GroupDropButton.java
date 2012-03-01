package calico.components.piemenu.groups;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.CGroupController;
import calico.inputhandlers.InputEventInfo;

public class GroupDropButton extends PieMenuButton
{
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
	private long group_uuid = 0L;
	private boolean isActive = false;
	
	public GroupDropButton(long uuid)
	{
		super("group.drop");

		group_uuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
		if (!CGroupController.exists(group_uuid) || isActive)
		{
			return;
		}
		
		isActive = true;
		
		super.onPressed(ev);
	}
	
	public void onReleased(InputEventInfo ev)
	{
		//super.onClick(ev);
		ev.stop();
		//System.out.println("CLICKED GROUP DROP BUTTON");
		CGroupController.drop(group_uuid);
		
		isActive = false;
	}
	
}
