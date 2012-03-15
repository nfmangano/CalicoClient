package calico.plugins.palette;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import calico.CalicoDataStore;
import calico.components.piemenu.PieMenu;
import calico.controllers.CCanvasController;
import calico.plugins.palette.iconsets.CalicoIconManager;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PImage;

public class PaletteBarItem extends PImage {
	
	private long paletteUUID;
	private long uuid;
	private Image img;
	
	public PaletteBarItem(long paletteUUID, long paletteItemUUID, Image img)
	{
		this.paletteUUID = paletteUUID;
		this.uuid = paletteItemUUID;
		BufferedImage bimage = new BufferedImage(PalettePlugin.PALETTE_ITEM_WIDTH, PalettePlugin.PALETTE_ITEM_HEIGHT, BufferedImage.TYPE_INT_ARGB);
//		this.img = img;
		
		if (img == null)
		{
			
//			try
//			{
//				img = CalicoIconManager.getIconImage("");
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
		}
		
		bimage.getGraphics().setColor(Color.white);
		bimage.getGraphics().fillRect(0, 0, PalettePlugin.PALETTE_ITEM_WIDTH, PalettePlugin.PALETTE_ITEM_HEIGHT);
		bimage.getGraphics().drawImage(img, 0, 0, null);
		setBounds(0,0,PalettePlugin.PALETTE_ITEM_WIDTH, PalettePlugin.PALETTE_ITEM_HEIGHT);
		
		setImage(bimage);
		
		
		
		this.setPaint(Color.white);
		

		
	}
	
	public long getUUID()
	{
		return uuid;
	}
	
}
