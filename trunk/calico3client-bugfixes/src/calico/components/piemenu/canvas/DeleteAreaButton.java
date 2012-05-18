package calico.components.piemenu.canvas;

import calico.Calico;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CStrokeController;
import calico.inputhandlers.InputEventInfo;

@Deprecated
public class DeleteAreaButton extends PieMenuButton {

	private long uuid = 0L;
	public DeleteAreaButton(long uuid)
	{
		super("eraser.eraser");
		this.uuid = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		super.onClick(ev);
		CStrokeController.deleteArea(uuid, Calico.uuid());
	}
}
