package calico.components.piemenu.groups;

import calico.Calico;
import calico.CalicoOptions;
import calico.components.*;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.components.piemenu.SetArrowColorButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;


public class GroupColorPickerButton extends PieMenuButton
{
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;	
	private boolean isActive = false;
	private Timer menuTimer = null;	
	private long uuid = 0L;
	
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
			PieMenu.displayPieMenu(point, 
					new GroupSetColorButton(uuid,Color.BLACK),
					new GroupSetColorButton(uuid,Color.RED),
					new GroupSetColorButton(uuid,Color.GREEN),
					new GroupSetColorButton(uuid,Color.BLUE),
					new GroupSetColorButton(uuid,Color.GRAY),
					new GroupSetColorButton(uuid,Color.ORANGE),
					new GroupSetColorButton(uuid,Color.YELLOW),
					new GroupSetColorButton(uuid,Color.MAGENTA)
			);
			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).repaint();
			this.cancel();
		}
	
	}
	
	public GroupColorPickerButton(long uid)
	{
		//super(CalicoIconManager.getIcon(icon));
		super("color.changecolor");
		uuid = uid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		ev.stop();
		
		menuTimer = new Timer("MenuTimer",true);
		menuTimer.schedule(new MenuTimer(ev.getGlobalPoint(),uuid), 100);
		

		
//		CGroupController.set_color(uuid,color);
//		CGroupController.set_children_color(uuid,color);
	}
}
