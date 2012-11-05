package calico.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import calico.CalicoDraw;
import calico.controllers.CCanvasController;

public class CanvasViewScrap extends CGroupImage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long targetCanvasUUID = 0l;
	private int lastSignature = 0;
	public static final int ROUNDED_RECTANGLE_OVERFLOW = 4;
	public static final int CELL_MARGIN = 6;
	private final double INITIAL_SIZE_RATIO = 0.33d;
	
	public CanvasViewScrap(long uuid, long cuid, long targetCanvas)
	{
		super(uuid, cuid, CCanvasController.image(targetCanvas));
		this.targetCanvasUUID = targetCanvas;
		
		
		int width = (int)(calico.CalicoDataStore.serverScreenWidth * INITIAL_SIZE_RATIO);
		int height = (int)(calico.CalicoDataStore.serverScreenHeight * INITIAL_SIZE_RATIO);
		
		Rectangle bounds = new Rectangle(200, 200, width, height);
		setShapeToRoundedRectangle(bounds, 0);
		
		setImage();
	}
	
//	public CanvasViewScrap(long uuid, long cuid, long puid, Image img,
//			int imgX, int imgY, int imageWidth, int imageHeight) {
//		super(uuid, cuid, puid, img, imgX, imgY, imageWidth, imageHeight);
//		// TODO Auto-generated constructor stub
//		
//		
//	}
	
	//set the image from the other canvases
	public void setImage()
	{
		render();
	}
	
	public void updateCell()
	{
		//render if changed
		int sig = CCanvasController.get_signature(targetCanvasUUID);
		if(sig!=this.lastSignature)
		{			
			setImage();
		}
	}
	
	public void render()
	{
		Rectangle tBounds = this.getPathReference().getBounds();
		Image img = CCanvasController.image(targetCanvasUUID, tBounds.width, tBounds.height);
//		Image img = getCanvasImage(canvasUID);
		this.lastSignature = CCanvasController.get_signature(targetCanvasUUID);
		
		Rectangle rect = new Rectangle(0, 0, tBounds.width, tBounds.height);
		RoundRectangle2D rRect = new RoundRectangle2D.Double(2, 2, tBounds.width-ROUNDED_RECTANGLE_OVERFLOW, tBounds.height-ROUNDED_RECTANGLE_OVERFLOW, 8, 8);
		Area border = new Area(rect);
		border.subtract(new Area(rRect));

		BufferedImage bimg = new BufferedImage(tBounds.width, tBounds.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D)bimg.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(img, 2, 2, tBounds.width, tBounds.height,null);
		g.setColor(Color.green);
		g.fill(border);
		g.setColor(new Color(100,100,100));
		g.draw(rRect);
		
//		if (CCanvasController.canvasdb.get(canvasUID).getLockValue())
//		{
//			g.setStroke(new BasicStroke(4));
//		}
		
		g.setColor(Color.black);
		g.drawRoundRect(	2,//new Double(getBounds().x).intValue() + 2,
							2,//new Double(getBounds().y).intValue() + 2,
							tBounds.width - ROUNDED_RECTANGLE_OVERFLOW,
							tBounds.height - ROUNDED_RECTANGLE_OVERFLOW,
							10,
							10);
		g.dispose();
		
		for(int i = 0; i < bimg.getHeight(); i++)
		{
			for(int j = 0; j < bimg.getWidth(); j++)
			{
				if(bimg.getRGB(j, i) == Color.green.getRGB())
				{
					//bimg.setRGB(j, i, 0x8F1C1C);
					bimg.setRGB(j, i, 0xFFFFFF);
				}
			}
		}
		
		image = bimg;
//		setImage( bimg );
		//setImage( img );

//		setBounds( tBounds.getX(),tBounds.getY(),tBounds.getWidth(),tBounds.getHeight());
		//CalicoDraw.setNodeBounds(this, tBounds.getX(),tBounds.getY(),tBounds.getWidth(),tBounds.getHeight());

		//CalicoDraw.repaint(this);
//		CalicoDataStore.gridObject.repaint();
		
//		updatePresenceText();
//		updateCanvasLockIcon();
	}/////
	
	@Override
	public void recomputeBounds()
	{
		super.recomputeBounds();
		setImage();
//		image = CCanvasController.image(this.targetCanvasUUID);
		CalicoDraw.repaint(this);
	}
	
	
	
	
	
	

}
