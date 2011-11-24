package calico.inputhandlers.groups;

import calico.*;

import calico.components.*;
import calico.components.menus.*;
import calico.components.piemenu.*;
import calico.controllers.CGroupController;
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
public class CGroupArrowModeInputHandler extends CalicoAbstractInputHandler
{
	private static Logger logger = Logger.getLogger(CGroupArrowModeInputHandler.class.getName());

	private long uuid = 0L;


	//private InputEventInfo lastEvent = null;
	
	private CGroupInputHandler parentHandler = null;
	

	public CGroupArrowModeInputHandler(long cuid, CGroupInputHandler parent)
	{
		uuid = cuid;
		parentHandler = parent;
		canvas_uid = CGroupController.groupdb.get(uuid).getCanvasUID();
		//this.setupModeIcon("mode.arrow");
	}

	public void actionPressed(InputEventInfo e)
	{
		CalicoInputManager.rerouteEvent(canvas_uid, e);
	}

	public void actionDragged(InputEventInfo e)
	{

		CalicoInputManager.rerouteEvent(canvas_uid, e);
	}
	
	public void actionScroll(InputEventInfo e)
	{
	}
	

	public void actionReleased(InputEventInfo e)
	{
		CalicoInputManager.rerouteEvent(canvas_uid, e);
	}
}
