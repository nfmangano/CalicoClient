package calico.inputhandlers.canvas;

import calico.*;
import calico.components.*;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.*;
import calico.components.piemenu.canvas.DeleteAreaButton;
import calico.components.piemenu.groups.GroupCreateTempButton;
import calico.controllers.CStrokeController;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.CCanvasInputHandler;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.PressAndHoldAction;
import calico.inputhandlers.groups.CGroupExpertModeInputHandler;
import calico.utils.Geometry;
import calico.utils.Ticker;

import java.awt.*;
import java.awt.geom.Arc2D.Double;
import java.util.*;
import org.apache.log4j.*;

import edu.umd.cs.piccolo.PLayer;

// implements PenListener
public class CCanvasStrokeModeInputHandler extends CalicoAbstractInputHandler
	implements PressAndHoldAction
{
	public static Logger logger = Logger.getLogger(CCanvasStrokeModeInputHandler.class.getName());
	
	private boolean hasStartedBge = false;
	
	private boolean hasBeenPressed = false;

	private Point lastPoint = null;
	private long lastPointTime = -1l;
	private long lastStroke = 0l;
	
	public Point mouseDown;
	public Point mouseUp;
	public static boolean mouseMoved = false;
	
	CalicoAbstractInputHandler.MenuTimer menuTimer;
	private CCanvasInputHandler parentHandler = null;
	
	public static boolean deleteSmudge = false;
	
	public void openMenu(long potScrap, long group, Point point)
	{
		CalicoAbstractInputHandler.clickMenu(potScrap, group, point);
	}
	
	public CCanvasStrokeModeInputHandler(long cuid, CCanvasInputHandler parent)
	{
		canvas_uid = cuid;
		parentHandler = parent;
		this.setupModeIcon("mode.stroke");
	}


	private void getMenuBarClick(Point point)
	{
		if(CCanvasController.canvasdb.get(canvas_uid).isPointOnMenuBar(point))
		{
			CCanvasController.canvasdb.get(canvas_uid).clickMenuBar(point);
		}
	}



	public void actionPressed(InputEventInfo e)
	{
		mouseMoved = false;
//		CalicoInputManager.drawCursorImage(canvas_uid,
//				CalicoIconManager.getIconImage("mode.stroke"), e.getPoint());
		
		hasBeenPressed = true;
		long uuid = 0l;
		
		if(e.isLeftButtonPressed())
		{
			int x = e.getX();
			int y = e.getY();
			uuid = Calico.uuid();
			CStrokeController.setCurrentUUID(uuid);
			CStrokeController.start(uuid, canvas_uid, 0L);
			CStrokeController.append(uuid, x, y);
			hasStartedBge = true;
			mouseDown = e.getPoint();
			
			if (CStrokeController.getPotentialScrap(e.getPoint()) > 0l && !BubbleMenu.isBubbleMenuActive())
			{
				PLayer layer = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer();
				menuTimer = new CalicoAbstractInputHandler.MenuTimer(this, uuid, CalicoOptions.core.hold_time/2, CalicoOptions.core.max_hold_distance, CalicoOptions.core.hold_time, e.getPoint(), e.group, layer);
				Ticker.scheduleIn(CalicoOptions.core.hold_time, menuTimer);
			}
//			menuThread = new DisplayMenuThread(this, e.getGlobalPoint(), e.group);		
//			Ticker.scheduleIn(CalicoOptions.core.hold_time, menuThread);
		}
		
		lastPoint = e.getPoint();
		lastPointTime = System.currentTimeMillis();
		lastStroke = uuid;
	}

	public void actionDragged(InputEventInfo e)
	{
		if (mouseMoved == false && mouseDown != null && mouseDown.distance(e.getPoint()) > 5)
		{
			mouseMoved = true;

		}
		if(PieMenu.isPieMenuActive())
		{
			return;
		}
		
//		this.hideModeIcon(e.getPoint());

		int x = e.getX();
		int y = e.getY();

		
		if(e.isLeftButtonPressed() && hasStartedBge)
		{
			CStrokeController.append(CStrokeController.getCurrentUUID(), x, y);

		}
		else if(e.isLeftButtonPressed() && !hasStartedBge)
		{
			long uuid = Calico.uuid();
			CStrokeController.setCurrentUUID(uuid);
			CStrokeController.start(uuid, canvas_uid, 0L);
			CStrokeController.append(uuid, x, y);
			hasStartedBge = true;
			hasBeenPressed = true;
		}
		
		lastPoint = e.getPoint();
		lastPointTime = System.currentTimeMillis();
	}
	
	public void actionScroll(InputEventInfo e)
	{
	}
	

	public void actionReleased(InputEventInfo e)
	{
		if(PieMenu.isPieMenuActive())
		{
			return;
		}
		mouseUp = e.getPoint();
//		this.hideModeIcon();
		
		CalicoInputManager.unlockHandlerIfMatch(canvas_uid);

		mouseMoved = true;

		
		int x = e.getX();
		int y = e.getY();
		
		if (e.isLeftButton())
		{
			long bguid;
			boolean isPotentialScrap = false;
			if(hasStartedBge)
//			{
//				bguid = Calico.uuid();
//				CStrokeController.setCurrentUUID(bguid);
//				CStrokeController.start(bguid, canvas_uid, 0L);
//			}
//			else
			{
				bguid = CStrokeController.getCurrentUUID();
				CStrokeController.append(bguid, x, y);
				 isPotentialScrap = CStrokeController.isPotentialScrap(CStrokeController.getCurrentUUID());
				//if it's a circle-scrap, we don't want to broadcast it to the server!
//				if (isPotentialScrap)
//					CStrokeController.no_notify_finish(bguid);
//				else
					CStrokeController.finish(bguid);
			}

			

			hasStartedBge = false;
			boolean isSmudge = false;
			if (deleteSmudge &&
					CStrokeController.strokes.get(CStrokeController.getCurrentUUID()).getWidth() <= 5 &&
					CStrokeController.strokes.get(CStrokeController.getCurrentUUID()).getHeight() <= 5)
			{
				isSmudge = true;
				CStrokeController.delete(CStrokeController.getCurrentUUID());
				deleteSmudge = false;
			}
			
			
			if (isPotentialScrap && !isSmudge)
			{
				long strokeUID = 0l;
				if (CStrokeController.isPotentialScrap(CStrokeController.getCurrentUUID()))
				{
					strokeUID = CStrokeController.getCurrentUUID();
				}
				CalicoAbstractInputHandler.clickMenu(strokeUID, 0l, mouseDown);
			}
		}

		hasBeenPressed = false;
		lastPoint = null;
		lastPointTime = 0l;
		lastStroke = 0l;
	}
	
	public Point getLastPoint()
	{
		return lastPoint;
	}
	
	public long getLastPointTime()
	{
		return lastPointTime;
	}
	
	public long getLastAction()
	{
		return lastStroke;
	}
	
	public void pressAndHoldCompleted()
	{
//		CStrokeController.no_notify_delete(CStrokeController.getCurrentUUID());
		CStrokeController.delete(CStrokeController.getCurrentUUID());
	}
	
	public Point getMouseDown()
	{
		return mouseDown;
	}
	
	public Point getMouseUp()
	{
		return mouseUp;
	}
	
	public void pressAndHoldAbortedEarly()
	{
		//Do nothing
	}
	
	public double getDraggedDistance()
	{
		if (lastStroke == 0l || !CStrokeController.exists(lastStroke))
			return java.lang.Double.MAX_VALUE;
		
		
		return CStrokeController.strokes.get(lastStroke).getLength();
	}
}
