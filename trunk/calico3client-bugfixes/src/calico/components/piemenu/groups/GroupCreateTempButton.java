package calico.components.piemenu.groups;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;

import calico.Calico;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.InputEventInfo;

public class GroupCreateTempButton extends PieMenuButton
{
	private Timer menuTimer = null;
	public static int SHOWON = 0;
	long guuid;
	
	public GroupCreateTempButton(long uuid)
	{
		super("group.temp");
		guuid = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		super.onClick(ev);
		if (CGroupController.exists(guuid))
		{
			CGroupController.set_permanent(guuid, true);
			ev.stop();
		}
		else if (CStrokeController.exists(guuid))
		{
			long new_uuid = Calico.uuid();
			CStrokeController.makeScrap(guuid, new_uuid);
			ev.stop();
			menuTimer = new Timer("MenuTimer",true);
			menuTimer.schedule(new MenuTimer(ev.getPoint(),new_uuid), 100);			
		}

		
		Calico.logger.debug("CLICKED GROUP PERM BUTTON");
	}
	
	private class MenuTimer extends TimerTask
	{
		private Point point = null;
		private long uuid = 0L;
		public MenuTimer(Point p, long uid)
		{
			point = p;
			uuid = uid;
		}
		
		public void run()
		{
			CGroupController.show_group_piemenu(uuid, point);
			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).repaint();
			this.cancel();
		}
	
	}
}
