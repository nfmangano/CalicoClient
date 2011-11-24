package calico.components.piemenu.groups;

import calico.Calico;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.*;

public class GroupDuplicateButton extends PieMenuButton
{
	
	long uuidToBeDeleted;
	
	public GroupDuplicateButton(long uuid)
	{
		super("group.copy");
		uuidToBeDeleted = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_DUPLICATE, uuidToBeDeleted));
		ev.stop();
		
		Calico.logger.debug("CLICKED GROUP DUPLICATE BUTTON");
	}
}
