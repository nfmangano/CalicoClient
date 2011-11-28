package calico.plugins.palette.menuitems;

import calico.events.CalicoEventHandler;
import calico.inputhandlers.InputEventInfo;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.plugins.palette.PaletteNetworkCommands;
import calico.plugins.palette.iconsets.CalicoIconManager;

public class ShowMenuBarIcons extends PaletteBarMenuItem {

	public ShowMenuBarIcons()
	{
		super();
		this.setImage(CalicoIconManager.getIconImage("palette.hidemenu"));
	}
	
	@Override
	public void onClick(InputEventInfo ev) {
		CalicoEventHandler.getInstance().fireEvent(PaletteNetworkCommands.PALETTE_SHOW_MENU_BAR_ICONS, CalicoPacket.getPacket(PaletteNetworkCommands.PALETTE_HIDE_MENU_BAR_ICONS));
	}

}
