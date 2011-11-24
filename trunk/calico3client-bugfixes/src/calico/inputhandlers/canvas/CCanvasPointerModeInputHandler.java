package calico.inputhandlers.canvas;

import java.awt.Point;
import java.util.Date;

import calico.Calico;
import calico.CalicoOptions;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.CCanvasInputHandler;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.utils.Ticker;

public class CCanvasPointerModeInputHandler extends
		CalicoAbstractInputHandler {

	boolean hasStartedBge = false;
	CCanvasInputHandler parentHandler;
	long canvas_uid;
	
	public CCanvasPointerModeInputHandler(long cuid, CCanvasInputHandler parent) {
		canvas_uid = cuid;
		parentHandler = parent;
		// TODO Auto-generated constructor stub
	}
	
	public void actionPressed(InputEventInfo e) {

		if(e.isLeftButtonPressed())
		{
			int x = e.getX();
			int y = e.getY();
			long uuid = Calico.uuid();
			CStrokeController.setCurrentUUID(uuid);
			CStrokeController.start(uuid, canvas_uid, 0L);
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
