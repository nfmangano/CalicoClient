package calico.inputhandlers.groups;

import calico.Calico;
import calico.controllers.CCanvasController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.CCanvasInputHandler;
import calico.inputhandlers.CGroupInputHandler;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;

public class CGroupPointerModeInputHandler extends CalicoAbstractInputHandler {

	boolean hasStartedBge = false;
	long uuid;
	CGroupInputHandler parentHandler;
	
	
	public CGroupPointerModeInputHandler(long u, CGroupInputHandler par) {
		uuid = u;
		parentHandler = par;
	}
	
	public void actionPressed(InputEventInfo e) {

		if(e.isLeftButtonPressed())
		{
			int x = e.getX();
			int y = e.getY();
			long uuid = Calico.uuid();
			CStrokeController.setCurrentUUID(uuid);
			CStrokeController.start(uuid, CCanvasController.getCurrentUUID(), 0L);
			CStrokeController.append(uuid, x, y);
			hasStartedBge = true;
		}
		
	}
	
	public void actionDragged(InputEventInfo e) {

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
		}
	}
	
	public void actionReleased(InputEventInfo e) {
		
		CalicoInputManager.unlockHandlerIfMatch(canvas_uid);

		
		int x = e.getX();
		int y = e.getY();
		
		if(!hasStartedBge && e.isLeftButton())
		{
			long uuid = Calico.uuid();
			CStrokeController.setCurrentUUID(uuid);
			CStrokeController.start(uuid, canvas_uid, 0L);

			CStrokeController.append(uuid, x, y);
			CStrokeController.finish(uuid);
		}
		else if(hasStartedBge && e.isLeftButton())
		{
			long bguid = CStrokeController.getCurrentUUID();
			CStrokeController.append(bguid, x, y);
			CStrokeController.finish(bguid);
			hasStartedBge = false;
		}
	}

}
