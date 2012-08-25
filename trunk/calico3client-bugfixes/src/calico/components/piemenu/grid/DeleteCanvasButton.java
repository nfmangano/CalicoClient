package calico.components.piemenu.grid;

import java.util.Date;

import calico.components.grid.CGrid;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.InputEventInfo;

public class DeleteCanvasButton extends PieMenuButton {

	private long uuid = 0L;
	public DeleteCanvasButton()
	{
		super("grid.canvas.delete");		
	}
	
	public void onClick(InputEventInfo ev)
	{
		//confirm with the user?
		//get the canvas ID
		long canvasClicked = CCanvasController.getCanvasAtPoint( PieMenu.lastOpenedPosition );	
		CGrid.getInstance().deleteCanvas(canvasClicked);
		CCanvasController.lock_canvas(canvasClicked, false, "clean canvas action", (new Date()).getTime());
		
	}
}
