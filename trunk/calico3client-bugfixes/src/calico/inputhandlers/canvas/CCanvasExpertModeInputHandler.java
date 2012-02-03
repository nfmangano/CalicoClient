package calico.inputhandlers.canvas;

import calico.*;

import calico.components.*;
import calico.components.menus.*;
import calico.components.piemenu.*;
import calico.components.piemenu.canvas.DeleteAreaButton;
import calico.components.piemenu.groups.GroupCopyButton;
import calico.components.piemenu.groups.GroupPasteButton;
import calico.components.piemenu.groups.GroupSetPermanentButton;
import calico.components.piemenu.groups.GroupShrinkToContentsButton;
import calico.controllers.*;
import calico.iconsets.CalicoIconManager;
import calico.input.CInputMode;
import calico.inputhandlers.*;
import calico.modules.*;
import calico.networking.*;
import calico.networking.netstuff.*;
import calico.utils.*;


import java.awt.geom.*;
import java.awt.*;

import java.net.URL;
import java.util.*;

import org.apache.log4j.*;

import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.PImage;
import java.lang.Math;

// implements PenListener
public class CCanvasExpertModeInputHandler extends CalicoAbstractInputHandler
{
	public static Logger logger = Logger.getLogger(CCanvasExpertModeInputHandler.class.getName());

	public static final double CREATE_GROUP_MIN_DIST = 15.0;
	
	private InputEventInfo lastEvent = null;
	
	private CCanvasInputHandler parentHandler = null;
	
//	private Point currentMouseLocation = null;
//	private Point pressedMouseLocation = null;
	private InputEventInfo pressPoint = null;

//	public static class MenuTimer extends TickerTask
//	{
//		private Point point;
//		private long cuuid;
//		
//		public MenuTimer(Point p, long c)
//		{
//			point = p;
//			cuuid = c;
//		}
//		
//		public boolean runtask()
//		{
//			PieMenu.displayPieMenu(point, 
//					new GroupSetPermanentButton(cuuid),
//					new GroupShrinkToContentsButton(cuuid),
//					new DeleteAreaButton(cuuid)
//				);
//			return false;
//		}
//	}
	
	
	

	private boolean hasSentGroupPress = false;
	
	private Point mouseDown;
	private Point mouseUp;

	public CCanvasExpertModeInputHandler(long cuid, CCanvasInputHandler parent)
	{
		canvas_uid = cuid;
		parentHandler = parent;
	}


	
	public void actionPressed(InputEventInfo e)
	{
		this.pressPoint = e;
//		this.currentMouseLocation = new Point(e.getX(), e.getY());
//		this.pressedMouseLocation = new Point(e.getX(), e.getY());
		this.hasSentGroupPress = false;
		if(e.isLeftButtonPressed())
		{
			this.parentHandler.routeToHandler_actionPressed(CInputMode.STROKE, this.pressPoint);
		}
		lastEvent = e;
		mouseDown = e.getPoint();
		
	}
	

	public void actionDragged(InputEventInfo e)
	{

//		this.currentMouseLocation = new Point(e.getX(), e.getY());
		
		
		
		if(e.isLeftButton()) // we are waiting for arrow mode
		{
			this.parentHandler.routeToHandler_actionDragged(CInputMode.STROKE, e);
//			if(this.pressedMouseLocation.distance(this.currentMouseLocation)>=CalicoOptions.core.max_hold_distance) // WE EXCEEDED THE THRESHOLD
//			{	
//				logger.debug("NOT GOING TO ENTER ARROW MODE - MOVED TOO FAR");
				
				// resend the event
				//this.parentHandler.routeToHandler_actionPressed(CInputMode.STROKE, this.pressPoint);
//			}
		}
		else if(e.isLeftButton()) // not ArrowMode
		{
			// draw stroke
			this.parentHandler.routeToHandler_actionDragged(CInputMode.STROKE, e);
		}
		else if(e.isRightButtonPressed())
		{
			if(!this.hasSentGroupPress)
			{
				this.hasSentGroupPress = true;
				lastEvent.setButtonAndMask(InputEventInfo.BUTTON_LEFT);
				this.parentHandler.routeToHandler_actionPressed(CInputMode.SCRAP, lastEvent);
			}
			
			e.setButtonAndMask(InputEventInfo.BUTTON_LEFT);
			this.parentHandler.routeToHandler_actionDragged(CInputMode.SCRAP, e);
		}
		
		
		
		lastEvent = e;
		
	}//dragged
	
	public void actionScroll(InputEventInfo e)
	{
	}
	

	public void actionReleased(InputEventInfo e)
	{
		mouseUp = e.getPoint();
//		this.currentMouseLocation = new Point(e.getX(), e.getY());
		
		// reset this (maybe they just tapped it accidentally)
		if(e.isLeftButton())
		{
			//logger.debug("LEFT BUTTON ELSE RELEASE");
			this.parentHandler.routeToHandler_actionReleased(CInputMode.STROKE, e);
		}
		else if(e.isRightButton())
		{
			// finish scrap
			/*e.setButtonAndMask(InputEventInfo.BUTTON_LEFT);
			logger.debug("RELEASE EXPERT RIGHT BUTTON: "+e.isLeftButtonPressed());
			this.parentHandler.routeToHandler_actionReleased(CInputMode.SCRAP, e);
			if (e.menuShown == false 
				&& mouseDown.distance(mouseUp) < CalicoOptions.pen.doubleClickTolerance)
			{
				CalicoAbstractInputHandler.clickMenu(0l, 0l, mouseDown);
			}*/
		}
		else
		{
			logger.debug("EXPERT RELEASED ELSE");
		}
		
//		long currTime = (new Date()).getTime();
		lastEvent = e;
		
		//	super.mouseReleased(e);
	}
	
}
