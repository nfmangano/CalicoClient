package calico.components.piemenu.groups;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import calico.Calico;
import calico.Geometry;
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
			Point2D center = calico.utils.Geometry.getMidPoint2D(group.getCoordList());
			int shift_x = ev.getPoint().x - (int)center.getX();
			int shift_y = ev.getPoint().y - (int)center.getY();
		
				
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
