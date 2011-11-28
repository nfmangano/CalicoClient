package calico.plugins.palette.menuitems;

import java.io.File;


import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import calico.plugins.palette.*;
import calico.plugins.palette.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class SavePalette extends PaletteBarMenuItem {

	public SavePalette()
	{
		super();
		this.setImage(CalicoIconManager.getIconImage("palette.save"));
	}
	
	@Override
	public void onClick(InputEventInfo ev) {
		JFileChooser fc = new JFileChooser(new File(File.separator+"palette"));
		fc.setFileFilter(new FileNameExtensionFilter("Calico palette file (*.cpal)", "cpal"));
		fc.showSaveDialog(null);
		File selFile = fc.getSelectedFile();
		if (selFile != null && selFile.toString().lastIndexOf(".cpal") < 0)
		{
			selFile = new File(selFile.getAbsolutePath() + ".cpal");
		}
			
		if (selFile != null)
			PalettePlugin.savePalette(selFile);
	}
	
	class CustomFileFilter extends javax.swing.filechooser.FileFilter {
	    public boolean accept(File file) {
	        String filename = file.getName().toLowerCase();
	        return filename.endsWith(".cpal");
	    }
	    public String getDescription() {
	        return "*.cpal (Calico palette file)";
	    }
	}

}
