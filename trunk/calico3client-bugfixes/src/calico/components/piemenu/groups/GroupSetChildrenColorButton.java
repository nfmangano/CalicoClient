package calico.components.piemenu.groups;

import calico.Calico;
import calico.CalicoOptions;
import calico.components.*;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import java.awt.*;


public class GroupSetChildrenColorButton extends PieMenuButton
{
	private Color color = null;
	private long uuid = 0L;
	
	public GroupSetChildrenColorButton(long uid, Color col)
	{
		//super(CalicoIconManager.getIcon(icon));
		super(CalicoOptions.getColorImage(col));
		uuid = uid;
		color = col;
	}
	
	public void onClick(InputEventInfo ev)
	{
		ev.stop();
		
		CGroupController.set_children_color(uuid,color);
	}
}
