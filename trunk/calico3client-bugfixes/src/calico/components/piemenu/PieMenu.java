package calico.components.piemenu;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Random;

import org.apache.log4j.Logger;

import calico.*;
import calico.components.*;
import calico.components.grid.CGrid;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;
import calico.inputhandlers.*;

import it.unimi.dsi.fastutil.objects.*;

public class PieMenu
{
	public static Logger logger = Logger.getLogger(PieMenu.class.getName());
	static double DEFAULT_DEGREE_INCREMENT = 360.0/9.0;
	public static double DEG2RAD = Math.PI/180.0;
	static double START_ANGLE = -180.0;
	public static int DEFAULT_MENU_RADIUS = 45;//35
	
	
	//private static PieMenuButton[] buttonList = null;
	
	static ObjectArrayList<PieMenuButton> buttonList = new ObjectArrayList<PieMenuButton>();
	
	private static PieMenuContainer pieContainer = null;
	
	//worst piece of programming right here --v 
	public static boolean isPerformingPieMenuAction = false;
	public static long highlightedGroup = 0l;
	public static Point lastOpenedPosition = null;
	
	
	// TODO: The pie menu must maintain a list of the current menu objects on display.
	// When one is called, then we call that button's onClick method.
	
	/**
	 * Users can call this and specify a point, and the array of buttons to be displayed
	 * @param location
	 * @param buttons
	 */
	public static void displayPieMenu(Point location, PieMenuButton... buttons)
	{
		displayPieMenuArray(location, buttons);
	}
	public static void displayPieMenuArray(Point location, PieMenuButton[] buttons)
	{
		lastOpenedPosition = location;
		if(pieContainer!=null)
		{
			// Clear out the old one, then try this again
			clearMenu();
		}
		
		buttonList.addElements(0, buttons, 0, buttons.length);
		getIconPositions(location);
		
		//adjust the location of the pie menu if it's off the screen
		Rectangle2D bounds = PieMenuContainer.getComputedBounds();
		Rectangle2D screenBounds = new Rectangle2D.Double(0d,0d, CalicoDataStore.ScreenWidth, CalicoDataStore.ScreenHeight);
		if (!screenBounds.contains(bounds))
		{
			if (bounds.getX() < 0)
				location.translate((int)(bounds.getX() * -1), 0);
			if (bounds.getY() < 0)
				location.translate(0, (int)(bounds.getY() * -1));
			if (bounds.getX() + bounds.getWidth() > screenBounds.getWidth())
				location.translate( (int)(screenBounds.getWidth() - bounds.getX() - bounds.getWidth()), 0);
			if (bounds.getY() + bounds.getHeight() > screenBounds.getHeight())
				location.translate(0, (int)(screenBounds.getHeight() - bounds.getY() - bounds.getHeight()));
			
			getIconPositions(location);
		}
		
		drawPieMenu( );
		
	}
	
	private static void drawPieMenu()//, PieMenuButton[] buttons)
	{
		pieContainer = new PieMenuContainer();
		pieContainer.setBounds(getBoundsOfButtons());
		
		if(CalicoDataStore.isViewingGrid){
			CGrid.getInstance().getCamera().addChild(pieContainer);
			CGrid.getInstance().getCamera().repaintFrom(pieContainer.getBounds(), pieContainer);
		}
		else{
			CCanvasController.canvasdb.get( CCanvasController.getCurrentUUID() ).getCamera().addChild(pieContainer);
			CCanvasController.canvasdb.get( CCanvasController.getCurrentUUID() ).repaint();
		}

	}
	
	
	private static void getIconPositions(Point center)
	{
		//if we are in the viewport the position of the center must be scaled
		if(CalicoDataStore.isInViewPort){
			center = CViewportCanvas.getInstance().unscalePointFromFocusedCanvas(center);
		}
		int numOfPositions = buttonList.size();
		
		double degIncrement = Math.min(360.0 / (numOfPositions), DEFAULT_DEGREE_INCREMENT);
		
		// Now we get the radius
		int menuRadius = getMinimumRadius(DEFAULT_MENU_RADIUS,numOfPositions) + 1;
		
		double curDegree = START_ANGLE;
		
		for(int i=0;i<numOfPositions;i++)
		{
			Point pos = getButtonPoint(curDegree,degIncrement,menuRadius);
//			pos.translate(center.x-(CalicoOptions.menu.icon_size/2), center.y-(CalicoOptions.menu.icon_size/2));
			pos.translate(center.x, center.y);
						
			buttonList.get(i).setPosition(pos);
			
			curDegree = curDegree + degIncrement;
		}
		
	}//
	
	public static Rectangle2D getBoundsOfButtons()
	{
		double lowX = java.lang.Double.MAX_VALUE, lowY = java.lang.Double.MAX_VALUE, highX = java.lang.Double.MIN_VALUE, highY = java.lang.Double.MIN_VALUE;
		
		Rectangle bounds;
		for (PieMenuButton button : buttonList)
		{
			bounds = button.bounds;
			if (lowX > bounds.x)
				lowX = bounds.x;
			if (lowY > bounds.y)
				lowY = bounds.y;
			if (highX < bounds.x + bounds.width)
				highX = bounds.x + bounds.width;
			if (highY < bounds.y + bounds.height)
				highY = bounds.y + bounds.height;
		}
		
		return new Rectangle2D.Double(lowX, lowY, highX - lowX, highY - lowY);
	}
	
	private static Point getButtonPoint(double curDegree, double degIncrement, int menuRadius)
	{
		double a = (curDegree + degIncrement/2.0) * DEG2RAD;
		
		return new Point(
				((int) (0/2 + Math.cos(a)*menuRadius)) - ((int)Math.round(menuRadius/4)),
				((int) (0/2 + Math.sin(a)*menuRadius)) - ((int)Math.round(menuRadius/4))
		);
	}
	
	static int getMinimumRadius(int startRadius, int numButtons)
	{
		if(doIconsOverlap(startRadius, numButtons))
		{
			return getMinimumRadius(startRadius+1,numButtons);
		}
		else
		{
			return startRadius;
		}
	}
	
	private static boolean doIconsOverlap(int radius, int numButtons)
	{
		Rectangle[] buttonBounds = new Rectangle[numButtons];
		
		double degIncrement = Math.min(360.0 / (numButtons), DEFAULT_DEGREE_INCREMENT);
	
		double curDegree = START_ANGLE;
		
		for(int i=0;i<numButtons;i++)
		{
			Point pos = getButtonPoint(curDegree,degIncrement,radius);
			buttonBounds[i] = new Rectangle(pos.x,pos.y,CalicoOptions.menu.icon_size,CalicoOptions.menu.icon_size);
			
			curDegree = curDegree + degIncrement;
		}
		
		for(int i=0;i<numButtons;i++)
		{
			for(int j=0;j<numButtons;j++)
			{
				if(i!=j && buttonBounds[i].intersects(buttonBounds[j]))
				{
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	private static Shape getIconSliceBounds(int radius, int numButtons, int buttonNumber)
	{
		double degIncrement = Math.min(360.0 / (numButtons), DEFAULT_DEGREE_INCREMENT);
		
		Arc2D.Double arc = new Arc2D.Double(pieContainer.getBounds(), PieMenu.START_ANGLE - degIncrement*(buttonNumber+1), degIncrement, Arc2D.PIE);
		
		return arc;
	}
	
	
	
	public static void clearMenu()
	{
		pieContainer.removeAllChildren();
		pieContainer.removeFromParent();
		buttonList.clear();
		pieContainer = null;
		if (highlightedGroup != 0l)
		{
			if (CGroupController.exists(highlightedGroup))
			{
				CGroupController.groupdb.get(highlightedGroup).highlight_off();
				CGroupController.groupdb.get(highlightedGroup).highlight_repaint();
			}
			
			highlightedGroup = 0l;
		}
	}
	
	public static boolean checkIfCoordIsOnPieMenu(Point point)
	{
		if(pieContainer==null)
		{
			return false;
		}		
		if(CalicoDataStore.isInViewPort){
			Point p = CViewportCanvas.getInstance().unscalePointFromFocusedCanvas(point);						
			return pieContainer.getFullBounds().contains(p);
		}
		return pieContainer.getFullBounds().contains(point);
	}
	
	public static void clickPieMenuButton(Point point, InputEventInfo ev)
	{		
		if(buttonList.size()==0)
		{
			return;
		}
		if(CalicoDataStore.isInViewPort){
			point = CViewportCanvas.getInstance().unscalePointFromFocusedCanvas(point);
		}
		
		int numOfPositions = buttonList.size();
		int menuRadius = getMinimumRadius(DEFAULT_MENU_RADIUS,numOfPositions) + 1;
		for(int i=0;i<buttonList.size();i++)
		{
			if (getIconSliceBounds(menuRadius, numOfPositions, i).contains(point))
//			if(getButton(i).checkWithinBounds(point))
			{
				CGroupController.restoreOriginalStroke = false;
				getButton(i).onClick(ev);
				clearMenu();
				//cancel stroke restore now that the user completed an action
				return;
			}
		}
		
		
		// This allows the menus on a HOLD (so they dont go away when you release your hold)
		if(ev.getAction()==InputEventInfo.ACTION_RELEASED)
		{
			logger.trace("PieMenu: Ignorning mouse event because it is a RELEASE event.");
			return;
		}
		
		// TODO: If we get to this point... should we just kill the menu?
		clearMenu();
		
		ev.stop();
//		ev.getMouseEvent().consume();
		
	}
	
	public static boolean isPieMenuActive()
	{
		return (pieContainer!=null);
	}
	
	static int getButtonCount()
	{
		return buttonList.size();
	}
	static PieMenuButton getButton(int i)
	{
		return buttonList.get(i);
	}
	
	public static boolean performingPieMenuAction()
	{
		return PieMenu.isPerformingPieMenuAction;
	}
	
	
}
