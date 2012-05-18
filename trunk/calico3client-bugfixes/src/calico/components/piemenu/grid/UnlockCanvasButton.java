package calico.components.piemenu.grid;

import java.util.Date;

import calico.CalicoDataStore;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;

public class UnlockCanvasButton extends PieMenuButton {

	private long uuid = 0L;
	public UnlockCanvasButton()
	{
		super("grid.canvas.unlock");		
	}
	
	public void onClick(InputEventInfo ev)
	{
		//confirm with the user?
		//get the canvas ID
		// GridRemoval: ((CGridInputHandler)CalicoInputManager.getInputHandler(CGridInputHandler.inputHandlerUUID)).triggerLoadCanvas = false;
		
		long canvasClicked = 0L; // GridRemoval: may need this one: CCanvasController.getCanvasAtPoint( PieMenu.lastOpenedPosition );
		long time = (new Date()).getTime();
		boolean lockStatus = CCanvasController.canvasdb.get(canvasClicked).getLockValue();
		CCanvasController.lock_canvas(canvasClicked, false, CalicoDataStore.Username, time);
	}
}
