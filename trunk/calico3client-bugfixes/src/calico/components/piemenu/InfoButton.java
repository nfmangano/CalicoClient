package calico.components.piemenu;

import calico.Calico;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class InfoButton extends PieMenuButton
{
	public InfoButton()
	{
		super("info");
	}
	
	public void onClick(InputEventInfo ev)
	{
		ev.stop();
		Calico.logger.debug("CLICKED INFO BUTTON");
	}
}
