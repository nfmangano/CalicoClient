package calico.inputhandlers.groups;

import java.awt.*;

import calico.*;
import calico.components.*;
import calico.components.piemenu.*;
import calico.components.piemenu.canvas.TextCreate;
import calico.components.piemenu.groups.GroupSetPermanentButton;
import calico.components.piemenu.groups.GroupShrinkToContentsButton;
import calico.controllers.CStrokeController;
import calico.controllers.CArrowController;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.CGroupInputHandler;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.modules.*;
import calico.networking.*;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

import java.awt.Color;
import java.awt.geom.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.shodor.util11.PolygonUtils;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolox.nodes.PLine;


public class CGroupStrokeModeInputHandler extends CalicoAbstractInputHandler
{
	public static Logger logger = Logger.getLogger(CGroupStrokeModeInputHandler.class.getName());
	
	private long uuid = 0L;
	
	private InputEventInfo lastEvent = null;

	private boolean hasBeenPressed = false;
	private boolean hasStartedBge = false;
	private boolean isRCMove = false;// is right click move?
	private boolean weDidSomething = false;
	
	
	private boolean serverNotifiedOfMove = false;

	private Point pressPoint = null;
	
	private Point lastMovePoint = new Point(0,0);
	
	
	private boolean hasNotPassedThreshold = true;
	
	private CGroupInputHandler parentHandler = null;

	private Polygon draggedCoords = null;
	
	private long prevClickTime = -1l;
	private int doubleClickThreshhold = 500;
	private Point mouseDown;
	
	public CGroupStrokeModeInputHandler(long u, CGroupInputHandler par)
	{
		uuid = u;
		parentHandler = par;
		this.canvas_uid =  CGroupController.groupdb.get(uuid).getCanvasUID();
//		this.setupModeIcon("mode.stroke");
	}
	
	public void actionPressed(InputEventInfo e)
	{

		
		hasBeenPressed = true;
		if(e.isLeftButtonPressed())
		{
			int x = e.getX();
			int y = e.getY();
			long suuid = Calico.uuid();
			CStrokeController.setCurrentUUID(suuid);
			CStrokeController.start(suuid, CCanvasController.getCurrentUUID(), this.uuid);
			CStrokeController.append(suuid, x, y);
			CStrokeController.append(suuid, x, y);
			hasStartedBge = true;
			mouseDown = e.getPoint();
		}

		CalicoInputManager.lockInputHandler(uuid);
		
		CalicoInputManager.drawCursorImage(canvas_uid,
				CalicoIconManager.getIconImage("mode.stroke"), e.getPoint());
//		this.showModeIcon(e.getPoint());
	}


	public void actionDragged(InputEventInfo e)
	{
//		this.hideModeIcon(e.getPoint());

//		CalicoInputManager.lockInputHandler(uuid);

		int x = e.getX();
		int y = e.getY();

		
		if(e.isLeftButtonPressed() && hasStartedBge)
		{
			CStrokeController.append(CStrokeController.getCurrentUUID(), x, y);
			//e.setHandled(true);
		}
		else if(e.isLeftButtonPressed() && !hasStartedBge)
		{
			long suuid = Calico.uuid();
			CStrokeController.setCurrentUUID(uuid);
			CStrokeController.start(suuid, CCanvasController.getCurrentUUID(), this.uuid);
			CStrokeController.append(suuid, x, y);
			hasStartedBge = true;
			//e.setHandled(true);
		}
	}


	public void actionReleased(InputEventInfo e)
	{
//		this.hideModeIcon();
		
		CalicoInputManager.unlockHandlerIfMatch(uuid);

		

		int x = e.getX();
		int y = e.getY();
//		if (prevClickTime > 0 && ((new Date()).getTime() - prevClickTime) < doubleClickThreshhold
//				&& e.getPoint().distance(mouseDown) < 10)
//		{
//			logger.debug("WOULD SHOW MENU");
//			
//			long potentialScrap = getPotentialScrap(e.getPoint());
//			if (potentialScrap > 0l)
//			{
//				CStroke stroke = CStrokeController.strokes.get(potentialScrap);
//				stroke.createTemporaryScrapPreview();
//				
//				PieMenu.displayPieMenu(e.getPoint(), 
//						new TextCreate(), 
//						new GroupSetPermanentButton(potentialScrap),
//						new GroupShrinkToContentsButton(potentialScrap));
//			}
//			else
//			{
//				PieMenu.displayPieMenu(e.getPoint(), new TextCreate());
//			}
//		}
		if(!hasBeenPressed && (e.getButton()==InputEventInfo.BUTTON_LEFT))
		{
			long suuid = Calico.uuid();
			CStrokeController.setCurrentUUID(suuid);
			CStrokeController.start(suuid, CCanvasController.getCurrentUUID(), this.uuid);

			CStrokeController.append(suuid, x, y);
			CStrokeController.finish(suuid);
		}
		else if(hasStartedBge && (e.getButton()==InputEventInfo.BUTTON_LEFT))
		{
			long bguid = CStrokeController.getCurrentUUID();
			CStrokeController.append(bguid, x, y);
			CStrokeController.finish(bguid);
			hasStartedBge = false;
		}
		
//		//added by nick 5/10/10
//		if (prevClickTime > 0 && ((new Date()).getTime() - prevClickTime) < doubleClickThreshhold
//				&& e.getPoint().distance(mouseDown) < 10)
//		{
//			logger.debug("WOULD SHOW MENU");
//			
//			long potentialScrap = getPotentialScrap(e.getPoint());
//			if (potentialScrap > 0l)
//			{
//				CStroke stroke = CStrokeController.strokes.get(potentialScrap);
//				stroke.createTemporaryScrapPreview();
//				
//				PieMenu.displayPieMenu(e.getPoint(), 
//						new TextCreate(), 
//						new GroupSetPermanentButton(potentialScrap),
//						new GroupShrinkToContentsButton(potentialScrap));
//			}
//			else
//			{
//				PieMenu.displayPieMenu(e.getPoint(), new TextCreate());
//			}
//		}
//		else if (potentialScrap(CStrokeController.getCurrentUUID()))
//		{
//			PieMenu.displayPieMenu(e.getPoint(), 
//					new GroupSetPermanentButton(CStrokeController.getCurrentUUID()),
//					new GroupShrinkToContentsButton(CStrokeController.getCurrentUUID())
//				);
//		}
		prevClickTime = (new Date()).getTime();
		hasBeenPressed = false;
		lastEvent = e;
		
		
	}

}
