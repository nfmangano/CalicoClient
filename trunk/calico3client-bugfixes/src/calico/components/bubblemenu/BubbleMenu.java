package calico.components.bubblemenu;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import calico.CalicoDataStore;
import calico.CalicoDraw;
import calico.components.menus.ContextMenu;
import calico.CalicoOptions;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.InputEventInfo;
import calico.perspectives.CalicoPerspective;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.util.PBounds;

public class BubbleMenu {
	public static Logger logger = Logger.getLogger(BubbleMenu.class.getName());
	
	final public static int TYPE_GROUP = 1;
	final public static int TYPE_STROKE = 2;
	final public static int TYPE_CONNECTOR = 3;
	
	//Uses PieMenuButton for compatibility
	static ObjectArrayList<PieMenuButton> buttonList = new ObjectArrayList<PieMenuButton>();
	static int[] buttonPosition;
	
	private static BubbleMenuContainer bubbleContainer = null;
	private static BubbleMenuHighlighter bubbleHighlighter = null;
	
	//worst piece of programming right here --v 
	public static boolean isPerformingBubbleMenuAction = false;
	//The UUID of the component
	public static long activeUUID = 0l;
	@Deprecated
	public static Point lastOpenedPosition = null;
	
	//UUID of parent to highlight when moving
	public static long highlightedParentGroup = 0l;
	//bounds of the active component
	public static PBounds activeBounds;
	//type of active component
	public static int activeType;
	
	//Index of highlighted button
	public static int selectedButtonIndex = -1;
	
	//Reference to activity for fading in the menu
	private static PActivity fadeActivity;
	
	//Is the bubble menu visible
	private static boolean isBubbleMenuActive = false;
	
	private static final List<ContextMenu.Listener> listeners = new ArrayList<ContextMenu.Listener>();

	public static void addListener(ContextMenu.Listener listener)
	{
		listeners.add(listener);
	}
	
	public static void removeListener(ContextMenu.Listener listener)
	{
		listeners.remove(listener);
	}
	
	//Displays the bubble menu given the scrap UUID and the buttons to add.
	public static void displayBubbleMenu(Long uuid, boolean fade, int type, PieMenuButton... buttons)
	{
		//if(bubbleContainer!=null)
		boolean fadeIn = fade;
		if (isBubbleMenuActive())
		{
			// Clear out the old one, then try this again
			clearMenu();
			//Don't fade in if menu was already active.
			fadeIn = false;
		}
		activeUUID = uuid;
		activeType = type;
		
		//Unfortunately this is necessary due to existing conventions
		switch(activeType)
		{
			case TYPE_GROUP: activeBounds = CGroupController.groupdb.get(activeUUID).getBounds();
											//Highlight the active group
											CGroupController.groupdb.get(activeUUID).highlight_on();
											CGroupController.groupdb.get(activeUUID).highlight_repaint();
				break;
			case TYPE_STROKE: activeBounds = CStrokeController.strokes.get(activeUUID).getBounds();
											 CStrokeController.strokes.get(activeUUID).highlight_on();
				break;
			case TYPE_CONNECTOR: activeBounds = CConnectorController.connectors.get(activeUUID).getBounds();
												CConnectorController.connectors.get(activeUUID).highlight_on();
				break;
		}
		
		
		
		
		//Reset buttons
		buttonList.clear();
		buttonList.addElements(0, buttons, 0, buttons.length);
		buttonPosition = new int[buttonList.size()];
		
		//Set canvas position for the icons and listeners
		setIconPositions();
		
		//add Menu to canvas 
		drawBubbleMenu(fadeIn);
		
		for (ContextMenu.Listener listener : listeners)
		{
			listener.menuDisplayed(ContextMenu.BUBBLE_MENU);
		}
	}

	//Makes the menu visible by adding it to the canvas
	private static void drawBubbleMenu(boolean fade)
	{
		//Initialize container
		bubbleContainer = new BubbleMenuContainer();
		//Makes the container invisible in case a fade in is required
		bubbleContainer.setTransparency(0f);
		//Initialize the highlighter
		bubbleHighlighter = new BubbleMenuHighlighter();
		
		//Update the bounds of the menu
		updateContainerBounds();
		
		isBubbleMenuActive = true;
		
		fade = false;

		/*final boolean tempfade = fade;
		final PActivity tempActivity = fadeActivity;
		final BubbleMenuContainer tempContainer = bubbleContainer;
		final BubbleMenuHighlighter tempHighlighter = bubbleHighlighter;
		
		SwingUtilities.invokeLater(
				new Runnable() { public void run() { 
					tempContainer.setTransparency(0);
					if(CalicoPerspective.Active.showBubbleMenu(tempHighlighter, tempContainer)){
						// Must schedule the activity with the root for it to run.
						if (tempfade)
						{
							tempContainer.getRoot().addActivity(tempActivity);
						}
						else
						{
							//updateContainerBounds();
							//bubbleContainer.setTransparency(100);
							tempContainer.setTransparency(1.0f);
							tempContainer.repaintFrom(tempContainer.getBounds(), tempContainer);
						}
					}
		}});*/
		
		if(CalicoPerspective.Active.showBubbleMenu(bubbleHighlighter, bubbleContainer)){
			
			//Check if a fade in is required
			if (fade)
			{
				// Must schedule the activity with the root for it to run.
				fadeActivity = new PActivity(500,70, System.currentTimeMillis()) {
					long step = 0;
		      
				    protected void activityStep(long time) {
				            super.activityStep(time);

				            bubbleContainer.setTransparency(1.0f * step/5);
				            //CalicoDraw.setNodeTransparency(bubbleContainer, 1.0f * step/5);
				            
//				            repaint();
				            step++;
				            
				            if (step > 5)
				            	terminate();
				    }
				    
				    protected void activityFinished() {
				    	//When finished make sure the menu is fully opaque
				    	bubbleContainer.setTransparency(1.0f);
				    	//CalicoDraw.setNodeTransparency(bubbleContainer, 1.0f);
				    }
				};
				
						
				//bubbleContainer.getRoot().addActivity(fadeActivity);
				CalicoDraw.addActivityToNode(bubbleContainer, fadeActivity);
			}
			//Simply show the menu otherwise
			else
			{
				fadeActivity = null;
				//bubbleContainer.setTransparency(1.0f);
				CalicoDraw.setNodeTransparency(bubbleContainer, 1.0f);
				CalicoDraw.repaintNode(bubbleContainer);
			}
		}
	}	
	
	//Change which group the menu affects without closing and reopening the menu
	//Currently only used with copy/drag scrap. 
	//Must check for completeness before additional usage
	public static void updateGroupUUID(long uuid)
	{
		if(activeType != TYPE_GROUP)
			return;
		
		for(int i=0;i<buttonList.size();i++)
		{
			activeUUID = uuid;
			activeBounds =  CGroupController.groupdb.get(activeUUID).getBounds();
			buttonList.get(i).updateGroupUUID(uuid);
			moveIconPositions(activeBounds);
		}
	}
	
	//Update the icon buttons and listeners to fit the new bounds
	public static void moveIconPositions(PBounds componentBounds)
	{
		for(int i=0;i<buttonList.size();i++)
		{
			Point pos = getButtonPointFromPosition(i, buttonPosition[i], componentBounds);
			buttonList.get(i).setPosition(pos);

			//bubbleContainer.getChild(i).setBounds(pos.getX(), pos.getY(), CalicoOptions.menu.icon_size, CalicoOptions.menu.icon_size);
			CalicoDraw.setNodeBounds(bubbleContainer.getChild(i), pos.getX(), pos.getY(), CalicoOptions.menu.icon_size, CalicoOptions.menu.icon_size);
			
		}
		updateHighlighterPosition(selectedButtonIndex);
		updateContainerBounds();
		
		CalicoDraw.repaintNode(bubbleHighlighter);
		CalicoDraw.repaintNode(bubbleContainer);
	}
	
	//Update the highlighter location based on the current position of the button
	private static void updateHighlighterPosition(int buttonNumber)
	{
		if (buttonNumber != -1)
		{
			//bubbleHighlighter.setX(buttonList.get(buttonNumber).getBounds().getMinX() - (BubbleMenuHighlighter.halo_buffer / 2));
			//bubbleHighlighter.setY(buttonList.get(buttonNumber).getBounds().getMinY() - (BubbleMenuHighlighter.halo_buffer / 2));
			CalicoDraw.setNodeX(bubbleHighlighter, buttonList.get(buttonNumber).getBounds().getMinX() - (BubbleMenuHighlighter.halo_buffer / 2));
			CalicoDraw.setNodeY(bubbleHighlighter, buttonList.get(buttonNumber).getBounds().getMinY() - (BubbleMenuHighlighter.halo_buffer / 2));
		}
	}
	
	//Sets the initial button and listener positions
	private static void setIconPositions()
	{
		for(int i=0;i<buttonList.size();i++)
		{
			buttonPosition[i] = getButtonPosition(buttonList.get(i).getClass().getName());
			Point pos = getButtonPointFromPosition(i, buttonPosition[i], activeBounds);

			buttonList.get(i).setPosition(pos);
		}
		
	}
	
	//Sets the button to be highlighted
	public static void setSelectedButton(int buttonNumber)
	{
		selectedButtonIndex = buttonNumber;
		if (bubbleHighlighter != null)
		{
			if (selectedButtonIndex != -1)
			{
				updateHighlighterPosition(selectedButtonIndex);
				
			}
			CalicoDraw.repaintNode(bubbleHighlighter);
		}
	}
	
	//Gets the button position for each button
	//Determines where the button is placed around the group
	private static int getButtonPosition(String className)
	{		
		//Group Buttons
		if (className.compareTo("calico.components.piemenu.groups.GroupSetPermanentButton") == 0)
		{
			return 1;
		}
		if (className.compareTo("calico.components.piemenu.groups.GroupShrinkToContentsButton") == 0)
		{
			return 2;
		}
		if (className.compareTo("calico.components.piemenu.groups.ListCreateButton") == 0)
		{
			return 12;
		}
		if (className.compareTo("calico.components.piemenu.groups.GroupMoveButton") == 0)
		{
			return 5;
		}
		if (className.compareTo("calico.components.piemenu.groups.GroupCopyDragButton") == 0)
		{
			return 6;
		}
		if (className.compareTo("calico.components.piemenu.groups.GroupRotateButton") == 0)
		{
			return 7;
		}
		if (className.compareTo("calico.components.piemenu.groups.GroupResizeButton") == 0)
		{
			return 8;
		}
		if (className.compareTo("calico.components.piemenu.canvas.ArrowButton") == 0)
		{
			return 9;
		}
		if (className.compareTo("calico.components.piemenu.groups.GroupDropButton") == 0)
		{
		    return 10;
		}
		if (className.compareTo("calico.components.piemenu.groups.GroupDeleteButton") == 0)
		{
			return 11;
		}
		
		//Stroke Buttons
		if (className.compareTo("calico.components.bubblemenu.strokes.StrokeMakeConnectorButton") == 0)
		{
			return 1;
		}
		
		//Connector Buttons
		if (className.compareTo("calico.components.bubblemenu.connectors.ConnectorLinearizeButton") == 0)
		{
			return 1;
		}
		if (className.compareTo("calico.components.bubblemenu.connectors.ConnectorMakeStrokeButton") == 0)
		{
			return 2;
		}
		if (className.compareTo("calico.components.bubblemenu.connectors.ConnectorMoveHeadButton") == 0)
		{
			return 0;
		}
		
		//Palette Plugin Buttons
		if (className.compareTo("calico.plugins.palette.SaveToPaletteButton") == 0)
		{
			return 4;
		}
		//motta.lrd
		//Analysis Plugin button
		if (className.compareTo("calico.plugins.analysis.components.buttons.BubbleTagButton") == 0)
		{
			return 3;
		}
		//IIP Pluging Buttons
		if (className.compareTo("calico.plugins.iip.components.piemenu.canvas.CreateDesignInsideLinkButton") == 0)
		{
			return 12;
		}
		
		
		//User Plugin Buttons
		if (className.compareTo("calico.plugins.userlist.UserImageCreate") == 0)
		{
			return 11;
		}
		
		
		return 0;
		
	}
	
	//Determines the location of a button 
	//Called for each button any time the active component bounds changes
	//Should account for screen borders and any other restrictions for button positioning
	private static Point getButtonPointFromPosition(int buttonIndex, int position, PBounds componentBounds)
	{
		//Minimum screen position. T
		int screenX = 32;
		int screenYTop = 0;
		int screenYBottom = 32;
		
		//Determines position of left and right buttons in each quadrant
		int small = 10;
		int large = 40;
		
		//subtract 12 because x,y represents center of button
		int centerOffset = CalicoOptions.menu.icon_size / 2;
		
		int iconSize = 24;
		int gap = 20;
		
		//Determines diagonal distance of bubble menu buttons away from the scrap
		int startX = 13;
		int startY = 13;
		
		int farSideDistance = (iconSize + small + large + gap);
		
		if (componentBounds.getWidth() < farSideDistance)
		{
			startX += (farSideDistance - componentBounds.getWidth()) / 2;
		}
		if (componentBounds.getHeight() < farSideDistance)
		{
			startY += (farSideDistance - componentBounds.getHeight()) / 2;
		}
		
		int minX, minY, maxX, maxY;
		
		//Start offscreen in case a button is missing
		int x = -50;
		int y = -50;
		
		int screenHeight = CalicoDataStore.ScreenHeight;
		int screenWidth = CalicoDataStore.ScreenWidth;
		
		switch(position)
		{
		case 0: Point p = buttonList.get(buttonIndex).getPreferredPosition();
				if (p == null) 
					return new Point(x, y);
				else
					return new Point(p.x - centerOffset, p.y - centerOffset);
				
		case 1: x = (int)componentBounds.getMinX() - startX - centerOffset - small;
				y = (int)componentBounds.getMinY() - startY - centerOffset + large;
				minX = screenX;
				minY = screenYTop + small + large;
				maxX = screenWidth - screenX - iconSize - farSideDistance - large - small;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance;
			break;
		case 2: x = (int)componentBounds.getMinX() - startX - centerOffset;
				y = (int)componentBounds.getMinY() - startY - centerOffset;
				minX = screenX + small;
				minY = screenYTop + small;
				maxX = screenWidth - screenX - iconSize - farSideDistance - large;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance - large;
			break;
		case 3: x = (int)componentBounds.getMinX() - startX - centerOffset + large;
				y = (int)componentBounds.getMinY() - startY - centerOffset - small;
				minX = screenX + small + large;
				minY = screenYTop;
				maxX = screenWidth - screenX - iconSize - farSideDistance;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance - large - small;
			break;
		case 4: x = (int)componentBounds.getMaxX() + startX - centerOffset - large;
				y = (int)componentBounds.getMinY() - startY - centerOffset - small;
				minX = screenX + farSideDistance;
				minY = screenYTop;
				maxX = screenWidth - screenX - iconSize - small - large;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance - large - small;
			break;
		case 5: x = (int)componentBounds.getMaxX() + startX - centerOffset;
				y = (int)componentBounds.getMinY() - startY - centerOffset;
				minX = screenX + farSideDistance + large;
				minY = screenYTop + small;
				maxX = screenWidth - screenX - iconSize - small;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance - large;
			break;
		case 6: x = (int)componentBounds.getMaxX() + startX - centerOffset + small;
				y = (int)componentBounds.getMinY() - startY - centerOffset + large;
				minX = screenX + farSideDistance + large + small;
				minY = screenYTop + small + large;
				maxX = screenWidth - screenX - iconSize;
				maxY = screenHeight - screenYBottom - iconSize - farSideDistance;
			break;
		case 7: x = (int)componentBounds.getMaxX() + startX - centerOffset + small;
				y = (int)componentBounds.getMaxY() + startY - centerOffset - large;
				minX = screenX + farSideDistance + large + small;
				minY = screenYTop + farSideDistance;
				maxX = screenWidth - screenX - iconSize;
				maxY = screenHeight - screenYBottom - iconSize - small - large;
			break;
		case 8: x = (int)componentBounds.getMaxX() + startX - centerOffset;
				y = (int)componentBounds.getMaxY() + startY - centerOffset;
				minX = screenX + farSideDistance + large;
				minY = screenYTop + farSideDistance + large;
				maxX = screenWidth - screenX - iconSize - small;
				maxY = screenHeight - screenYBottom - iconSize - small;
			break;
		case 9: x = (int)componentBounds.getMaxX() + startX - centerOffset - large;
				y = (int)componentBounds.getMaxY() + startY - centerOffset + small;
				minX = screenX + farSideDistance;
				minY = screenYTop + farSideDistance + large + small;
				maxX = screenWidth - screenX - iconSize - small - large;
				maxY = screenHeight - screenYBottom - iconSize;
			break;
		case 10: x = (int)componentBounds.getMinX() - startX - centerOffset + large;
				 y = (int)componentBounds.getMaxY() + startY - centerOffset + small;
				 minX = screenX + small + large;
				 minY = screenYTop + farSideDistance + large + small;
				 maxX = screenWidth - screenX - iconSize - farSideDistance;
				 maxY = screenHeight - screenYBottom - iconSize;
			break;
		case 11: x = (int)componentBounds.getMinX() - startX - centerOffset;
				 y = (int)componentBounds.getMaxY() + startY - centerOffset;
				 minX = screenX + small;
				 minY = screenYTop + farSideDistance + large;
				 maxX = screenWidth - screenX - iconSize - farSideDistance - large;
				 maxY = screenHeight - screenYBottom - iconSize - small;
			break;
		case 12: x = (int)componentBounds.getMinX() - startX - centerOffset - small;
				 y = (int)componentBounds.getMaxY() + startY - centerOffset - large;
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
		

		return new Point(x,y);
	}
	
	//Removes the menu
	public static void clearMenu()
	{
		if (fadeActivity != null)
			fadeActivity.terminate(PActivity.TERMINATE_WITHOUT_FINISHING);
		
		/*SwingUtilities.invokeLater(
				new Runnable() { public void run() { 
					tempContainer.removeAllChildren();
					tempContainer.removeFromParent();
					tempHighlighter.removeFromParent();
				}});*/
		CalicoDraw.removeAllChildrenFromNode(bubbleContainer);
		CalicoDraw.removeNodeFromParent(bubbleContainer);
		CalicoDraw.removeNodeFromParent(bubbleHighlighter);
		//buttonList.clear();
		//buttonPosition = null;
		isBubbleMenuActive = false;
		//bubbleContainer = null;
		//bubbleHighlighter = null;
		selectedButtonIndex = -1;
		
		if (activeUUID != 0l)
		{
			//SwingUtilities.invokeLater(
			//		new Runnable() { public void run() { 
			if (activeType == TYPE_GROUP && CGroupController.exists(activeUUID))
			{
				CGroupController.groupdb.get(activeUUID).highlight_off();
				CGroupController.groupdb.get(activeUUID).highlight_repaint();
				if (!CGroupController.groupdb.get(BubbleMenu.activeUUID).isPermanent())
				{
					CGroupController.drop(BubbleMenu.activeUUID);
				}
			}
			else if (activeType == TYPE_STROKE && CStrokeController.exists(activeUUID))
			{
				CStrokeController.strokes.get(activeUUID).highlight_off();
			}
			else if (activeType == TYPE_CONNECTOR && CConnectorController.exists(activeUUID))
			{
				CConnectorController.connectors.get(activeUUID).highlight_off();
			}
			
			//		}});
			activeUUID = 0l;
		}
		isPerformingBubbleMenuAction = false;

		for (ContextMenu.Listener listener : listeners)
		{
			listener.menuCleared(ContextMenu.BUBBLE_MENU);
		}
	}
	
	//Updates the bounds of the menu
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
		
		
		//bubbleContainer.setBounds(new Rectangle2D.Double(lowX, lowY, highX - lowX, highY - lowY));
		CalicoDraw.setNodeBounds(bubbleContainer, new Rectangle2D.Double(lowX, lowY, highX - lowX, highY - lowY));
	}

	//Check if the point overlaps with a button's hit zone
	public static boolean checkIfCoordIsOnBubbleMenu(Point point)
	{
		//if(bubbleContainer==null)
		if (!isBubbleMenuActive())
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
			Ellipse2D.Double halo = getButtonHalo(i);
			
			if (halo.contains(point))
			{
				return true;
			}
		}
		return false;
	}
	
	//Pass the event to the button
	public static void handleButtonInput(Point point, InputEventInfo ev)
	{		
		if(buttonList.size()==0)
		{
			return;
		}
		
		if(ev.getAction()==InputEventInfo.ACTION_PRESSED)
		{
			BubbleMenu.isPerformingBubbleMenuAction = true;
			for(int i=0;i<buttonList.size();i++)
			{
				if(getButtonHalo(i).contains(point))
				{
					CGroupController.restoreOriginalStroke = false;
					
					setSelectedButton(i);
					BubbleMenu.setHaloEnabled(true);
					getButton(i).onPressed(ev);
					
					//For compatibility with plugin buttons until they are modified
					getButton(i).onClick(ev);
					
					//cancel stroke restore now that the user completed an action
					return;
				}
			}
		}
		else if (ev.getAction()==InputEventInfo.ACTION_RELEASED)
		{
			if(selectedButtonIndex != -1 && (getButton(selectedButtonIndex).draggable || getButtonHalo(selectedButtonIndex).contains(point)) )
			{
				getButton(selectedButtonIndex).onReleased(ev);
			}

			setSelectedButton(-1);
			BubbleMenu.isPerformingBubbleMenuAction = false;
			return;
		}
		else if (ev.getAction()==InputEventInfo.ACTION_DRAGGED)
		{
			if(selectedButtonIndex != -1)
			{
				if (getButton(selectedButtonIndex).draggable)
				{
					getButton(selectedButtonIndex).onDragged(ev);
				}
				else if (getButtonHalo(selectedButtonIndex).contains(point))
				{
					BubbleMenu.setHaloEnabled(true);
				}
				else
				{
					BubbleMenu.setHaloEnabled(false);
				}
				
			}
			return;
		}
		

		
		// TODO: If we get to this point... should we just kill the menu?
		clearMenu();
		
		ev.stop();
//		ev.getMouseEvent().consume();
		
	}
	
	
	public static void setHaloEnabled(boolean enable)
	{
		getButton(selectedButtonIndex).setHaloEnabled(enable);
		CalicoDraw.repaintNode(bubbleHighlighter);
	}
	
	public static Ellipse2D.Double getButtonHalo(int buttonIndex)
	{
			return new Ellipse2D.Double(buttonList.get(buttonIndex).getBounds().getMinX() - (BubbleMenuHighlighter.halo_buffer / 2),
									buttonList.get(buttonIndex).getBounds().getMinY() - (BubbleMenuHighlighter.halo_buffer / 2),
									BubbleMenuHighlighter.halo_size, BubbleMenuHighlighter.halo_size);
	}
	
	public static PBounds getContainerBounds()
	{
		return bubbleContainer.getBounds();
	}
	
	public static boolean isBubbleMenuActive()
	{
		//return (bubbleContainer!=null);
		return isBubbleMenuActive;
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
