package calico.plugins.palette.menuitems;

import java.io.File;
import calico.plugins.palette.*;
import calico.plugins.palette.iconsets.CalicoIconManager;

import javax.swing.JFileChooser;

import calico.inputhandlers.InputEventInfo;

public class ImportImages extends PaletteBarMenuItem {

	public ImportImages()
	{
		super();
		this.setImage(CalicoIconManager.getIconImage("palette.images"));
	}
	
	@Override
	public void onClick(InputEventInfo ev) {
		JFileChooser fileChooser = new JFileChooser("File Dialog");
		fileChooser.setFileFilter(new ImageFileFilter());
		fileChooser.setMultiSelectionEnabled(true);
		int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			PalettePlugin.importImages(files);
		}

	}
	
	class ImageFileFilter extends javax.swing.filechooser.FileFilter {
	    public boolean accept(File file) {
	        String filename = file.getName().toLowerCase();
	        return filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".gif");
	    }
	    public String getDescription() {
	        return "*.png, *.jpg, *.gif";
	    }
	}

}
