package calico.plugins.palette.menuitems;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import calico.inputhandlers.InputEventInfo;
import calico.plugins.palette.PalettePlugin;
import calico.plugins.palette.iconsets.CalicoIconManager;

public class OpenPalette extends PaletteBarMenuItem {

	public OpenPalette()
	{
		super();
		this.setImage(CalicoIconManager.getIconImage("palette.open"));
	}
	
	
	@Override
	public void onClick(InputEventInfo ev) {
		JFileChooser fc = new JFileChooser(new File(File.separator+"palette"));
		fc.setFileFilter(new FileNameExtensionFilter("Calico palette file (*.cpal)", "cpal"));
		fc.showOpenDialog(null);
		File selFile = fc.getSelectedFile();
		
		if (selFile != null)
			PalettePlugin.loadPalette(selFile);

	}

}
