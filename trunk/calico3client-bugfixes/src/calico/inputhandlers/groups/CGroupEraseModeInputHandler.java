package calico.inputhandlers.groups;

import calico.*;

import calico.components.*;
import calico.components.menus.*;
import calico.components.piemenu.*;
import calico.controllers.CStrokeController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.CGroupInputHandler;
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
public class CGroupEraseModeInputHandler extends CalicoAbstractInputHandler
{
	public static Logger logger = Logger.getLogger(CGroupEraseModeInputHandler.class.getName());

	public static final double CREATE_GROUP_MIN_DIST = 15.0;
	
	private long uuid = 0L;

	private CGroupInputHandler parentHandler = null;
	

	public CGroupEraseModeInputHandler(long cuid, CGroupInputHandler parent)
	{
		uuid = cuid;
		parentHandler = parent;
		this.canvas_uid =  CGroupController.groupdb.get(uuid).getCanvasUID();
//		this.setupModeIcon("mode.delete");
	}

	public void actionPressed(InputEventInfo e)
	{
		CalicoInputManager.rerouteEvent(this.canvas_uid, e);
	}

	public void actionDragged(InputEventInfo e)
	{
		CalicoInputManager.rerouteEvent(this.canvas_uid, e);		
	}
	
	public void actionScroll(InputEventInfo e)
	{
		CalicoInputManager.rerouteEvent(this.canvas_uid, e);
	}
	

	public void actionReleased(InputEventInfo e)
	{
		CalicoInputManager.rerouteEvent(this.canvas_uid, e);
		
		
		
		if(e.getButton()==InputEventInfo.BUTTON_RIGHT)
		{
			if(CGroupController.groupdb.get(uuid).containsPoint(e.getX(), e.getY()))
			{
				CGroupController.delete(uuid);
			}
			CGroupController.setCurrentUUID(0L);
		}
		
	}
}
