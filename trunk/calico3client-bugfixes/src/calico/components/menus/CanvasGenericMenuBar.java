package calico.components.menus;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import calico.*;
import calico.CalicoOptions.menu.menubar;
import calico.components.*;
import calico.components.grid.*;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.*;
import calico.modules.*;
import calico.networking.*;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.*;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.event.*;



public class CanvasGenericMenuBar extends PComposite
{
	public static Logger logger = Logger.getLogger(CanvasGenericMenuBar.class.getName());
	
	
	public static final int POSITION_TOP = 1;
	public static final int POSITION_BOTTOM = 2;
	
	public static final int ALIGN_LEFT = 10;
	public static final int ALIGN_RIGHT = 11;
	
	private static final long serialVersionUID = 1L;
	
	private Rectangle rect_overall = new Rectangle();
	protected Rectangle[] rect_array = new Rectangle[50];
	protected CanvasMenuButton[] button_array = new CanvasMenuButton[50];
	protected int button_array_index = 0;
	
	protected Rectangle[] text_rect_array = new Rectangle[50];
	protected CanvasTextButton[] text_button_array = new CanvasTextButton[50];
	protected int text_button_array_index = 0;
	
	//private long cuid = 0L;
	
	private int xcoord_position = 3;
	private int xcoord_position_right_aligned = -3;
	
	private int icon_padding = 4;
	
	private int position = CanvasGenericMenuBar.POSITION_BOTTOM;
	
	
	private Rectangle screenBounds = null;

	
	
	public CanvasGenericMenuBar(int position, Rectangle screenBounds)
	{
		this.position = position;
		xcoord_position_right_aligned += screenBounds.width;
		
		this.screenBounds = new Rectangle(screenBounds);
		if(CalicoDataStore.isInViewPort){
			
			this.screenBounds = new Rectangle(CViewportCanvas.getInstance().getBounds());
		}
		icon_padding = CalicoOptions.menu.menubar.padding;
		
		
		if(this.position==CanvasGenericMenuBar.POSITION_TOP)
		{
			//CalicoOptions.menu.menubar.padding
			this.rect_overall = new Rectangle(0,0,this.screenBounds.width,menubar.defaultIconDimension+menubar.iconBuffer*2);
		}
		else if(this.position==CanvasGenericMenuBar.POSITION_BOTTOM)
		{
			this.rect_overall = new Rectangle(0, this.screenBounds.height-(menubar.defaultIconDimension+menubar.iconBuffer*2),this.screenBounds.width,menubar.defaultIconDimension+menubar.iconBuffer*2);
		}
		
		setBounds(this.rect_overall);
		setPaint( CalicoOptions.menu.menubar.background_color );
	}
	public CanvasGenericMenuBar(int position)
	{		
		this(position, new Rectangle(0,0,CalicoDataStore.ScreenWidth, CalicoDataStore.ScreenHeight));		
	}
	public CanvasGenericMenuBar()
	{
		this(CanvasGenericMenuBar.POSITION_BOTTOM);
	}
	
	public void addGap(int size)
	{
		xcoord_position = xcoord_position + size;
	}
	
	public void addSpacer()
	{
		addSpacer(ALIGN_LEFT);
	}
	
	public void addSpacer(int align)
	{
		//xcoord_position = xcoord_position + 24;
		addRightCap(align);
		//xcoord_position = xcoord_position + 5;
		addLeftCap(align);
	}
	
	public void addSpacerRightAligned()
	{
		
	}
	
	public Rectangle addIcon()
	{
		return addIcon(menubar.defaultIconDimension);
	}
	
	public Rectangle addIcon(Rectangle rect)
	{
		return addIcon(rect.width);
	}
	
	public Rectangle addIcon(int width)
	{
		int xpos = xcoord_position;
		
		Rectangle temp = addIcon(width, xpos);
		
		xcoord_position = xcoord_position + (temp.width);//+icon_padding);
		return temp;
	}
	
	public Rectangle addIconRightAligned(int width)
	{
		int xpos_rightAligned = xcoord_position_right_aligned - width;
		
		Rectangle temp = addIcon(width, xpos_rightAligned);
		
		xcoord_position_right_aligned -= width;
		return temp;
	}
	
	private Rectangle addIcon(int width, int xpos) {
		Rectangle temp = null;
		if(this.position==CanvasGenericMenuBar.POSITION_BOTTOM)
		{
			temp = new Rectangle(xpos,this.screenBounds.height-(menubar.defaultIconDimension + menubar.iconBuffer),width,menubar.defaultIconDimension);
		}
		else if(this.position==CanvasGenericMenuBar.POSITION_TOP)
		{
			temp = new Rectangle(xpos, menubar.iconBuffer, width, menubar.defaultIconDimension);
		}
		return temp;
	}
	
	public void addIcon(CanvasMenuButton icon)
	{
		rect_array[button_array_index] = addIcon();
		button_array[button_array_index] = icon;
		button_array[button_array_index].setBounds(rect_array[button_array_index]);
		
		
		addChild(0,button_array[button_array_index]);
		button_array_index++;
	}
	
	public void addIconRightAligned(CanvasMenuButton icon)
	{
		rect_array[button_array_index] = addIconRightAligned(menubar.defaultIconDimension);
		button_array[button_array_index] = icon;
		button_array[button_array_index].setBounds(rect_array[button_array_index]);
		
		
		addChild(0,button_array[button_array_index]);
		button_array_index++;
	}
	
	
	public boolean isPointInside(Point point)
	{
		return rect_overall.contains(point);
	}
	
	
	public PImage addText(String text, Font font, CanvasTextButton buttonHandler)
	{
		Image img = getTextImage(text,font);
		
		Rectangle temp = addIcon(img.getWidth(null));
		
		PImage img2 = new PImage();
		
		img2.setImage(img);
		
		img2.setBounds(temp);

		text_rect_array[text_button_array_index] = temp;

		text_button_array[text_button_array_index] = buttonHandler;
		text_button_array_index++;
		
		addChild(0,img2);
		
		return img2;
	}
	
	public PImage addTextRightAligned(String text, Font font, CanvasTextButton buttonHandler)
	{
		Image img = getTextImage(text,font);
		
		Rectangle temp = addIconRightAligned(img.getWidth(null));
		
		PImage img2 = new PImage();
		
		img2.setImage(img);
		
		img2.setBounds(temp);

		text_rect_array[text_button_array_index] = temp;

		text_button_array[text_button_array_index] = buttonHandler;
		text_button_array_index++;
		
		addChild(0,img2);
		
		return img2;
	}
	
	public void addText(String text, Font font) {
		addText(text,font,null);
	}
	public void addText(String text)
	{
		addText(text, new Font("Monospaced", Font.BOLD, 14), null);
	}
	public void addText(String text, CanvasTextButton buttonHandler)
	{
		addText(text, new Font("Monospaced", Font.BOLD, 14), buttonHandler);
	}
	
	public void addLeftCap()
	{
		addLeftCap(ALIGN_LEFT);
	}
	
	public void addLeftCap(int align)
	{
		try
		{
			Rectangle temp = (align == ALIGN_LEFT)?addIcon(2):addIconRightAligned(2);
			
			PImage img = new PImage();
			//this is using a sprite and scaling it
			img.setImage(CalicoIconManager.getImagePart(CalicoIconManager.getIconImage("menu.button_bg"),
				menubar.defaultIconDimension+1,
				0,
				4,
				menubar.defaultIconDimension
			) );
			
			img.setBounds(temp);
			addChild(0,img);
			//button_array_index++;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//getImagePart
	}
	
	public void addRightCap()
	{
		addRightCap(ALIGN_LEFT);
	}
	
	public void addRightCap(int align)
	{
		try
		{
			
			Rectangle temp = (align == ALIGN_LEFT)?addIcon(2):addIconRightAligned(2);
			
			PImage img = new PImage();
			
			img.setImage(CalicoIconManager.getImagePart(CalicoIconManager.getIconImage("menu.button_bg"),
				menubar.defaultIconDimension,
				0,
				4,
				menubar.defaultIconDimension
			) );
			
			img.setBounds(temp);
			addChild(0,img);
			//button_array_index++;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//getImagePart
	}	
	
	/**
	 * They clicked the menu, this should process which button they pushed
	 * @param point
	 */
	public void clickMenu(Point point)
	{
		for(int i=0;i<button_array_index;i++)
		{
			if(rect_array[i].contains(point))
			{
				button_array[i].actionMouseClicked();
				return;
			}
		}
		if(text_button_array_index>0) {
			for(int i=0;i<text_button_array_index;i++)
			{
				if(text_rect_array[i].contains(point))
				{
					text_button_array[i].actionMouseClicked(text_rect_array[i]);
					return;
				}
			}
		}
	}
	
	
	
	public static Image getTextImage(String text, Font font)
	{
		Image background = CalicoIconManager.getIconImage("menu.button_bg");
		//button_bg_black.png = 88x66 px
		BufferedImage bgBuf = new BufferedImage(88,66, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bgBuf.getGraphics();
		g.setBackground(new Color(83,83,83));
		g.drawImage(background, null, null);
		//the 110 might be arbitrary
		BufferedImage finalImage = new BufferedImage(110, menubar.defaultSpriteSize, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g2 = (Graphics2D) finalImage.getGraphics();
		g2.setBackground(new Color(83,83,83));
		//g.drawImage(this.background, null, null);

		g2.setFont(font);
		
		FontMetrics fontmetrics = g2.getFontMetrics(font);
		Rectangle2D strbounds = fontmetrics.getStringBounds(text, g2);
		
		int offset = (int) Math.ceil(Math.abs(strbounds.getY()));
		
	//	logger.debug("FONT BOUNDS: "+strbounds.toString()+" | "+fontmetrics.stringWidth(text));
		
		
		for(int i=0;i<6;i++)
		{
			g2.drawImage(bgBuf.getSubimage(0,0, menubar.defaultSpriteSize,menubar.defaultSpriteSize), i*menubar.defaultSpriteSize,0,null);//, null, null);
			//g2.translate(i*22, 0);
		}
		//g2.translate(0, 0);
		g2.setPaint(Color.BLACK);
		//g2.drawString(text, 0, offset);
		FontRenderContext frc = g2.getFontRenderContext();
		   TextLayout layout = new TextLayout(text, font, frc);
		   layout.draw(g2, (float)0, (float)offset);
		
		//g2.drawImage(img,  3, 3, null);
		
		return (Image)finalImage.getSubimage(0, 0, (int)strbounds.getWidth(), (int)strbounds.getHeight());
		
	}
	
	
		
}
