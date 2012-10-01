package calico.inputhandlers.canvas;

import calico.*;
import calico.components.*;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.decorators.CListDecorator;
import calico.components.piemenu.*;
import calico.components.piemenu.canvas.DeleteAreaButton;
import calico.components.piemenu.groups.GroupCreateTempButton;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.CCanvasInputHandler;
import calico.inputhandlers.CGroupInputHandler;
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

	@Deprecated
	private void getMenuBarClick(Point point)
	{
		if(CCanvasController.canvasdb.get(canvas_uid).isPointOnMenuBar(point))
		{
			CCanvasController.canvasdb.get(canvas_uid).clickMenuBar(null, point);
		}
	}



	public void actionPressed(InputEventInfo e)
	{
		//Tablets might not get the released event if the stylus is moved off the edge.
		//Make sure any previous stroke is finished before starting the next
		if (hasBeenPressed)
		{
			InputEventInfo lastEvent = new InputEventInfo();
			lastEvent.setPoint(lastPoint);
			lastEvent.setButton(InputEventInfo.BUTTON_LEFT);
			actionReleased(lastEvent);
		}
		
		mouseMoved = false;
//		CalicoInputManager.drawCursorImage(canvas_uid,
//				CalicoIconManager.getIconImage("mode.stroke"), e.getPoint());
		
		hasBeenPressed = true;
		long uuid = 0l;
		
		if(e.isLeftButton())
		{
			int x = e.getX();
			int y = e.getY();
			uuid = Calico.uuid();
			CStrokeController.setCurrentUUID(uuid);
			CStrokeController.start(uuid, canvas_uid, 0L);
			CStrokeController.append(uuid, x, y);
			hasStartedBge = true;
			mouseDown = e.getPoint();
			
			long potentialConnector;
			if ((potentialConnector = CStrokeController.getPotentialConnector(e.getPoint(), 20)) > 0l)
			{
				PLayer layer = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer();
				menuTimer = new CalicoAbstractInputHandler.MenuTimer(this, uuid, CalicoOptions.core.hold_time/2, CalicoOptions.core.max_hold_distance, CalicoOptions.core.hold_time, e.getPoint(), potentialConnector, layer);
				Ticker.scheduleIn(CalicoOptions.core.hold_time, menuTimer);
			}
			else if (CStrokeController.getPotentialScrap(e.getPoint()) > 0l)
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
		/*if(BubbleMenu.isBubbleMenuActive())
		{
			return;
		}*/
		
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
		/*if(BubbleMenu.isBubbleMenuActive())
		{
			return;
		}*/
		mouseUp = e.getPoint();
//		this.hideModeIcon();
		
		CalicoInputManager.unlockHandlerIfMatch(canvas_uid);

		mouseMoved = true;

		
		int x = e.getX();
		int y = e.getY();
		
		if (e.isLeftButton())
		{
			long strokeUID = CStrokeController.getCurrentUUID();
			boolean isPotentialScrap = false;
			if(hasStartedBge)
//			{
//				bguid = Calico.uuid();
//				CStrokeController.setCurrentUUID(bguid);
//				CStrokeController.start(bguid, canvas_uid, 0L);
//			}
//			else
			{
				CStrokeController.append(strokeUID, x, y);
				 isPotentialScrap = CStrokeController.isPotentialScrap(strokeUID);
				//if it's a circle-scrap, we don't want to broadcast it to the server!
//				if (isPotentialScrap)
//					CStrokeController.no_notify_finish(bguid);
//				else
					CStrokeController.finish(strokeUID);
			}
			
			long nearestConnector = CConnectorController.getNearestConnector(e.getPoint(), 20);
			if (nearestConnector > 0l)
				deleteSmudge = true;

			hasStartedBge = false;
			boolean isSmudge = false;
			if (CStrokeController.exists(strokeUID))
			{
				if (CStrokeController.strokes.get(strokeUID).getWidth() <= 10 &&
						CStrokeController.strokes.get(strokeUID).getHeight() <= 10)
				{
					isSmudge = true;
				}
				if (isSmudge && deleteSmudge)
				{
					CStrokeController.delete(strokeUID);
				}
			}
			
			
			if (isPotentialScrap)
			{
				/*if (CStrokeController.isPotentialScrap(CStrokeController.getCurrentUUID()))
				{
					strokeUID = CStrokeController.getCurrentUUID();
				}*/
				CalicoAbstractInputHandler.clickMenu(strokeUID, 0l, mouseDown);
			}
			else if (nearestConnector > 0l && isSmudge)
			{
				CConnectorController.show_stroke_bubblemenu(nearestConnector, false);
			}
			else if (CStrokeController.exists(strokeUID))
			{
				
				Polygon poly = CStrokeController.strokes.get(strokeUID).getRawPolygon();
				long guuidA = CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), new Point(poly.xpoints[0],poly.ypoints[0]));
				long guuidB = CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), new Point(poly.xpoints[poly.npoints-1], poly.ypoints[poly.npoints-1]));
				if (guuidA != 0l && guuidB != 0l 
						&& !(guuidA == guuidB && CGroupController.groupdb.get(guuidA).containsShape(poly))
						&& !(CGroupController.groupdb.get(guuidA) instanceof CListDecorator) && !(CGroupController.groupdb.get(guuidB) instanceof CListDecorator))
				{
					/**
					 * Refactored so that the originating group can choose what to do. 
					 */
					CalicoAbstractInputHandler groupInputHandler = CalicoInputManager.getInputHandler(guuidA);
					if (groupInputHandler instanceof CGroupInputHandler)
						((CGroupInputHandler)CalicoInputManager.getInputHandler(guuidA)).actionStrokeToAnotherGroup(strokeUID, guuidB);
					//CConnectorController.no_notify_create(Calico.uuid(), CCanvasController.getCurrentUUID(), 0l, CalicoDataStore.PenColor, CalicoDataStore.PenThickness, guuidA, guuidB, strokeUID);
					//CStrokeController.delete(strokeUID);
				}
			}
		}

		deleteSmudge = false;
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
