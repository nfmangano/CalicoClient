package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
import calico.Calico;
import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.networking.Networking;
import calico.networking.PacketHandler;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;

public class CCanvasLinkController implements CCanvas.ContentContributor
{
	public static CCanvasLinkController getInstance()
	{
		return INSTANCE;
	}
	
	public static void initialize()
	{
		INSTANCE = new CCanvasLinkController();
	}

	private static CCanvasLinkController INSTANCE;

	private static Long2ReferenceArrayMap<CCanvasLink> links = new Long2ReferenceArrayMap<CCanvasLink>();
	
	public CCanvasLinkController()
	{
		CCanvasController.addContentContributor(this);
	}

	public void addLink(CCanvasLink link)
	{
		links.put(link.getId(), link);

		// CGroupController.groupdb.put(uuid, userImage);
		// CCanvasController.canvasdb.get(cuid).getCamera().addChild(
		// CGroupController.groupdb.get(uuid));
		// CGroupController.groupdb.get(uuid).drawPermTemp(true);
		// CGroupController.no_notify_finish(uuid, false);

		IntentionGraphController.getInstance().addLink(link);
		IntentionCanvasController.getInstance().addLink(link);
		
		notifyCanvasContentChange(link);
	}

	public CCanvasLink getLinkById(long uuid)
	{
		return links.get(uuid);
	}

	public void removeLinkById(long uuid)
	{
		notifyCanvasContentChange(links.remove(uuid));
	}
	
	private void notifyCanvasContentChange(CCanvasLink link)
	{
		if (link != null)
		{
			CCanvasController.notifyContentChanged(this, link.getAnchorA().getCanvasId());
			CCanvasController.notifyContentChanged(this, link.getAnchorB().getCanvasId());
		}
	}
	
	private long getNextEmptyCanvas(long fromCanvasId)
	{
		for (long canvas_uuid : CCanvasController.getCanvasIDList())
		{
			if (canvas_uuid == fromCanvasId)
			{
				continue;
			}
			
			if (CCanvasController.hasContent(canvas_uuid))
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
	
	@Override
	public boolean hasContent(long canvas_uuid)
	{
		for (CCanvasLink link : links.values())
		{
			if (link.getAnchorA().getCanvasId() == canvas_uuid)
			{
				return true;
			}
			if (link.getAnchorB().getCanvasId() == canvas_uuid)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void contentChanged(long canvas_uuid)
	{
		IntentionGraphController.getInstance().contentChanged(canvas_uuid);
	}
}
