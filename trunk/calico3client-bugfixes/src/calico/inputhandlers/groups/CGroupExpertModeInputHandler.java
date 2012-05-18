package calico.inputhandlers.groups;

import java.awt.*;

import calico.*;
import calico.components.*;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.decorators.CListDecorator;
import calico.components.piemenu.*;
import calico.components.piemenu.canvas.DeleteAreaButton;
import calico.components.piemenu.canvas.TextCreate;
import calico.components.piemenu.groups.GroupSetPermanentButton;
import calico.components.piemenu.groups.GroupShrinkToContentsButton;
import calico.controllers.*;
import calico.iconsets.*;
import calico.input.CInputMode;
import calico.inputhandlers.*;
import calico.inputhandlers.canvas.CCanvasStrokeModeInputHandler;
import calico.modules.*;
import calico.networking.*;
import calico.networking.netstuff.*;
import calico.utils.*;

import java.awt.Color;
import java.awt.geom.*;
import java.net.URL;
import java.util.*;

import org.apache.log4j.Logger;
import org.shodor.util11.PolygonUtils;

import sun.misc.PerformanceLogger;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolox.nodes.PLine;


public class CGroupExpertModeInputHandler extends CalicoAbstractInputHandler
{
	public static Logger logger = Logger.getLogger(CGroupExpertModeInputHandler.class.getName());
	
	private long uuid = 0L;
	
	private Point currentMouseLocation = null;
	private Point pressedMouseLocation = null;
	private Point prevClickPoint = null;
	
	private boolean isWaitingRightHold = false;// are we waiting for the right click hold to set
	private RightClickTimerTicker currentRightClickTimer = null;
	
	private InputEventInfo pressPoint = null;
	
	public static class RightClickTimerTicker extends TickerTask
	{
		private CGroupExpertModeInputHandler handler = null;
		
		public RightClickTimerTicker(CGroupExpertModeInputHandler handler)
		{
			this.handler = handler;
		}
		
		public boolean runtask()
		{
			if(this.handler.getAwaitingRightClickMode(this))
			{
				this.handler.setRightClickHoldMode();
			}
			return false;
		}
	}//RightClickTimerTicker
	
	private boolean isInRightClickMode = false;
	private boolean onePressActionPerformed = false;
	
	private CGroupInputHandler parentHandler = null;
	
	public CGroupExpertModeInputHandler(long u, CGroupInputHandler par)
	{
		uuid = u;
		parentHandler = par;
	}
	
	
	public boolean getAwaitingRightClickMode(RightClickTimerTicker taskObj)
	{
		// we dont want to honor requests from old crap
		if(this.currentRightClickTimer!=taskObj)
		{
			logger.debug("OLD TIMER TASK REQUEST");
			return false;
		}
		
		return this.isWaitingRightHold;
	}
	
	public void setRightClickHoldMode()
	{
		this.isInRightClickMode = true;
		logger.debug("ENABLING RIGHT CLICK MODE");
		CalicoInputManager.drawCursorImage(CGroupController.groupdb.get(this.uuid).getCanvasUID(),
				CalicoIconManager.getIconImage("scrap.move"), this.currentMouseLocation);
	}
	
	public void actionPressed(InputEventInfo e)
	{
		this.pressPoint = e;
		this.currentMouseLocation = new Point(e.getX(), e.getY());
		this.pressedMouseLocation = new Point(e.getX(), e.getY());
		
		this.canvas_uid = CGroupController.groupdb.get(this.uuid).getCanvasUID();
		
		if(e.isRightButtonPressed())
		{
			/*this.isWaitingRightHold = true;
			
			this.currentRightClickTimer = new RightClickTimerTicker(this);
			Ticker.scheduleIn(CalicoOptions.core.hold_time, this.currentRightClickTimer );*/
		}
		else if(e.isLeftButtonPressed())
		{

			if (CGroupController.groupdb.get(uuid) instanceof CListDecorator
				&& ((CListDecorator)CGroupController.groupdb.get(uuid)).getGroupCheckMarkAtPoint(e.getPoint()) != 0)
			{
				CListDecorator list = (CListDecorator)CGroupController.groupdb.get(uuid);
				long grp = list.getGroupCheckMarkAtPoint(e.getPoint());
				CGroupDecoratorController.list_set_check(uuid, CCanvasController.getCurrentUUID(), list.getParentUUID(), grp, !list.isChecked(grp));
				onePressActionPerformed = true;
			}
			else
			{
				if (BubbleMenu.activeUUID != uuid && CGroupController.groupdb.get(uuid).isPermanent())
				{
					CGroupController.show_group_bubblemenu(uuid);
					CCanvasStrokeModeInputHandler.deleteSmudge = true;
				}
				CalicoInputManager.rerouteEvent(this.canvas_uid, e);
			}
		}
	}


	public void actionDragged(InputEventInfo e)
	{
		if (onePressActionPerformed)
			return;
		this.currentMouseLocation = new Point(e.getX(), e.getY());
		
		
		if(e.isRightButton() && this.isInRightClickMode)/////////////////////////////////////////////
		{
//			this.drawRightClickIcon(this.currentMouseLocation);
			/*
			if(this.pressPoint!=null)
			{
				this.parentHandler.routeToHandler_actionPressed(CInputMode.SCRAP, this.pressPoint);
				this.pressPoint = null;
			}
			
			this.parentHandler.routeToHandler_actionDragged(CInputMode.SCRAP, e);*/
		}
		else if(e.isRightButtonPressed() && this.isWaitingRightHold)/////////////////////////////////////////////
		{
			/*if(this.pressedMouseLocation.distance(this.currentMouseLocation)>=CalicoOptions.core.max_hold_distance) // WE EXCEEDED THE THRESHOLD
			{
				this.isWaitingRightHold = false;
				logger.debug("NOT GOING TO ENTER RIGHTCLICK MODE - MOVED TOO FAR");

				this.pressPoint.setButtonAndMask(InputEventInfo.BUTTON_LEFT);
				this.parentHandler.routeToHandler_actionPressed(CInputMode.SCRAP, this.pressPoint);
			}*/
		}
		else if(e.isRightButtonPressed())/////////////////////////////////////////////
		{
			// Reroute to canvas handler
			/*e.setButtonAndMask(InputEventInfo.BUTTON_LEFT);
			this.parentHandler.routeToHandler_actionDragged(CInputMode.SCRAP, e);*/
		}
		else if(e.isLeftButtonPressed())/////////////////////////////////////////////
		{
			CalicoInputManager.rerouteEvent(this.canvas_uid, e);
		}
	}


	public void actionReleased(InputEventInfo e)
	{
		CalicoInputManager.unlockHandlerIfMatch(this.uuid);
		
		if (onePressActionPerformed)
		{
			onePressActionPerformed = false;
			return;
		}
		this.currentMouseLocation = new Point(e.getX(), e.getY());
		

		if(e.isRightButton() && this.isInRightClickMode)
		{
			/*this.isInRightClickMode = false;

			this.parentHandler.routeToHandler_actionReleased(CInputMode.SCRAP, e);*/
		}
		else if(e.isRightButton() && this.isWaitingRightHold)
		{
			/*logger.debug("WOULD SHOW MENU");
			
			long stroke = CStrokeController.getPotentialScrap(e.getPoint());
			if (!CGroupController.groupdb.get(uuid).isPermanent())
				stroke = 0;
			
			if (stroke != 0l)
				CalicoAbstractInputHandler.clickMenu(0l, 0l, e.getPoint());
			else
				CGroupController.show_group_piemenu(uuid, e.getGlobalPoint());*/

		}
		else if(e.isRightButton() && !this.isInRightClickMode)
		{
			/*e.setButtonAndMask(InputEventInfo.BUTTON_LEFT);
			this.parentHandler.routeToHandler_actionReleased(CInputMode.SCRAP, e);*/
		}
		

		else if(e.isLeftButton())
		{
			CalicoInputManager.rerouteEvent(this.canvas_uid, e);
		}
		
		prevClickPoint = e.getPoint();
		this.isWaitingRightHold = false;
		
		
	}

}
