package calico.components.piemenu.arrows;

import java.awt.*;
import java.util.*;

import calico.Calico;
import calico.components.piemenu.*;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

@Deprecated
public class ChangeArrowColorButton extends PieMenuButton
{
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
					new SetArrowColorButton(uuid,Color.RED),
					new SetArrowColorButton(uuid,Color.BLUE),
					new SetArrowColorButton(uuid,Color.GREEN),
					new SetArrowColorButton(uuid,Color.ORANGE),
					new SetArrowColorButton(uuid,Color.PINK),
					new SetArrowColorButton(uuid,Color.YELLOW),
					new SetArrowColorButton(uuid,Color.BLACK)
			);
			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).repaint();
			this.cancel();
		}
	
	}
	
	public ChangeArrowColorButton(long uid)
	{
		super("color.changecolor");
		uuid = uid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		ev.stop();
		
		menuTimer = new Timer("MenuTimer",true);
		menuTimer.schedule(new MenuTimer(ev.getGlobalPoint(),uuid), 100);
				
	}
}
