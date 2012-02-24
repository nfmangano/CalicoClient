package calico.components.piemenu.arrows;

import calico.Calico;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CArrowController;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

@Deprecated
public class ArrowDeleteButton extends PieMenuButton
{
	
	long uuid = 0L;
	
	public ArrowDeleteButton(long uid)
	{
		super("group.delete");
		uuid = uid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		CArrowController.delete(uuid);
		ev.stop();
		
	}
}
