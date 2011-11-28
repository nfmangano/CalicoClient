package calico.plugins.palette.menuitems;

import calico.plugins.palette.*;
import calico.plugins.palette.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import calico.events.*;
import calico.networking.netstuff.*;

public class HideMenuBarIcons extends PaletteBarMenuItem {

	public HideMenuBarIcons()
	{
		super();
		this.setImage(CalicoIconManager.getIconImage("palette.showmenu"));
	}
	
	@Override
	public void onClick(InputEventInfo ev) {
		CalicoEventHandler.getInstance().fireEvent(PaletteNetworkCommands.PALETTE_HIDE_MENU_BAR_ICONS, CalicoPacket.getPacket(PaletteNetworkCommands.PALETTE_HIDE_MENU_BAR_ICONS));

	}

}
