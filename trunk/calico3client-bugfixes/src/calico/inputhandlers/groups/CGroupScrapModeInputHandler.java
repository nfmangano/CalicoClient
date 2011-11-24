package calico.inputhandlers.groups;

import java.awt.*;

import calico.*;
import calico.components.*;
import calico.components.piemenu.*;
import calico.controllers.*;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.*;
import calico.modules.*;
import calico.networking.*;
import calico.networking.netstuff.*;

import java.awt.Color;
import java.awt.geom.*;
import java.util.*;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolox.nodes.PLine;


public class CGroupScrapModeInputHandler extends CalicoAbstractInputHandler
{
	public static Logger logger = Logger.getLogger(CGroupScrapModeInputHandler.class.getName());
	
	private long uuid = 0L;

	private static final double DRAG_THRESHOLD = 10.0;
	
	private InputEventInfo lastEvent = null;

	private boolean hasStartedGroup = false;
	private boolean hasStartedBge = false;
	private boolean isRCMove = false;// is right click move?
	private boolean weDidSomething = false;
	
	
	private boolean serverNotifiedOfMove = false;

	private Point pressPoint = null;
	
	private Point lastMovePoint = new Point(0,0);
	
	
	
	private boolean hasNotPassedThreshold = true;
	

	private Polygon draggedCoords = null;
	
	private CGroupInputHandler parentHandler = null;
	
	public CGroupScrapModeInputHandler(long u, CGroupInputHandler par)
	{
		uuid = u;
		parentHandler = par;
		
		canvas_uid = CGroupController.groupdb.get(uuid).getCanvasUID();
		
//		this.setupModeIcon("mode.scrap");
	}
	
	public void actionPressed(InputEventInfo e)
	{
		
		CalicoInputManager.lockInputHandler(uuid);
		weDidSomething = false;
		

		hasStartedGroup = false;
		
		if(e.isRightButtonPressed())
		{
			hasNotPassedThreshold = true;
		}
		pressPoint = e.getPoint();
		lastEvent = e;
		
		CalicoInputManager.drawCursorImage(canvas_uid,
				CalicoIconManager.getIconImage("mode.scrap"), e.getPoint());
//		this.showModeIcon(e.getPoint());
	}


	public void actionDragged(InputEventInfo e)
	{
//		this.hideModeIcon(e.getPoint());
		
		
		
		// Ignore the distance if we are still within the presspoint
		
		/*
		if(isRightButton && hasNotPassedThreshold && pressPoint.distance(e.getPoint())<CalicoOptions.getFloat("scrap.drag.threshold"))
		{
			draggedCoords.addPoint(e.getX(), e.getY());
			return;
			//menuTimer.cancel();
			
		}
		else
		{
			hasNotPassedThreshold = false;
		}
		*/
		
		int x = e.getX();
		int y = e.getY();
		

		if(e.isRightButton() && lastEvent!=null)
		{
			if (!weDidSomething)
			{

			}
			if(!serverNotifiedOfMove)
			{
				CGroupController.move_start(this.uuid);
				serverNotifiedOfMove = true;
			}
			CalicoInputManager.lockInputHandler(this.uuid);
			Point delta = e.getDelta(lastEvent);
			lastMovePoint.translate(delta.x, delta.y);
			CGroupController.move(this.uuid, delta.x, delta.y);
			weDidSomething = true;
		}
		else if(e.isLeftButtonPressed())
		{
			if(!hasStartedGroup)
			{
				long nguuid = Calico.uuid();
				CGroupController.start(nguuid, canvas_uid, this.uuid, false);
				CGroupController.setCurrentUUID(nguuid);
				if (lastEvent != null)
					CGroupController.append(nguuid, lastEvent.getX(), lastEvent.getY());
				hasStartedGroup = true;
			}
			else
			{
				CGroupController.append(CGroupController.getCurrentUUID(), x, y);
			}
			weDidSomething = true;
		}
		
		lastEvent = e;
		
	}


	public void actionReleased(InputEventInfo e)
	{
//		this.hideModeIcon();
		
		CalicoInputManager.unlockHandlerIfMatch(this.uuid);
		CGroupController.no_notify_unbold(this.uuid);

		if(weDidSomething)
		{
			int x = e.getX();
			int y = e.getY();
		
			if(e.isRightButton())
			{
				// Are we done drawing/moving?
				// DONT COMMENT THIS OUT! We need it to fix parents
				CGroupController.move_end(this.uuid, lastMovePoint.x, lastMovePoint.y);
					
				lastMovePoint = new Point(0,0);
				
				CGroupController.groupdb.get(this.uuid).resetRightClickMode();
			}
			else if(hasStartedGroup && e.isLeftButton())
			{
				CGroupController.append(CGroupController.getCurrentUUID(), x, y);
				
				
				//CGroupController.set_parent(CGroupController.getCurrentUUID(), this.uuid);
				
				
				CGroupController.finish(CGroupController.getCurrentUUID(), true);

				CGroupController.setLastCreatedGroupUUID(CGroupController.getCurrentUUID());
				
				CGroupController.show_group_piemenu(CGroupController.getCurrentUUID(), e.getPoint(), PieMenuButton.SHOWON_SCRAP_CREATE);
				/*
				PieMenu.displayPieMenu(e.getPoint(), 
						new GroupDeleteButton(CGroupController.getCurrentUUID()), 
						new GroupSetPermanentButton(CGroupController.getCurrentUUID())
						//new GroupChangeChildrenColorButton(CGroupController.getCurrentUUID()),
						//new GroupCopyButton(CGroupController.getCurrentUUID())
				);*/
				hasStartedGroup = false;
			}
			
			
			if(!CGroupController.groupdb.get(uuid).isPermanent())
			{
				CGroupController.drop(uuid);
			}
			
			
		}
		else if(!weDidSomething && e.isRightButton())
		{
			long stroke = CStrokeController.getPotentialScrap(e.getPoint());
			if (!CGroupController.groupdb.get(uuid).isPermanent())
				stroke = 0;
			
			if (stroke != 0l)
				CalicoAbstractInputHandler.clickMenu(stroke, 0l, e.getPoint());
			else
				CGroupController.show_group_piemenu(uuid, e.getGlobalPoint());
		}
		
		
	}

}
