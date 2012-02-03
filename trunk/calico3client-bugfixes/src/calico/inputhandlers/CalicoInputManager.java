package calico.inputhandlers;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import calico.*;
import calico.components.*;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.PieMenu;
import calico.controllers.*;
import calico.events.CalicoEventHandler;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.canvas.CCanvasStrokeModeInputHandler;
import calico.modules.MessageObject;
import calico.networking.ListenServer;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

import java.awt.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;

import it.unimi.dsi.fastutil.longs.*;

/**
 * This handles all the input from the input sources. It then dishes it out to the respective handlers.
 * @author mdempsey
 *
 */
public class CalicoInputManager
{
	private static Logger logger = Logger.getLogger(CalicoInputManager.class.getName());
	
	// TODO: We should be able to determine what canvas we are on using the CCanvasController.getCurrentUUID
	// We Will NEVER get input from a canvas we arent viewing.

	// Need to have some listeners
	private static Long2ReferenceOpenHashMap<CalicoAbstractInputHandler> inputHandlers = new Long2ReferenceOpenHashMap<CalicoAbstractInputHandler>();
	
	private static ArrayList<StickyItem> stickyItems = new ArrayList<StickyItem>();

	//private static IgnorantInputHandler gridInputHandler = new CGridInputHandler();

	// NOTE: These are negative, because that denotes a mouse.
	// When we have multitouch, we will provide multiple buttons, which will all be positive
	// 0 = null?
	public static final int BUTTONID_LEFT		= -1;
	public static final int BUTTONID_RIGHT		= -2;
	public static final int BUTTONID_MIDDLE		= -3;
	public static final int BUTTONID_FAIL		= 0;


	private static long lockInputHandler = 0L;

	public static Point mostRecentPoint;

	public static long group;


	public static void addGroupInputHandler(long uuid)
	{
		if(inputHandlers.containsKey(uuid))
		{
			return;
		}
		inputHandlers.put(uuid, new CGroupInputHandler(uuid));
	}
	
	public static CalicoAbstractInputHandler getInputHandler(long handlerUUID)
	{
		return inputHandlers.get(handlerUUID);
	}
	
//	public static void addListCheckBoxInputHandler(long uuid)
//	{
//		if(inputHandlers.containsKey(uuid))
//		{
//			return;
//		}
//		inputHandlers.put(uuid, new CListCheckBoxInputHandler(uuid));
//	}
	
//	public static void removeListCheckBoxInputHandler(long uuid)
//	{
//		inputHandlers.remove(uuid);
//	}

	/**
	 * This is not used anymore because we dont have any actions for a bgelement
	 * @deprecated
	 * @param uuid
	 */
	public static void addBGElementInputHandler(long uuid)
	{
		return;
		//inputHandlers.put(uuid, new BGElementInputHandler(uuid));
	}

	public static void addCanvasInputHandler(long uuid)
	{
		if(inputHandlers.containsKey(uuid))
		{
			return;
		}
		inputHandlers.put(uuid, new CCanvasInputHandler(uuid));
	}

	public static void addArrowInputHandler(long uuid)
	{
		if(inputHandlers.containsKey(uuid))
		{
			return;
		}
		inputHandlers.put(uuid, new CArrowInputHandler(uuid));
	}
	
	public static void addGridInputHandler()
	{
		if(inputHandlers.containsKey(0L))
		{
			return;
		}
		inputHandlers.put(CGridInputHandler.inputHandlerUUID, new CGridInputHandler());
	}
	
	public static void addCustomInputHandler(long uuid, CalicoAbstractInputHandler handler)
	{
		if (inputHandlers.containsKey(uuid))
		{
			return;
		}
		inputHandlers.put(uuid, handler);
	}
	
	public static void removeCustomInputHandler(long uuid)
	{
		if (inputHandlers.containsKey(uuid))
		{
			inputHandlers.remove(uuid);
		}
	}

	// Locking
	/**
	 * Will release the input handler lock only if the locked uuid == this uuid;
	 */
	public static void unlockHandlerIfMatch(long uuid)
	{
		if(lockInputHandler==uuid)
		{
			lockInputHandler = 0L;
		}
	}

	/**
	 * When we lock an input handler, we FORCE all events to be sent to that handler (regardless of where their position is on the canvas)
	 * @param uuid
	 */
	public static void lockInputHandler(long uuid)
	{
		if (lockInputHandler == 0l)
			lockInputHandler = uuid;
	}



	public static void determineObjectsAtPoint(long canvasuid, Point2D p)
	{
		determineObjectsAtPoint( canvasuid, (int)p.getX(), (int) p.getY());
	}


	private static void determineObjectsAtPoint(long canvasuid, int x, int y)
	{
		// Ok, now we check.
		//System.out.println("Checking Containment ("+x+","+y+"):");

		// Check Groups.
		long[] grplist = CCanvasController.canvasdb.get(canvasuid).getChildGroups();
		if(grplist.length>0)
		{
			long containedGID = 0L;
			double containedGIDArea = Double.MAX_VALUE;

			for(int i=0;i<grplist.length;i++)
			{
				if(CGroupController.groupdb.get(grplist[i]).getArea()< containedGIDArea && CGroupController.groupdb.get(grplist[i]).containsPoint(x, y))
				{
					containedGID = grplist[i];
					containedGIDArea = CGroupController.groupdb.get(grplist[i]).getArea();
				}//if contained
			}//for groups
		}//if grplist>0

	}

	/**
	 * Returns a list of GroupUUIDs that the selected point is contained within.
	 * @param x
	 * @param y
	 * @return
	 */
//	public static long[] getGroupsAtPoint(int x, int y)
//	{
//		long[] grplist = CCanvasController.canvasdb.get( CCanvasController.getCurrentUUID() ).getChildGroups();
//
//		if(grplist.length>0)
//		{
//			LongArrayList groupsAtPoint = new LongArrayList();
//			int groupsMatched = 0;
//
//			for(int i=0;i<grplist.length;i++)
//			{
//				if(CGroupController.groupdb.get(grplist[i]).isFinished() && CGroupController.groupdb.get(grplist[i]).containsPoint(x, y))
//				{
//					groupsAtPoint.add(grplist[i]);
//					groupsMatched++;
//				}//if contained
//			}//for groups
//
//			if(groupsMatched>0)
//			{
//				return groupsAtPoint.toLongArray();
//			}
//			else
//			{
//				return null;
//			}
//
//		}//if grplist>0
//		return null;
//	}
	
	/**
	 * This returns the smallest group UUID of all the groups with the point.
	 * @param x
	 * @param y
	 * @return
	 */
//	public static long getSmallestGroupAtPoint(int x, int y)
//	{
//		long[] matches = getGroupsAtPoint(x,y);
//
//		// No matches were found.
//		if(matches==null)
//		{
//			return 0L;
//		}
//
//		double biggestArea = Double.MAX_VALUE;
//		long chosenGUID = 0L;
//
//		for(int i=0;i<matches.length;i++)
//		{
//			if( CGroupController.groupdb.get(matches[i]).getArea() < biggestArea )
//			{
//				chosenGUID = matches[i];
//				biggestArea = CGroupController.groupdb.get(matches[i]).getArea();
//			}//if contained
//		}//for groups
//		return chosenGUID;
//	}

	public static long getSmallestGroupAtPoint(InputEventInfo ev)
	{
		return CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), ev.getPoint());
//		return getSmallestGroupAtPoint(ev.getX(), ev.getY());
	}
	
	/**
	 * What UUID arrow is located at the requested X,Y coordinate
	 * @param x
	 * @param y
	 * @return
	 */
	public static long getArrowAtPoint(int x, int y)
	{
		long[] arrowlist = CCanvasController.canvasdb.get( CCanvasController.getCurrentUUID() ).getChildArrows();
		if(arrowlist.length>0)
		{
			for(int i=0;i<arrowlist.length;i++)
			{
				if(CArrowController.arrows.get(arrowlist[i]).containsMousePoint(new Point(x,y)) )
				{
					return arrowlist[i];
				}//if contained
			}//for groups
			
		}//if grplist>0
		return 0L;
	}
	public static long getArrowAtPoint(InputEventInfo ev)
	{
		return getArrowAtPoint(ev.getX(), ev.getY());
	}





	/**
	 * Input comes in sets of 3 - (Xpos, Ypos, ButtonID)
	 * @param inputThings
	 */
	public static void handleInput(int... inputThings)
	{
		// THINGS COME IN SETS OF THREE!!!! (x, y, BUTTON

		// Crash out. And smack the dumb coder who send us something not in 3-pair
		if(inputThings.length%3!=0)
		{
			return;
		}

		if(inputThings.length==3)
		{
			// This is single input.

			int xpos = inputThings[0];
			int ypos = inputThings[1];
			int button = inputThings[2];

		}
		else
		{
			// THIS IS SPARTA I MEAN MULTITOUCH
		}//end else multitouch


	}// end handleInput




	public static void handleInput(InputEventInfo ev)
	{
		if(ev.isIgnored())
		{
			return;
		}

		//Calico.log_debug("INPUT: "+ev.toString());
//		handleInputFromQueue(ev);
		InputQueue.queue(ev);
	}

	/**
	 * This reroutes the action to the appropriate handler
	 * @param ev
	 */
	public static void handleInputFromQueue(InputEventInfo ev)
	{
		handleDeviceInput(ev);
		/*
		switch(ev.getType())
		{
			case InputEventInfo.TYPE_MOUSE:handleDeviceInput(ev);break;
			case InputEventInfo.TYPE_PEN:handleDeviceInput(ev);break;

			default:
				// Do nothing at all for now.
				break;
		}
		*/
	}

	public static void handleDeviceInput(InputEventInfo ev)
	{
		// Update the "last action" thing
		CalicoDataStore.touch_input();
		CalicoInputManager.mostRecentPoint = ev.getPoint();
		
		//fire event handlers
		switch(ev.getAction())
		{
			case InputEventInfo.ACTION_DRAGGED:
				break;
			case InputEventInfo.ACTION_PRESSED:
				CalicoEventHandler.getInstance().fireEvent(NetworkCommand.ACTION_PRESSED, 
						CalicoPacket.getPacket(NetworkCommand.ACTION_PRESSED, 
								ev.getPoint().x, ev.getPoint().y, ev.group, System.currentTimeMillis()));
				break;
			case InputEventInfo.ACTION_RELEASED:
				CalicoEventHandler.getInstance().fireEvent(NetworkCommand.ACTION_RELEASED, 
						CalicoPacket.getPacket(NetworkCommand.ACTION_RELEASED, 
								ev.getPoint().x, ev.getPoint().y, ev.group, System.currentTimeMillis()));
				break;
		}
		
		// if we are in the ViewPort view we want to override the default behavior		
		if(CalicoDataStore.isInViewPort){			
			boolean processEvent =CalicoViewportInputHandler.transformInput(ev);
			//Calico.logger.debug("CalicoViewportInputHandler returned:"+processEvent);
			if(!processEvent){
				return;
			}
		}
		
		if (PieMenu.performingPieMenuAction() && ev.getAction() ==InputEventInfo.ACTION_PRESSED)
		{
			PieMenu.isPerformingPieMenuAction = false;
			lockInputHandler = 0l;
		}
		if (BubbleMenu.performingBubbleMenuAction() && ev.getAction() ==InputEventInfo.ACTION_PRESSED)
		{
			BubbleMenu.isPerformingBubbleMenuAction = false;
			lockInputHandler = 0l;
		}

		// This means we have a menu open, so we give that priority!
		if(PieMenu.isPieMenuActive())
		{			
			// We are from a pie menu
			ev.setFlag(InputEventInfo.FLAG_IS_FROM_PIEMENU);
			
			// Was it actually on the thing?
			if(PieMenu.checkIfCoordIsOnPieMenu(ev.getGlobalPoint()))
			{				
				// Did we press the button?
				if(ev.getAction()==InputEventInfo.ACTION_PRESSED)
				{
					PieMenu.clickPieMenuButton(ev.getGlobalPoint(), ev);
				}
				if(CalicoDataStore.isViewingGrid && ev.getAction()==InputEventInfo.ACTION_PRESSED)
				{
					PieMenu.clickPieMenuButton(ev.getGlobalPoint(), ev);
				}
				if (!CalicoDataStore.isViewingGrid)
				{
					lockInputHandler = 0l;
					PieMenu.isPerformingPieMenuAction = true;
				}
				return;
			}
			else if (ev.getAction() == InputEventInfo.ACTION_PRESSED)
			{				
				// Now just kill it.
				PieMenu.clearMenu();
				//return;// Dont return, we should let this one thru!
			}
		}
		else if (PieMenu.performingPieMenuAction())
		{
			lockInputHandler = 0;
			return;
		}

		else if (BubbleMenu.isBubbleMenuActive() && ev.getAction()!=InputEventInfo.ACTION_DRAGGED)
		{
			// We are from a pie menu. PIGGYBACK FROM PIEMENU FLAG FOR NOW
			ev.setFlag(InputEventInfo.FLAG_IS_FROM_PIEMENU);
			
			// Was it actually on the thing?
			if(BubbleMenu.checkIfCoordIsOnBubbleMenu(ev.getGlobalPoint()))
			{				
				//Make sure did not land on it from a stroke
				if (BubbleMenu.performingBubbleMenuAction() || ev.getAction()==InputEventInfo.ACTION_PRESSED)
				{
				
					// Did we press the button?
					if(ev.getAction()==InputEventInfo.ACTION_PRESSED || ev.getAction()==InputEventInfo.ACTION_RELEASED)
					{
						BubbleMenu.clickBubbleMenuButton(ev.getGlobalPoint(), ev);
					}
					//No bubblemenu on grid currently
					/*if(CalicoDataStore.isViewingGrid && ev.getAction()==InputEventInfo.ACTION_PRESSED)
					{
						PieMenu.clickPieMenuButton(ev.getGlobalPoint(), ev);
					}*/
					if (!CalicoDataStore.isViewingGrid)
					{
						lockInputHandler = 0l;
						BubbleMenu.isPerformingBubbleMenuAction = true;
					}
					return;
				}
			}
			else if (BubbleMenu.performingBubbleMenuAction())
			{
				if(ev.getAction()==InputEventInfo.ACTION_RELEASED)
				{
					BubbleMenu.clickBubbleMenuButton(ev.getGlobalPoint(), ev);
				}
				lockInputHandler = 0l;
				return;
			}
			else if (ev.getAction() == InputEventInfo.ACTION_PRESSED)
			{				
				// Now just kill it.
				
				//Need this if else in case group does not exist
				if (BubbleMenu.isBubbleMenuActive())
				{
					if (!CGroupController.exists(BubbleMenu.activeGroup))
					{
						BubbleMenu.clearMenu();
						CCanvasStrokeModeInputHandler.deleteSmudge = true;
					}
					else if (!CGroupController.groupdb.get(BubbleMenu.activeGroup).containsPoint(ev.getPoint().x, ev.getPoint().y)
							|| !CGroupController.groupdb.get(BubbleMenu.activeGroup).isPermanent())
					{
						BubbleMenu.clearMenu();
						CCanvasStrokeModeInputHandler.deleteSmudge = true;
						
					}
				}
				//return;// Dont return, we should let this one thru!
			}
		}
		else if (BubbleMenu.performingBubbleMenuAction())
		{
			lockInputHandler = 0;
			return;
		}
		else if (ev.getAction() == InputEventInfo.ACTION_PRESSED && getStickyItem(ev.getPoint()) != 0)
		{
			lockInputHandler = getStickyItem(ev.getPoint());
		}
		// Are they clicking the menu bar?
		else if(/*!CalicoDataStore.isInViewPort &&*/ !CalicoDataStore.isViewingGrid && CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).isPointOnMenuBar(ev.getGlobalPoint()))
		{
			if (ev.getAction() == InputEventInfo.ACTION_RELEASED)
				CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).clickMenuBar(ev.getGlobalPoint());
			return;
		}		
		
		// 0L is the grid
		if(CalicoDataStore.isViewingGrid )
		{			
			sendEventOut(0L,ev);
			return;
		}
		
		// Ok, now we find what input handler to run
		long itemUUID = 0L;
		if(lockInputHandler==0L)
		{
			
			// check for arrows
			long arrowAtPoint = getArrowAtPoint(ev);
			if(arrowAtPoint!=0L)
			{
				itemUUID = arrowAtPoint;
			}
			
			if(itemUUID==0L)
			{
				// Check to see if any groups fit in to the point
				long smallestGroupUUID = getSmallestGroupAtPoint(ev);
				if(smallestGroupUUID!=0L)
				{
					// we found the smallest group that contains the coord. So run her action listener
					itemUUID = smallestGroupUUID;
				}
			}
			
//			//check for List.CheckBox!
//			
//			if (itemUUID==0L)
//			{
//				long firstUUID = 0L;
//				long[] boxes = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getChildCheckBoxes();
//				for (int i = 0; i < boxes.length; i++)
//					if (CListController.checkBoxes.get(boxes[i]).getBounds().contains(ev.getPoint()))
//						itemUUID = boxes[i];
//			}
			
			
			// Set a default, if all else fails, we go to the canvas
			if(itemUUID==0L)
			{
				itemUUID = CCanvasController.getCurrentUUID();
			}
		}
		else
		{
			// One of the input handlers has a lock, so we load that.
			itemUUID = lockInputHandler;
		}
		try
		{
			sendEventOut(itemUUID,ev);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * This sends the event object to the requested input handler object
	 * @param itemUUID
	 * @param ev
	 */
	private static void sendEventOut(long itemUUID, InputEventInfo ev)
	{
		//logger.debug("ROUTING EVENT TO "+inputHandlers.get(itemUUID).getClass().getName());
//		System.out.println("Sending to event handler: " + inputHandlers.get(itemUUID).getClass().getName());
		
		switch(ev.getAction())
		{
			case InputEventInfo.ACTION_DRAGGED:
				inputHandlers.get(itemUUID).actionDragged(ev);
				break;
			case InputEventInfo.ACTION_PRESSED:
				inputHandlers.get(itemUUID).actionPressed(ev);
//				CalicoEventHandler.getInstance().fireEvent(NetworkCommand.ACTION_PRESSED, 
//						CalicoPacket.getPacket(NetworkCommand.ACTION_PRESSED, 
//								ev.getPoint().x, ev.getPoint().y, ev.group, System.currentTimeMillis()));
				break;
			case InputEventInfo.ACTION_RELEASED:
				inputHandlers.get(itemUUID).actionReleased(ev);
//				CalicoEventHandler.getInstance().fireEvent(NetworkCommand.ACTION_RELEASED, 
//						CalicoPacket.getPacket(NetworkCommand.ACTION_RELEASED, 
//								ev.getPoint().x, ev.getPoint().y, ev.group, System.currentTimeMillis()));
				break;
			case InputEventInfo.ACTION_SCROLL:
				inputHandlers.get(itemUUID).actionScroll(ev);
				break;
			default:
				// Lazy ass
				break;
		}
	}
	
	
	/**
	 * This means we weant to reroute an event
	 * @param newItemUUID UUID of the event handler to route to
	 * @param ev
	 */
	public static void rerouteEvent(long newItemUUID, InputEventInfo ev)
	{
		ev.setFlag(InputEventInfo.FLAG_REROUTED);
		//logger.debug("RE-ROUTING EVENT TO "+inputHandlers.get(newItemUUID).getClass().getName());
		sendEventOut(newItemUUID, ev);
	}
	
	public static void drawCursorImage(long cuuid, Image iconImage, Point p)
	{
		try
		{
			PImage leftClickIcon = new PImage();
			leftClickIcon.setImage(iconImage);
			
			leftClickIcon.setBounds(p.getX()-16, p.getY()-16, 16, 16);
//			leftClickIcon.setPaintInvalid(true);
			
			CCanvasController.canvasdb.get( cuuid ).getLayer().addChild(leftClickIcon);
//			CCanvasController.canvasdb.get( cuuid ).getLayer().repaint();
			
			CalicoInputManager.RemoveCursorImageListener mouseListener = (new CalicoInputManager()).new RemoveCursorImageListener(cuuid, leftClickIcon);
			
			if (CalicoDataStore.isInViewPort)
			{
				CViewportCanvas.getInstance().addMouseListener(mouseListener);
				CViewportCanvas.getInstance().addMouseMotionListener(mouseListener);
			}
			else
			{
				CCanvasController.canvasdb.get(cuuid).addMouseMotionListener(mouseListener);
				CCanvasController.canvasdb.get(cuuid).addMouseListener(mouseListener);
			}

			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public class RemoveCursorImageListener implements MouseMotionListener, MouseListener
	{
		long cuuid;
		PImage icon;
		
		public RemoveCursorImageListener(long cuuid, PImage icon)
		{
			this.cuuid = cuuid;
			this.icon = icon;
		}

		public void mouseDragged(MouseEvent e) {
			CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
			
			e.consume();
			CCanvasController.canvasdb.get( cuuid ).getLayer().removeChild(icon);
//			CCanvasController.canvasdb.get( cuuid ).getLayer().repaint();
		}

		public void mouseMoved(MouseEvent e) {}
		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (CalicoDataStore.isInViewPort)
			{
				CViewportCanvas.getInstance().removeMouseMotionListener(this);
				CViewportCanvas.getInstance().removeMouseListener(this);
			}
			else
			{
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
			}

			
			e.consume();
			CCanvasController.canvasdb.get( cuuid ).getLayer().removeChild(icon);
//			CCanvasController.canvasdb.get( cuuid ).getLayer().repaint();
			
		}		
	}
	
	public static void registerStickyItem(StickyItem node)
	{
		if (!stickyItems.contains(node))
			stickyItems.add(node);
	}
	
	public static void unregisterStickyItem(StickyItem node)
	{
		if (stickyItems.contains(node))
			stickyItems.remove(node);
	}
	
	private static long getStickyItem(Point point) {
		for (StickyItem sticky : stickyItems)
		{
			if (sticky.containsPoint(point))
				return sticky.getUUID();
		}
		return 0;
	}
	

}
