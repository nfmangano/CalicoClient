package calico.plugins.palette;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.*;
import calico.inputhandlers.*;
import calico.networking.*;
import calico.networking.netstuff.*;
import calico.plugins.palette.iconsets.*;

public class SaveToPaletteButton extends PieMenuButton
{
	private long group_uuid = 0L;
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
	
	
	public SaveToPaletteButton(long uuid)
	{
		super("plugins.designminders.send");
		


		group_uuid = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		ev.stop();
		
		PalettePlugin.addGroupToPalette(PalettePlugin.getActivePaletteUUID(), group_uuid);
	}
}
