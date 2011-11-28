package calico.plugins.palette.menuitems;

import calico.inputhandlers.InputEventInfo;
import calico.plugins.palette.*;
import calico.plugins.palette.iconsets.CalicoIconManager;

public class ClosePalette extends PaletteBarMenuItem {

	public ClosePalette()
	{
		super();
		this.setImage(CalicoIconManager.getIconImage("palette.close"));
	}
	
	@Override
	public void onClick(InputEventInfo ev) {
		PalettePlugin.deletePalette(PalettePlugin.getActivePaletteUUID());
	}

}
