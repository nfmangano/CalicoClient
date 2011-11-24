package calico.inputhandlers.canvas;

import calico.*;

import calico.components.*;
import calico.components.menus.*;
import calico.components.piemenu.*;
import calico.controllers.CArrowController;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.CCanvasInputHandler;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.networking.*;
import calico.networking.netstuff.*;


import java.awt.geom.*;
import java.awt.*;

import java.util.*;

import org.apache.log4j.*;

import edu.umd.cs.piccolo.event.*;


// implements PenListener
public class CCanvasArrowModeInputHandler extends CalicoAbstractInputHandler
{
	public static Logger logger = Logger.getLogger(CCanvasArrowModeInputHandler.class.getName());

	public static final double CREATE_GROUP_MIN_DIST = 15.0;
	
	

	@SuppressWarnings("unused")
	private InputEventInfo lastEvent = null;
	
	
	@SuppressWarnings("unused")
	private CCanvasInputHandler parentHandler = null;
	
	private Point pressedPoint = null;
	private boolean hasBrokenDistanceThreshold = false;
	
	
	private CArrow tempArrow = null;

	public CCanvasArrowModeInputHandler(long cuid, CCanvasInputHandler parent)
	{
		canvas_uid = cuid;
		parentHandler = parent;
//		this.setupModeIcon("mode.arrow");
	}

	public void actionPressed(InputEventInfo e)
	{
		
		if(e.isLeftButtonPressed())
		{
			pressedPoint = e.getPoint();
			hasBrokenDistanceThreshold = false;
		}
//		this.showModeIcon(e.getPoint());
		CalicoInputManager.drawCursorImage(canvas_uid,
				CalicoIconManager.getIconImage("mode.arrow"), e.getPoint());
		
		lastEvent = e;
	}

	
	public void actionDragged(InputEventInfo e)
	{
//		this.hideModeIcon(e.getPoint());

		CalicoInputManager.lockInputHandler(canvas_uid);

		if(e.isLeftButtonPressed())
		{
			
			if(hasBrokenDistanceThreshold)
			{
				// at this point, we dont care if they slip below the threshold.
				
				if(tempArrow==null)
				{
					// we need to make an arrow
					long auid = Calico.uuid();
					tempArrow = new CArrow(auid,canvas_uid, CalicoDataStore.PenColor, CArrow.TYPE_NORM_HEAD_B,
						new AnchorPoint(CArrow.TYPE_CANVAS, canvas_uid, pressedPoint),
						new AnchorPoint(CArrow.TYPE_CANVAS, canvas_uid, e.getPoint())
					);
					CCanvasController.canvasdb.get(canvas_uid).getLayer().addChild(tempArrow);
				}
				else
				{
					tempArrow.setAnchorB(new AnchorPoint(CArrow.TYPE_CANVAS, canvas_uid, e.getPoint()));
				}
				tempArrow.redraw(true);
				
			}
			else
			{
				// have they broken the threshold
				if(pressedPoint.distance(e.getPoint()) >= CalicoOptions.arrow.create_dist_threshold)
				{
					hasBrokenDistanceThreshold = true;
				}
			}
		}
		
		lastEvent = e;
		
	}
	
	public void actionScroll(InputEventInfo e)
	{
	}
	

	public void actionReleased(InputEventInfo e)
	{
//		this.hideModeIcon();
		CalicoInputManager.unlockHandlerIfMatch(canvas_uid);
		
		boolean isLeft = (e.getButton()==InputEventInfo.BUTTON_LEFT);
		
		if(isLeft && tempArrow!=null)
		{
			// Now we need to find out what groups its endpoints are on, as well linking its parents
			// Group at First point?
			long guuidA = CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), this.tempArrow.getAnchorA().getPoint());
			long guuidB = CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), this.tempArrow.getAnchorB().getPoint());
						
			if(guuidA!=0L)
			{
				tempArrow.setAnchorA(new AnchorPoint(CArrow.TYPE_GROUP, tempArrow.getAnchorA().getPoint(), guuidA));
			}
			
			if(guuidB!=0L)
			{
				tempArrow.setAnchorB(new AnchorPoint(CArrow.TYPE_GROUP, tempArrow.getAnchorB().getPoint(), guuidB));
			}
			
			
			CArrowController.start(tempArrow.getUUID(), tempArrow.getCanvasUUID(), tempArrow.getColor(), tempArrow.getArrowType(), 
				tempArrow.getAnchorA(), 
				tempArrow.getAnchorB()
			);
			
			CCanvasController.canvasdb.get(canvas_uid).getLayer().removeChild(tempArrow);
			
			tempArrow = null;
		}
		
		lastEvent = e;
	}
}
