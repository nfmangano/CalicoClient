package calico.components.piemenu.grid;

import calico.components.piemenu.PieMenuButton;
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
		long canvasClicked = 0L; // GridRemoval: CCanvasController.getCanvasAtPoint( PieMenu.lastOpenedPosition );		
		if(canvasClicked!=0l){
			// GridRemoval: CGrid.canvasAction=CGrid.CUT_CANVAS;
			// GridRemoval: CGrid.getInstance().drawSelectedCell(canvasClicked, ev.getX(), ev.getY());			
		}		
	}
}
