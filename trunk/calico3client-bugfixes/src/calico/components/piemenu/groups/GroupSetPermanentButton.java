package calico.components.piemenu.groups;

import java.awt.Point;
import java.awt.Polygon;

import calico.Calico;
import calico.components.CStroke;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class GroupSetPermanentButton extends PieMenuButton
{

	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_CREATE;
	long guuid;
	private boolean isActive = false;
	
	public GroupSetPermanentButton(long uuid)
	{
		super("group.perm");
		guuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
		if (!CGroupController.exists(guuid) || isActive)
		{
			return;
		}
		
		isActive = true;
		super.onPressed(ev);
	}
	
	public void onReleased(InputEventInfo ev)
	{
		//super.onClick(ev);
		if (CGroupController.exists(guuid))
		{
			CGroupController.set_permanent(guuid, true);
		}
		else if (CStrokeController.exists(guuid))
		{
			long new_uuid = Calico.uuid();
			CStrokeController.makeScrap(guuid, new_uuid);
			CGroupController.set_permanent(new_uuid, true);
		}
		
		
		ev.stop();
		
		Calico.logger.debug("CLICKED GROUP PERM BUTTON");
		
		isActive = false;
	}
	
}
