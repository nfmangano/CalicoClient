package calico.components.menus;

import java.awt.*;

import edu.umd.cs.piccolo.nodes.PImage;

public abstract class MenuBarButtonCluster
{
	private CanvasGenericMenuBar menubar = null;
	protected long canvas_uuid = 0L;
	
	public MenuBarButtonCluster(CanvasGenericMenuBar menubar, long canvas_uuid)
	{
		this.menubar = menubar;
		this.canvas_uuid = canvas_uuid;
	} 
	
	public abstract void display();
	
	
	
	protected final void addSpacer(){this.menubar.addSpacer();}
	
	protected final Rectangle addIcon(){return this.menubar.addIcon(22);}
	
	protected final Rectangle addIcon(Rectangle rect){return this.menubar.addIcon(rect.width);}
	protected final Rectangle addIcon(int width){return this.menubar.addIcon(width);}
	
	protected final void addIcon(CanvasMenuButton icon){this.menubar.addIcon(icon);}
	
	
	protected final void addText(String text, Font font){this.menubar.addText(text,font);}
	protected final void addText(String text){this.menubar.addText(text);}
}
