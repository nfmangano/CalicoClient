package calico.plugins.palette.menuitems;

import calico.inputhandlers.InputEventInfo;
import calico.plugins.palette.*;
import calico.plugins.palette.iconsets.CalicoIconManager;

import calico.events.*;
import calico.networking.netstuff.*;

public class ShiftToLeftPalette extends PaletteBarMenuItem {
	
	public ShiftToLeftPalette()
	{
		this.setImage(CalicoIconManager.getIconImage("palette.up"));
	}
	
	@Override
	public void onClick(InputEventInfo ev) {
		PalettePlugin.shiftVisiblePaletteLeft();
	}

}
