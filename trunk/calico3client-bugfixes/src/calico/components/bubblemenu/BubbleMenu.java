package calico.components.bubblemenu;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.components.CViewportCanvas;
import calico.components.grid.CGrid;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.components.piemenu.PieMenuContainer;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.inputhandlers.InputEventInfo;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.nodes.PImage;

public class BubbleMenu {
	public static Logger logger = Logger.getLogger(BubbleMenu.class.getName());
	//Uses PieMenuButton for compatibility
	static ObjectArrayList<PieMenuButton> buttonList = new ObjectArrayList<PieMenuButton>();
	static int[] buttonPosition;
	
	private static BubbleMenuContainer bubbleContainer = null;
	
	static double DEFAULT_DEGREE_INCREMENT = 360.0/9.0;
	public static double DEG2RAD = Math.PI/180.0;
	static double START_ANGLE = -180.0;
	public static int DEFAULT_MENU_RADIUS = 45;//35
	
	//worst piece of programming right here --v 
	public static boolean isPerformingBubbleMenuAction = false;
	public static long highlightedGroup = 0l;
	public static Point lastOpenedPosition = null;
	
	//public static double menuWidth = 0;
	//public static double menuHeight = 0;
	public static PBounds activeGroupBounds;
	
	public static void displayBubbleMenu(Point location, PBounds bounds, PieMenuButton... buttons)
	{
		lastOpenedPosition = location;
		activeGroupBounds = bounds;
		//menuWidth = activeGroupBounds.getWidth() / Math.cos(Math.atan(1));
		//menuHeight = activeGroupBounds.getHeight() / Math.sin(Math.atan(1));
		//System.out.println(activeGroupBounds.getMaxX() + " " + activeGroupBounds.getMaxY());
		displayBubbleMenuArray(location, buttons);
	}
	public static void displayBubbleMenuArray(Point location, PieMenuButton[] buttons)
	{
		lastOpenedPosition = location;
		if(bubbleContainer!=null)
		{
			// Clear out the old one, then try this again
			clearMenu();
		}
		
		buttonList.addElements(0, buttons, 0, buttons.length);
		buttonPosition = new int[buttonList.size()];
		
		getIconPositions(new Point((int)activeGroupBounds.getCenterX(), (int)activeGroupBounds.getCenterY()));
		
		//adjust the location of the pie menu if it's off the screen
		/*Rectangle2D bounds = PieMenuContainer.getComputedBounds();
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
		}*/
		
		drawBubbleMenu( );
		
	}
	
	private static void drawBubbleMenu()//, PieMenuButton[] buttons)
	{
		bubbleContainer = new BubbleMenuContainer();
		bubbleContainer.setBounds(activeGroupBounds);
		
		if(CalicoDataStore.isViewingGrid){
			
		}
		else{
			CCanvasController.canvasdb.get( CCanvasController.getCurrentUUID() ).getCamera().addChild(bubbleContainer);
			CCanvasController.canvasdb.get( CCanvasController.getCurrentUUID() ).repaint();
		}

	}
	
	private static void setButtonPositions()
	{
		
	}
	
	public static void moveIconPositions(int shiftX, int shiftY)
	{
		for(int i=0;i<buttonList.size();i++)
		{
			Point pos = new Point((int)(buttonList.get(i).getBounds().getX() + shiftX), (int)(buttonList.get(i).getBounds().getY() + shiftY));
			
			buttonList.get(i).setPosition(pos);

			bubbleContainer.getChild(i).setBounds(bubbleContainer.getChild(i).getX() + shiftX, bubbleContainer.getChild(i).getY() + shiftY,
					bubbleContainer.getChild(i).getWidth(), bubbleContainer.getChild(i).getHeight());
		}
		
	}
	
	private static void getIconPositions(Point center)
	{
		//if we are in the viewport the position of the center must be scaled
		if(CalicoDataStore.isInViewPort){
			//center = CViewportCanvas.getInstance().unscalePointFromFocusedCanvas(center);
		}
		int numOfPositions = buttonList.size();
		
		//double degIncrement = Math.min(360.0 / (numOfPositions), DEFAULT_DEGREE_INCREMENT);
		double degIncrement = DEFAULT_DEGREE_INCREMENT;
		// Now we get the radius
		//int menuRadius = getMinimumRadius(DEFAULT_MENU_RADIUS,numOfPositions) + 1;
		
		double curDegree = START_ANGLE;
		
		for(int i=0;i<numOfPositions;i++)
		{
			Point pos = getButtonPoint(i, curDegree,degIncrement);
//			pos.translate(center.x-(CalicoOptions.menu.icon_size/2), center.y-(CalicoOptions.menu.icon_size/2));

			//pos.translate(center.x, center.y);
			//pos.x += 30;
			buttonList.get(i).setPosition(pos);
			
			curDegree = curDegree + degIncrement;
		}
		
	}//
	
	private static Point getButtonPoint(int index, double curDegree, double degIncrement)
	{
		int x = 13;
		int y = 13;
		int small = 10;
		int large = 35;
		
		//1
		if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.GroupSetPermanentButton") == 0)
		{
			buttonPosition[index] = 1;
			x = (int)activeGroupBounds.getMinX() - x - small;
			y = (int)activeGroupBounds.getMinY() - y + large;
		}
		//2
		else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.GroupShrinkToContentsButton") == 0)
		{
			buttonPosition[index] = 2;
			x = (int)activeGroupBounds.getMinX() - x;
			y = (int)activeGroupBounds.getMinY() - y;
		}
		//3
		else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.ListCreateButton") == 0)
		{
			buttonPosition[index] = 3;
			x = (int)activeGroupBounds.getMinX() - x + large;
			y = (int)activeGroupBounds.getMinY() - y - small;
		}
		//4
		else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.GroupMoveButton") == 0)
		{
			buttonPosition[index] = 4;
			x = (int)activeGroupBounds.getMaxX() + x - large;
			y = (int)activeGroupBounds.getMinY() - y - small;
		}
		//5
		else if (buttonList.get(index).getClass().getName().compareTo("calico.plugins.palette.SaveToPaletteButton") == 0)
		{
			buttonPosition[index] = 5;
			x = (int)activeGroupBounds.getMaxX() + x;
			y = (int)activeGroupBounds.getMinY() - y;
		}
		//6
		else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.GroupCopyDragButton") == 0)
		{
			buttonPosition[index] = 6;
			x = (int)activeGroupBounds.getMaxX() + x + small;
			y = (int)activeGroupBounds.getMinY() - y + large;
		}
		//7
		else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.GroupRotateButton") == 0)
		{
			buttonPosition[index] = 7;
			x = (int)activeGroupBounds.getMaxX() + x + small;
			y = (int)activeGroupBounds.getMaxY() + y - large;
		}
		//8
		/*else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.") == 0)
		{
			buttonPosition[index] = 8;
			x = (int)activeGroupBounds.getMaxX() + x;
			y = (int)activeGroupBounds.getMaxY() + y;
		}*/
		//9
		else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.canvas.ArrowButton") == 0)
		{
			buttonPosition[index] = 9;
			x = (int)activeGroupBounds.getMaxX() + x - large;
			y = (int)activeGroupBounds.getMaxY() + y + small;
		}
		//10
		//else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.GroupShrinkToContentsButton") == 0)
		/*{
		 *  buttonPosition[index] = 10;
			x = (int)activeGroupBounds.getMinX() - x + large;
			y = (int)activeGroupBounds.getMaxY() + y + small;
		}*/
		//11
		else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.GroupDeleteButton") == 0)
		{
			buttonPosition[index] = 11;
			x = (int)activeGroupBounds.getMinX() - x;
			y = (int)activeGroupBounds.getMaxY() + y;
		}
		//12
		else if (buttonList.get(index).getClass().getName().compareTo("calico.components.piemenu.groups.GroupDropButton") == 0)
		{
			buttonPosition[index] = 12;
			x = (int)activeGroupBounds.getMinX() - x - small;
			y = (int)activeGroupBounds.getMaxY() + y - large;
		}
		
		//System.out.println(buttonList.get(index).getClass().getName());
		
		
		
		/*double theta = (curDegree) * DEG2RAD;
		
		
		double a = menuWidth/2;
		double b = menuHeight/2;
		double hypotenuse = a * b / Math.sqrt( Math.pow(b * Math.cos(theta),2) + Math.pow(a * Math.sin(theta), 2) );
		System.out.println(hypotenuse);
		
		
		return new Point(
				(int) (Math.cos(theta) * hypotenuse) - 12,
				(int) (Math.sin(theta) * hypotenuse) - 12
		);*/
		return new Point(
				x - 12,y - 12
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
			Point pos = getButtonPoint(i,curDegree,degIncrement);
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
	
	public static void clearMenu()
	{
		bubbleContainer.removeAllChildren();
		bubbleContainer.removeFromParent();
		buttonList.clear();
		bubbleContainer = null;
		if (highlightedGroup != 0l)
		{
			if (CGroupController.exists(highlightedGroup))
				CGroupController.groupdb.get(highlightedGroup).highlight_off();
			highlightedGroup = 0l;
		}
	}
	
	public static Rectangle2D getBoundsOfButtons()
	{
		/*double lowX = java.lang.Double.MAX_VALUE, lowY = java.lang.Double.MAX_VALUE, highX = java.lang.Double.MIN_VALUE, highY = java.lang.Double.MIN_VALUE;
		
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
		
		return new Rectangle2D.Double(lowX, lowY, highX - lowX, highY - lowY);*/
		return new Rectangle2D.Double(0,0,0,0);
	}

	public static boolean checkIfCoordIsOnBubbleMenu(Point point)
	{
		if(bubbleContainer==null)
		{
			return false;
		}		
		/*if(CalicoDataStore.isInViewPort){
			Point p = CViewportCanvas.getInstance().unscalePointFromFocusedCanvas(point);						
			return bubbleContainer.getFullBounds().contains(p);
		}*/
		
		//return bubbleContainer.getFullBounds().contains(point);
		for (int i = 0; i < buttonList.size(); i++)
		{
			if (buttonList.get(i).getBounds().contains(point))
			{
				return true;
			}
		}
		return false;
	}
	
	public static void clickBubbleMenuButton(Point point, InputEventInfo ev)
	{		
		if(buttonList.size()==0)
		{
			return;
		}
		if(CalicoDataStore.isInViewPort){
			//point = CViewportCanvas.getInstance().unscalePointFromFocusedCanvas(point);
		}
		
		int numOfPositions = buttonList.size();
		//int menuRadius = getMinimumRadius(DEFAULT_MENU_RADIUS,numOfPositions) + 1;
		for(int i=0;i<buttonList.size();i++)
		{
			
			//if (getIconSliceBounds(menuRadius, numOfPositions, i).contains(point))
			if(getButton(i).checkWithinBounds(point))
			{
				CGroupController.restoreOriginalStroke = false;
				getButton(i).onClick(ev);
				//clearMenu();
				//cancel stroke restore now that the user completed an action
				return;
			}
		}
		
		
		// This allows the menus on a HOLD (so they dont go away when you release your hold)
		if(ev.getAction()==InputEventInfo.ACTION_RELEASED)
		{
			logger.trace("BubbleMenu: Ignorning mouse event because it is a RELEASE event.");
			return;
		}
		
		// TODO: If we get to this point... should we just kill the menu?
		clearMenu();
		
		ev.stop();
//		ev.getMouseEvent().consume();
		
	}
	
	public static boolean isBubbleMenuActive()
	{
		return (bubbleContainer!=null);
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
		return BubbleMenu.isPerformingBubbleMenuAction;
	}
}
