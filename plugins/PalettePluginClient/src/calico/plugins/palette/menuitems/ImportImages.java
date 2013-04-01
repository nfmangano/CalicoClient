package calico.plugins.palette.menuitems;

import java.io.File;

import calico.CalicoDataStore;
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
		final JFileChooser fileChooser;
		if (CalicoDataStore.lastOpenedDirectory.compareTo("") == 0)
			fileChooser = new JFileChooser();
		else
			fileChooser = new JFileChooser(CalicoDataStore.lastOpenedDirectory);
		fileChooser.setFileFilter(new ImageFileFilter());
		fileChooser.setMultiSelectionEnabled(true);
		int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			if (files.length > 0)
			{
				PalettePlugin.importImages(files);
				CalicoDataStore.lastOpenedDirectory = files[0].getPath();
			}
		}

	}
	
	class ImageFileFilter extends javax.swing.filechooser.FileFilter {
	    public boolean accept(File file) 
	    {
	    	if (file.isFile())
	    	{
    	        String filename = file.getName().toLowerCase();
    	        return filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".gif");
	    	}
	    	else
	    	{
	    		return true;
	    	}
	    }
	    
	    public String getDescription() {
	        return "*.png, *.jpg, *.gif";
	    }
	}

}
