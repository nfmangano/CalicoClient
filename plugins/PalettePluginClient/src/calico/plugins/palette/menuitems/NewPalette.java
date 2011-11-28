package calico.plugins.palette.menuitems;

import calico.plugins.palette.*;
import calico.plugins.palette.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import calico.*;

public class NewPalette extends PaletteBarMenuItem {

	public NewPalette()
	{
		super();
		this.setImage(CalicoIconManager.getIconImage("palette.new"));
	}
	
	@Override
	public void onClick(InputEventInfo ev) {
		long newPaletteUUID = Calico.uuid();
		PalettePlugin.addPalette(newPaletteUUID);
		PalettePlugin.setActivePalette(newPaletteUUID);
	}

}
