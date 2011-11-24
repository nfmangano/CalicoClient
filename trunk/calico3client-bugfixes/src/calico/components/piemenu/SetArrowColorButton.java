package calico.components.piemenu;

import calico.Calico;
import calico.CalicoOptions;
import calico.controllers.CArrowController;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import java.awt.*;


public class SetArrowColorButton extends PieMenuButton
{
	private Color color = null;
	private long uuid = 0L;
	
	public SetArrowColorButton(long uid, Color col)
	{
		//super(CalicoIconManager.getIcon(icon));
		super(CalicoOptions.getColorImage(col));
		uuid = uid;
		color = col;
	}
	
	public void onClick(InputEventInfo ev)
	{
		ev.stop();
		
		//CArrowController.color(uuid,color);
	}
}
