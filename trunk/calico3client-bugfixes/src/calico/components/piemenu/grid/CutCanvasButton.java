package calico.components.piemenu.grid;

import calico.components.grid.CGrid;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.InputEventInfo;

public class CutCanvasButton extends PieMenuButton {

	private long uuid = 0L;
	public CutCanvasButton()
	{
		super("grid.canvas.move");		
	}
	
	public void onClick(InputEventInfo ev)
	{
		//confirm with the user?
		//get the canvas ID
		long canvasClicked = CCanvasController.getCanvasAtPoint( PieMenu.lastOpenedPosition );		
		if(canvasClicked!=0l){
			CGrid.canvasAction=CGrid.CUT_CANVAS;
			CGrid.getInstance().drawSelectedCell(canvasClicked, ev.getX(), ev.getY());			
		}		
	}
}
