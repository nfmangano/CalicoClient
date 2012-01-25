package calico.components.bubblemenu;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import calico.CalicoDataStore;
import calico.components.CViewportCanvas;
import calico.components.piemenu.PieMenu;
import calico.controllers.CCanvasController;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.*;

public class BubbleMenuContainer extends PComposite {
	public BubbleMenuContainer()
	{
		int buttons = BubbleMenu.getButtonCount();
		for(int i=0;i<buttons;i++)
		{
			addChild( BubbleMenu.getButton(i).getPImage() );
		}
	}
	
	@Override
	public boolean setBounds(Rectangle2D rect)
	{
		//return super.setBounds(getComputedBounds());
		return super.setBounds(rect);
	}
	
	
	/**
	 * This paints the pie slices
	 */
	protected void paint(PPaintContext paintContext)
	{
		
	}
}
