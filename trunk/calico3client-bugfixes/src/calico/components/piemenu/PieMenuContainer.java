package calico.components.piemenu;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.*;

import calico.inputhandlers.*;

import calico.*;
import calico.components.*;
import calico.controllers.CCanvasController;

/**
 * This contains all the PieMenuButtons and handles them.
 * @author mdempsey
 *
 */
public class PieMenuContainer extends PComposite
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int circleBuffer = 20;
	
	public PieMenuContainer()
	{
		int buttons = PieMenu.getButtonCount();
		for(int i=0;i<buttons;i++)
		{
			addChild( PieMenu.getButton(i).getPImage() );
		}
	}
	
	@Override
	public boolean setBounds(Rectangle2D rect)
	{

		return super.setBounds(getComputedBounds());
	}
	
	public static Rectangle2D getComputedBounds()
	{
		double radius = PieMenu.getMinimumRadius(PieMenu.DEFAULT_MENU_RADIUS, PieMenu.buttonList.size());
//		double edge = Math.max(rect.getWidth(), rect.getHeight());
		
		Point center = PieMenu.lastOpenedPosition;
		
		double newX = center.x - radius;
		double newY = center.y - radius;
		
		
		
		Rectangle2D rectWithPadding = new Rectangle2D.Double(newX - circleBuffer, newY - circleBuffer, radius * 2 + circleBuffer*2, radius * 2 + circleBuffer*2);
		return rectWithPadding;
	}
	
	/**
	 * This paints the pie slices
	 */
	protected void paint(PPaintContext paintContext)
	{
		Graphics2D graphics = (Graphics2D)paintContext.getGraphics();
		graphics.setStroke(new BasicStroke(1.0f));
		double degIncrement = Math.min(360.0 / (PieMenu.buttonList.size()), PieMenu.DEFAULT_DEGREE_INCREMENT);
		int max = (int)(360.0 / degIncrement);
		Rectangle2D buttonBounds = this.getBounds();
		Rectangle circleBounds = new Rectangle((int)buttonBounds.getX(), (int)buttonBounds.getY(), (int)buttonBounds.getWidth(), (int)buttonBounds.getHeight());
		
		double innerRadius = circleBounds.getWidth()/6;
		Ellipse2D.Double circle = new Ellipse2D.Double(circleBounds.getX() + circleBounds.getWidth()/2 - innerRadius, circleBounds.getY() + circleBounds.getHeight()/2 - innerRadius,
														innerRadius * 2, innerRadius * 2);
		
		RadialGradientPaint rgp = new RadialGradientPaint((float)circleBounds.getCenterX(), (float)circleBounds.getCenterY(), (float)circleBounds.width/2, new float[] { 0.0f, 0.8f, 0.95f, 1.0f }, new Color[] {Color.white, new Color(205, 201, 201), new Color(205, 201, 201), Color.gray});
		for (int i = 0; i < max; i++)
		{
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
			graphics.setPaint(rgp);
			Arc2D.Double arc = new Arc2D.Double(circleBounds, PieMenu.START_ANGLE + degIncrement*i, degIncrement, Arc2D.PIE);
			
			Area arcArea = new Area(arc);
			arcArea.subtract(new Area(circle));
			graphics.fill(arcArea);
			graphics.setPaint(Color.gray);
			graphics.draw(arcArea);
		}
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		
	}
	
	
	
}
