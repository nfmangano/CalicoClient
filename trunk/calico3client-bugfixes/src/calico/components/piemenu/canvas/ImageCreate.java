package calico.components.piemenu.canvas;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import calico.Calico;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.grid.CGrid;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CImageController;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

public class ImageCreate extends PieMenuButton {

	public static int SHOWON = PieMenuButton.SHOWON; //PieMenuButton.SHOWON_SCRAP_MENU;
	long uuid = 0L;
	
	public ImageCreate()
	{
		super("canvas.image");		
	}
	
	public ImageCreate(long u)
	{
		super("canvas.image");
		this.uuid = u;
	}
	
	public void onClick(InputEventInfo ev)
	{
		super.onClick(ev);
		
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new ImageFileFilter());
        int returnVal = fc.showOpenDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()));

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            Networking.send(CImageController.getImageTransferPacket(Calico.uuid(), CCanvasController.getCurrentUUID(), 
            		BubbleMenu.lastOpenedPosition.x, BubbleMenu.lastOpenedPosition.y, file));
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
