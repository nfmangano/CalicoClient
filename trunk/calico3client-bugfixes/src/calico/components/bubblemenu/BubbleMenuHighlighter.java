package calico.components.bubblemenu;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import calico.CalicoOptions;

import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PComposite;

public class BubbleMenuHighlighter extends PComposite {
	public static int halo_buffer = 12;
	public static int halo_size = CalicoOptions.menu.icon_size + halo_buffer;
	
	public BubbleMenuHighlighter()
	{
		setBounds(0,0,halo_size,halo_size);
	}
	
	
	protected void paint(PPaintContext paintContext)
	{
		Graphics2D graphics = (Graphics2D)paintContext.getGraphics();
		graphics.setStroke(new BasicStroke(1.0f));
		
		if (BubbleMenu.selectedButtonIndex != -1)
		{
			//Rectangle2D buttonBounds = BubbleMenu.buttonList.get(BubbleMenu.selectedButtonIndex).getBounds();
			
			Ellipse2D.Double halo = new Ellipse2D.Double(getBounds().getX(), getBounds().getY(), getBounds().getWidth(), getBounds().getHeight());
			
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			
			graphics.setColor(new Color(255,196,121));
			graphics.fill(halo);
			//graphics.setPaint(new Color(243,179,97));
			graphics.setPaint(Color.gray);
			graphics.draw(halo);
		}
			
	}
}
