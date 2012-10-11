package calico.components.piemenu.groups;

import java.awt.Rectangle;

import javax.swing.JOptionPane;

import calico.CalicoDraw;
import calico.components.CList;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.inputhandlers.InputEventInfo;

public class GroupTextButton extends PieMenuButton
{
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
	private boolean isActive = false;
	
	public GroupTextButton(long uuid)
	{
		super("group.text");

		this.uuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
		if (!CGroupController.exists(uuid) || isActive)
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
//		CGroupController.drop(uuid);
		String text = CGroupController.groupdb.get(uuid).getText();
		String response = JOptionPane.showInputDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent(),
				  "Create Scrap with Text",
				  text
				  /*,
				  JOptionPane.QUESTION_MESSAGE*/);
		if (response != null)
		{
			CGroupController.groupdb.get(uuid).setText(response);
			Rectangle rect = CGroupController.groupdb.get(uuid).getBoundsOfContents();			
			if (!(CGroupController.groupdb.get(uuid) instanceof CList))
				CGroupController.makeRectangle(uuid, rect.x, rect.y, rect.width, rect.height);			
			CGroupController.set_text(uuid, response);
			
			CalicoDraw.repaint(CGroupController.groupdb.get(uuid));
		}
		
		isActive = true;
	}
	
}
