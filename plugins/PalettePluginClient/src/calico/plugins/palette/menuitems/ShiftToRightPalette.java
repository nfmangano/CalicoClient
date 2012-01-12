package calico.plugins.palette.menuitems;

import calico.inputhandlers.InputEventInfo;
import calico.plugins.palette.PalettePlugin;
import calico.plugins.palette.iconsets.CalicoIconManager;

public class ShiftToRightPalette extends PaletteBarMenuItem {
	
	public ShiftToRightPalette()
	{
		this.setImage(CalicoIconManager.getIconImage("palette.down"));
	}
	

	@Override
	public void onClick(InputEventInfo ev) {
		PalettePlugin.shiftVisisblePaletteRight();
	}
	
}
