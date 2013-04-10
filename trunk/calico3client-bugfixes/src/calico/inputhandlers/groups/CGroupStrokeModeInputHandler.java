package calico.inputhandlers.groups;

import calico.*;
import calico.controllers.CStrokeController;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.CGroupInputHandler;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;

import org.apache.log4j.Logger;


public class CGroupStrokeModeInputHandler extends CalicoAbstractInputHandler
{
	public static Logger logger = Logger.getLogger(CGroupStrokeModeInputHandler.class.getName());
	
	private long uuid = 0L;

	private boolean hasBeenPressed = false;
	private boolean hasStartedBge = false;
	
	public CGroupStrokeModeInputHandler(long u, CGroupInputHandler par)
	{
		uuid = u;
		this.canvas_uid =  CGroupController.groupdb.get(uuid).getCanvasUID();
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
		}

		CalicoInputManager.lockInputHandler(uuid);
		
		CalicoInputManager.drawCursorImage(canvas_uid,
				CalicoIconManager.getIconImage("mode.stroke"), e.getPoint());
	}


	public void actionDragged(InputEventInfo e)
	{

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
		}
	}


	public void actionReleased(InputEventInfo e)
	{
		
		CalicoInputManager.unlockHandlerIfMatch(uuid);

		

		int x = e.getX();
		int y = e.getY();
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
		hasBeenPressed = false;
		
	}

}
