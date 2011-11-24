package calico.components.piemenu.groups;

import calico.Calico;
import calico.components.CGroup;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.*;

public class GroupDrawButton extends PieMenuButton
{
	
	long uuidToBeMoved = 0L;
	
	public GroupDrawButton(long uuid)
	{
		super("group.draw");
		uuidToBeMoved = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		//DO SOMETHING COOL

		CGroupController.groupdb.get(uuidToBeMoved).setRightClickMode( CGroup.RIGHTCLICK_MODE_DRAWGROUP );
		
		ev.stop();
		
		Calico.logger.debug("CLICKED GROUP DRAW BUTTON +SET");
	}
}
