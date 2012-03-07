package calico.components.piemenu.canvas;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.components.grid.CGrid;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

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
