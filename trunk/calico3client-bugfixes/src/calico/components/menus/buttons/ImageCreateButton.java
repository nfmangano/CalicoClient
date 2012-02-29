package calico.components.menus.buttons;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import calico.Calico;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CImageController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;

public class ImageCreateButton extends CanvasMenuButton {
	
	long cuid = 0L;
	public ImageCreateButton(long c)
	{
		super();
		this.cuid = c;
		try
		{
			setImage(CalicoIconManager.getIconImage("canvas.image"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void actionMouseClicked()
	{		
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new ImageFileFilter());
        int returnVal = fc.showOpenDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent());

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            Networking.send(CImageController.getImageTransferPacket(Calico.uuid(), CCanvasController.getCurrentUUID(), 
            		50, 50, file));
		}
//		if (this.uuid != 0l && new_uuid != 0l && CGroupController.groupdb.get(new_uuid).getParentUUID() == 0l)
//		{
//			CGroupController.move_start(new_uuid);
//			CGroupController.move_end(new_uuid, ev.getX(), ev.getY());
//		}
	}
	
	private boolean isImageURL(String text)
	{
		String regex = "((https?|ftp|gopher|telnet|file|notes|ms-help):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)+\\.(?:gif|jpg|jpeg|png|bmp|GIF|JPEG|JPG|PNG|BMP|Gif|Jpg|Jpeg|Png|Bmp)$";
		Pattern pattern = Pattern.compile(regex); 
		Matcher matcher = pattern.matcher(text); 
		return matcher.matches();
	}
	
	class ImageFileFilter extends javax.swing.filechooser.FileFilter 
	{
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
	    
	    public String getDescription() 
	    {
	        return "*.png, *.jpg, *.gif";
	    }
	}
}
