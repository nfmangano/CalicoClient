package calico.components.bubblemenu;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

import calico.CalicoDataStore;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CGroupController;
import calico.inputhandlers.InputEventInfo;
import calico.perspectives.CalicoPerspective;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.util.PBounds;

public class BubbleMenu {
	public static Logger logger = Logger.getLogger(BubbleMenu.class.getName());
	//Uses PieMenuButton for compatibility
	static ObjectArrayList<PieMenuButton> buttonList = new ObjectArrayList<PieMenuButton>();
	static int[] buttonPosition;
	
	private static BubbleMenuContainer bubbleContainer = null;
	private static BubbleMenuHighlighter bubbleHighlighter = null;
	
	//worst piece of programming right here --v 
	public static boolean isPerformingBubbleMenuAction = false;
	public static long activeGroup = 0l;
	public static Point lastOpenedPosition = null;
	
	//bounds of the active group
	public static long highlightedParentGroup = 0l;
	public static PBounds activeGroupBounds;
	
	public static int selectedButtonIndex = -1;
	
	private static PActivity fade;
	
	public static void displayBubbleMenu(Point location, Long uuid, PieMenuButton... buttons)
	{
		if(bubbleContainer!=null)
		{
			// Clear out the old one, then try this again
			clearMenu();
		}
		activeGroup = uuid;
		activeGroupBounds =  CGroupController.groupdb.get(activeGroup).getBounds();
		
		//displayBubbleMenuArray(location, buttons);
		
		lastOpenedPosition = location;
		
		buttonList.addElements(0, buttons, 0, buttons.length);
		buttonPosition = new int[buttonList.size()];
		
		getIconPositions();
		
		drawBubbleMenu( );
	}
	
	/*public static void displayBubbleMenuArray(Point location, PieMenuButton[] buttons)
	{
		
		
	}*/
	
	private static void drawBubbleMenu()//, PieMenuButton[] buttons)
	{
		bubbleContainer = new BubbleMenuContainer();
		bubbleContainer.setTransparency(0);
		bubbleHighlighter = new BubbleMenuHighlighter();
		updateContainerBounds();
		
		if(CalicoPerspective.Active.showBubbleMenu(bubbleHighlighter, bubbleContainer)){
			fade = new PActivity(500,70, System.currentTimeMillis()) {
				long step = 0;
	      
			    protected void activityStep(long time) {
			            super.activityStep(time);

			            bubbleContainer.setTransparency(1.0f * step/5);
			            
	//		            repaint();
			            step++;
			            
			            if (step > 5)
			            	terminate();
			    }
			    
			    protected void activityFinished() {
			    		bubbleContainer.setTransparency(1.0f);
			    }
			};
			// Must schedule the activity with the root for it to run.
			bubbleContainer.getRoot().addActivity(fade);
			
			//CCanvasController.canvasdb.get( CCanvasController.getCurrentUUID() ).repaint();
		}

	}
	
	
	public static void moveIconPositions(PBounds groupBounds)
	{
		for(int i=0;i<buttonList.size();i++)
		{
			Point pos = getButtonPointFromPosition(buttonPosition[i], groupBounds);
			buttonList.get(i).setPosition(pos);

			bubbleContainer.getChild(i).setBounds(pos.getX(), pos.getY(),
					bubbleContainer.getChild(i).getWidth(), bubbleContainer.getChild(i).getHeight());	
			
		}
		updateHighlighterPosition(selectedButtonIndex);
	}
	
	private static void updateHighlighterPosition(int buttonNumber)
	{
		if (buttonNumber != -1)
		{
			bubbleHighlighter.setX(buttonList.get(buttonNumber).getBounds().getMinX() - 5);
			bubbleHighlighter.setY(buttonList.get(buttonNumber).getBounds().getMinY() - 5);
		}
	}
	
	private static void getIconPositions()
	{
		for(int i=0;i<buttonList.size();i++)
		{
			buttonPosition[i] = getButtonPosition(buttonList.get(i).getClass().getName());
			Point pos = getButtonPointFromPosition(buttonPosition[i], activeGroupBounds);

			buttonList.get(i).setPosition(pos);
		}
		
	}
	
	public static void setSelectedButton(int buttonNumber)
	{
		selectedButtonIndex = buttonNumber;
		if (bubbleHighlighter != null)
		{
			if (selectedButtonIndex != -1)
			{
				updateHighlighterPosition(selectedButtonIndex);
				
			}
			bubbleHighlighter.repaintFrom(bubbleHighlighter.getBounds(), null);
		}
	}
	
	private static int getButtonPosition(String className)
	{		
		if (className.compareTo("calico.components.piemenu.groups.GroupSetPermanentButton") == 0)
		{
			return 1;
		}
		else if (className.compareTo("calico.components.piemenu.groups.GroupShrinkToContentsButton") == 0)
		{
			return 2;
		}
		else if (className.compareTo("calico.components.piemenu.groups.ListCreateButton") == 0)
		{
			return 3;
		}
		else if (className.compareTo("calico.plugins.palette.SaveToPaletteButton") == 0)
		{
			return 4;
		}
		else if (className.compareTo("calico.components.piemenu.groups.GroupMoveButton") == 0)
		{
			return 5;
		}
		else if (className.compareTo("calico.components.piemenu.groups.GroupCopyDragButton") == 0)
		{
			return 6;
		}
		else if (className.compareTo("calico.components.piemenu.groups.GroupRotateButton") == 0)
		{
			return 7;
		}
		else if (className.compareTo("calico.components.piemenu.groups.GroupResizeButton") == 0)
		{
			return 8;
		}
		else if (className.compareTo("calico.components.piemenu.canvas.ArrowButton") == 0)
		{
			return 9;
		}
		//else if (className.compareTo("calico.components.piemenu.groups.GroupShrinkToContentsButton") == 0)
		/*{
		    return 10;
		}*/
		else if (className.compareTo("calico.components.piemenu.groups.GroupDeleteButton") == 0)
		{
			return 11;
		}
		else if (className.compareTo("calico.components.piemenu.groups.GroupDropButton") == 0)
		{
			return 12;
		}
		
		return 0;
		
	}
	
	private static Point getButtonPointFromPosition(int position, PBounds groupBounds)
	{
		//Minimum screen position. T
		int screenX = 32;
		int screenYTop = 0;
		int screenYBottom = 32;
		
		//Determines position of left and right buttons in each quadrant
		int small = 10;
		int large = 35;
		
		//subtract 12 because x,y represents center of button
		int centerOffset = 12;
		
		int iconSize = 24;
		int gap = 10;
		
		//Determines diagonal distance of bubble menu buttons away from the scrap
		int startX = 13;
		int startY = 13;
		
		int farSideDistance = iconSize + small + large + gap;
		
		if (groupBounds.getWidth() < farSideDistance)
		{
			startX += (farSideDistance - groupBounds.getWidth()) / 2;
		}
		if (groupBounds.getHeight() < 80)
		{
			startY += (farSideDistance - groupBounds.getHeight()) / 2;
		}
		
		int minX, minY, maxX, maxY;
		
		int x = 0;
		int y = 0;
		
		int screenHeight = CalicoDataStore.ScreenHeight;
		int screenWidth = CalicoDataStore.ScreenWidth;
		
		switch(position)
		{
		case 1: x = (int)groupBounds.getMinX() - startX - centerOffset - small;
				y = (int)groupBounds.getMinY() - startY - centerOffset + large;
				minX = screenX;
				minY = screenYTop + small + large;
				maxX = screenWidth - screenX - iconSize - farSideDistance - large - small;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance;
			break;
		case 2: x = (int)groupBounds.getMinX() - startX - centerOffset;
				y = (int)groupBounds.getMinY() - startY - centerOffset;
				minX = screenX + small;
				minY = screenYTop + small;
				maxX = screenWidth - screenX - iconSize - farSideDistance - large;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance - large;
			break;
		case 3: x = (int)groupBounds.getMinX() - startX - centerOffset + large;
				y = (int)groupBounds.getMinY() - startY - centerOffset - small;
				minX = screenX + small + large;
				minY = screenYTop;
				maxX = screenWidth - screenX - iconSize - farSideDistance;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance - large - small;
			break;
		case 4: x = (int)groupBounds.getMaxX() + startX - centerOffset - large;
				y = (int)groupBounds.getMinY() - startY - centerOffset - small;
				minX = screenX + farSideDistance;
				minY = screenYTop;
				maxX = screenWidth - screenX - iconSize - small - large;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance - large - small;
			break;
		case 5: x = (int)groupBounds.getMaxX() + startX - centerOffset;
				y = (int)groupBounds.getMinY() - startY - centerOffset;
				minX = screenX + farSideDistance + large;
				minY = screenYTop + small;
				maxX = screenWidth - screenX - iconSize - small;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance - large;
			break;
		case 6: x = (int)groupBounds.getMaxX() + startX - centerOffset + small;
				y = (int)groupBounds.getMinY() - startY - centerOffset + large;
				minX = screenX + farSideDistance + large + small;
				minY = screenYTop + small + large;
				maxX = screenWidth - screenX - iconSize;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance;
			break;
		case 7: x = (int)groupBounds.getMaxX() + startX - centerOffset + small;
				y = (int)groupBounds.getMaxY() + startY - centerOffset - large;
				minX = screenX + farSideDistance + large + small;
				minY = screenYTop + farSideDistance;
				maxX = screenWidth - screenX - iconSize;
				maxY = screenHeight - screenYBottom - iconSize - small - large;
			break;
		case 8: x = (int)groupBounds.getMaxX() + startX - centerOffset;
				y = (int)groupBounds.getMaxY() + startY - centerOffset;
				minX = screenX + farSideDistance + large;
				minY = screenYTop + farSideDistance + large;
				maxX = screenWidth - screenX - iconSize - small;
				maxY = screenHeight - screenYBottom - iconSize - small;
			break;
		case 9: x = (int)groupBounds.getMaxX() + startX - centerOffset - large;
				y = (int)groupBounds.getMaxY() + startY - centerOffset + small;
				minX = screenX + farSideDistance;
				minY = screenYTop + farSideDistance + large + small;
				maxX = screenWidth - screenX - iconSize - small - large;
				maxY = screenHeight - screenYBottom - iconSize;
			break;
		case 10: //x = (int)groupBounds.getMinX() - startX + large;
				 //y = (int)groupBounds.getMaxY() + startY + small;
				minX = screenX + small + large;
				minY = screenYTop + farSideDistance + large + small;
				maxX = screenWidth - screenX - iconSize - farSideDistance;
				maxY = screenHeight - screenYBottom - iconSize;
			break;
		case 11: x = (int)groupBounds.getMinX() - startX - centerOffset;
				 y = (int)groupBounds.getMaxY() + startY - centerOffset;
				 minX = screenX + small;
				 minY = screenYTop + farSideDistance + large;
				 maxX = screenWidth - screenX - iconSize - farSideDistance - large;
				 maxY = screenHeight - screenYBottom - iconSize - small;
			break;
		case 12: x = (int)groupBounds.getMinX() - startX - centerOffset - small;
				 y = (int)groupBounds.getMaxY() + startY - centerOffset - large;
				 minX = screenX;
				 minY = screenYTop + farSideDistance;
				 maxX = screenWidth - screenX - iconSize - farSideDistance - large - small;
				 maxY = screenHeight - screenYBottom - iconSize - small - large; 
			break;
		default: minX = x;
				 maxX = x;
				 minY = y;
				 maxY = y;
			break;
		}
		
		if (x < minX)
			x = minX;
		else if (x > maxX)
			x=maxX;
		if (y < minY)
			y = minY;
		else if (y > maxY)
			y = maxY;
		

		return new Point(
				x,y
		);
	}
	
	
	public static void clearMenu()
	{
		if (fade.isStepping())
		{
			fade.terminate();
		}
		bubbleContainer.removeAllChildren();
		bubbleContainer.removeFromParent();
		bubbleHighlighter.removeFromParent();
		buttonList.clear();
		buttonPosition = null;
		bubbleContainer = null;
		bubbleHighlighter = null;
		selectedButtonIndex = -1;
		if (activeGroup != 0l)
		{
			if (CGroupController.exists(activeGroup))
			{
				CGroupController.groupdb.get(activeGroup).highlight_off();
				CGroupController.groupdb.get(activeGroup).highlight_repaint();
			}
			activeGroup = 0l;
		}
	}
	
	public static void updateContainerBounds()
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
		
		
		bubbleContainer.setBounds(new Rectangle2D.Double(lowX, lowY, highX - lowX, highY - lowY));
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
		
		int numOfPositions = buttonList.size();
		//int menuRadius = getMinimumRadius(DEFAULT_MENU_RADIUS,numOfPositions) + 1;
		if(ev.getAction()==InputEventInfo.ACTION_PRESSED)
		{
			for(int i=0;i<buttonList.size();i++)
			{
				
				//if (getIconSliceBounds(menuRadius, numOfPositions, i).contains(point))
				if(getButton(i).checkWithinBounds(point))
				{
					CGroupController.restoreOriginalStroke = false;
					
					setSelectedButton(i);
					getButton(i).onPressed(ev);
					
					//For compatibility with plugin buttons until they are modified
					getButton(i).onClick(ev);
					
					//setSelectedButton(0);
					//clearMenu();
					//cancel stroke restore now that the user completed an action
					return;
				}
			}
		}
		else if (ev.getAction()==InputEventInfo.ACTION_RELEASED)
		{
			for(int i=0;i<buttonList.size();i++)
			{
				
				//if (getIconSliceBounds(menuRadius, numOfPositions, i).contains(point))
				if(getButton(i).checkWithinBounds(point) && i == selectedButtonIndex)
				{
					getButton(i).onReleased(ev);
					
				}
			}
			setSelectedButton(-1);
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
	
	public static PBounds getContainerBounds()
	{
		return bubbleContainer.getBounds();
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
	public static boolean performingBubbleMenuAction()
	{
		return BubbleMenu.isPerformingBubbleMenuAction;
	}
}
