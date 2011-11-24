package calico.components.piemenu.groups;

import java.awt.Point;
import java.awt.Rectangle;

import calico.Calico;
import calico.components.CGroup;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

public class GroupPasteButton extends PieMenuButton
{
	private long uuid = 0L;
	public GroupPasteButton(long uuid)
	{
		super("scrap.paste");
		this.uuid = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
		ev.stop();
		
		if (CGroupController.exists(this.uuid))
		{
		
			CGroup group = CGroupController.groupdb.get(this.uuid);
			Rectangle rect = group.getCoordList().getBounds();
			Point center = org.shodor.util11.PolygonUtils.polygonCenterOfMass(group.getCoordList());
			int shift_x = ev.getPoint().x - center.x;
			int shift_y = ev.getPoint().y - center.y;
		
				
			Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_COPY_TO_CANVAS, 
				this.uuid,
				CCanvasController.getCurrentUUID(),
				0L,
				shift_x,
				shift_y,
				ev.getPoint().x,
				ev.getPoint().y
			));
		}
	}
}
