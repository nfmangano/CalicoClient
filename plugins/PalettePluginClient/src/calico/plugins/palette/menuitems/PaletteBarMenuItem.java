package calico.plugins.palette.menuitems;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import edu.umd.cs.piccolo.nodes.PImage;

import calico.CalicoOptions.menu.menubar;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.palette.*;
import calico.plugins.palette.iconsets.CalicoIconManager;

public abstract class PaletteBarMenuItem extends PImage {
	
	private Image background = null;
	
	private BufferedImage bgBuf = null;
	private int buttonBorder = 5;
	
	public PaletteBarMenuItem()
	{
		this.background = calico.iconsets.CalicoIconManager.getIconImage("menu.button_bg");
		
		bgBuf = new BufferedImage(88,66, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bgBuf.getGraphics();
		g.setBackground(new Color(83,83,83));
		g.drawImage(this.background, null, null);
	}
	
	public abstract void onClick(InputEventInfo ev);

	public void setImage(Image img)
	{
		int width = PalettePlugin.PALETTE_ITEM_WIDTH;
		int height = PalettePlugin.PALETTE_ITEM_HEIGHT;
		BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g = (Graphics2D) finalImage.getGraphics();
		g.setBackground(new Color(83,83,83));
		//g.drawImage(this.background, null, null);

		g.drawImage(this.bgBuf.getSubimage(0,0, menubar.defaultSpriteSize,menubar.defaultSpriteSize).getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH), null, null);

		
		BufferedImage unscaledImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D gUnscaledImage = (Graphics2D)unscaledImage.getGraphics();
		gUnscaledImage.drawImage(img, 0, 0, null);
		
		g.drawImage(unscaledImage.getScaledInstance(width-buttonBorder*2, height-buttonBorder*2, BufferedImage.SCALE_SMOOTH),  buttonBorder, buttonBorder, null);
		super.setImage((Image)finalImage);
		
		this.invalidatePaint();
		//this.foreground.setImage(img);
	}
	
}
