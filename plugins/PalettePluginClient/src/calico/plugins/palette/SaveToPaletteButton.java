package calico.plugins.palette;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.*;
import calico.inputhandlers.*;
import calico.networking.*;
import calico.networking.netstuff.*;
import calico.plugins.palette.iconsets.*;

public class SaveToPaletteButton extends PieMenuButton
{
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
	private boolean isActive = false;
	
	public SaveToPaletteButton(long uuid)
	{
		super("plugins.designminders.send");
		


		this.uuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
		if (!CGroupController.exists(uuid) || isActive)
		{
			return;
		}
		
		isActive = true;
	}
	
	public void onReleased(InputEventInfo ev)
	{
		ev.stop();
		
		PalettePlugin.addGroupToPalette(PalettePlugin.getActivePaletteUUID(), uuid);
		isActive = false;
	}
	
}
