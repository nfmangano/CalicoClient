package calico.components.piemenu.groups;

import calico.Calico;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class GroupConvexHullButton extends PieMenuButton
{

	public static int SHOWON = 0;
	long guuid;
	
	public GroupConvexHullButton(long uuid)
	{
		super("group.convexhull");
		guuid = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		if (CGroupController.exists(guuid))
		{
			CGroupController.set_permanent(guuid, true);
			CGroupController.shrinkToConvexHull(guuid);
		}
		else if (CStrokeController.exists(guuid))
		{
//			long groupUUID = CStrokeController.makeScrap(guuid);
//			CGroupController.shrinkToConvexHull(groupUUID);
		}
		ev.stop();
		
		Calico.logger.debug("CLICKED CONVEX HULL BUTTON");
	}
}
