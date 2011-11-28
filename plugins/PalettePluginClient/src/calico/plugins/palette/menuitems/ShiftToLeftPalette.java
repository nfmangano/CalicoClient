package calico.plugins.palette.menuitems;

import calico.inputhandlers.InputEventInfo;
import calico.plugins.palette.*;
import calico.plugins.palette.iconsets.CalicoIconManager;

import calico.events.*;
import calico.networking.netstuff.*;

public class ShiftToLeftPalette extends PaletteBarMenuItem {
	
	public ShiftToLeftPalette()
	{
		this.setImage(calico.iconsets.CalicoIconManager.getIconImage("arrow.left"));
	}
	
	@Override
	public void onClick(InputEventInfo ev) {
		PalettePlugin.shiftVisiblePaletteLeft();
	}

}
