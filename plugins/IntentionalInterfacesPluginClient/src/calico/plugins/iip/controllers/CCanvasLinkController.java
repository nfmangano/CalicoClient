package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
import calico.Calico;
import calico.controllers.CCanvasController;
import calico.networking.Networking;
import calico.networking.PacketHandler;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CCanvasLinkArrow;
import calico.plugins.iip.components.CCanvasLinkBadge;

public class CCanvasLinkController
{
	public static CCanvasLinkController getInstance()
	{
		return INSTANCE;
	}

	private static final CCanvasLinkController INSTANCE = new CCanvasLinkController();

	private static Long2ReferenceArrayMap<CCanvasLink> links = new Long2ReferenceArrayMap<CCanvasLink>();

	public void addLink(CCanvasLink link)
	{
		links.put(link.getId(), link);

		// CGroupController.groupdb.put(uuid, userImage);
		// CCanvasController.canvasdb.get(cuid).getCamera().addChild(
		// CGroupController.groupdb.get(uuid));
		// CGroupController.groupdb.get(uuid).drawPermTemp(true);
		// CGroupController.no_notify_finish(uuid, false);

		IntentionPerspectiveController.getInstance().addArrow(new CCanvasLinkArrow(link));
		CanvasPerspectiveController.getInstance().addBadge(new CCanvasLinkBadge(link.getAnchorA()));
		CanvasPerspectiveController.getInstance().addBadge(new CCanvasLinkBadge(link.getAnchorB()));
	}

	public CCanvasLink getLinkById(long uuid)
	{
		return links.get(uuid);
	}

	public void removeLinkById(long uuid)
	{
		links.remove(uuid);
	}
	
	private long getNextEmptyCanvas(long fromCanvasId)
	{
		for (long canvas_uuid : CCanvasController.getCanvasIDList())
		{
			if (canvas_uuid == fromCanvasId)
			{
				continue;
			}
			
			if (!CCanvasController.canvasdb.get(canvas_uuid).isEmpty())
			{
				continue;
			}
			
			if (CanvasPerspectiveController.getInstance().getBadgeCount(canvas_uuid) > 0)
			{
				continue;
			}
			
			return canvas_uuid;
		}
		
		return 0L;
	}

	public void createLink(long fromCanvasId, CCanvasLink.LinkType type)
	{
		long toCanvasId = getNextEmptyCanvas(fromCanvasId);
		if (toCanvasId == 0L)
		{
			System.out.println("Can't create a link to a new canvas of type " + type + " because there are no more empty canvases!");
			return;
		}
		
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_CREATE);
		packet.putLong(Calico.uuid());
		packet.putInt(type.ordinal());
		packAnchor(packet, fromCanvasId, CCanvasLinkAnchor.Type.INTENTION_CELL);
		packAnchor(packet, toCanvasId, CCanvasLinkAnchor.Type.INTENTION_CELL);
		
		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}
	
	private void packAnchor(CalicoPacket packet, long canvas_uuid, CCanvasLinkAnchor.Type type)
	{
		packet.putLong(Calico.uuid());
		packet.putLong(canvas_uuid);
		packet.putLong(0L);
		packet.putInt(type.ordinal());
		packet.putInt(0);
		packet.putInt(0);
	}
}
