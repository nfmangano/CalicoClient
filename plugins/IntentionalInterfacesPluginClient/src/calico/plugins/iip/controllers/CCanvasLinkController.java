package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import calico.Calico;
import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.networking.Networking;
import calico.networking.PacketHandler;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CCanvasLinkAnchor.Type;

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

	private static Long2ReferenceArrayMap<CCanvasLink> linksById = new Long2ReferenceArrayMap<CCanvasLink>();
	private static Long2ReferenceArrayMap<CCanvasLinkAnchor> anchorsById = new Long2ReferenceArrayMap<CCanvasLinkAnchor>();
	private static Long2ReferenceArrayMap<List<Long>> anchorsIdsByCanvasId = new Long2ReferenceArrayMap<List<Long>>();

	public CCanvasLinkController()
	{
		CCanvasController.addContentContributor(this);
	}

	public CCanvasLinkAnchor getAnchor(long uuid)
	{
		return anchorsById.get(uuid);
	}

	public List<Long> getAnchorIdsByCanvasId(long canvas_uuid)
	{
		List<Long> anchorIds = anchorsIdsByCanvasId.get(canvas_uuid);
		if (anchorIds == null)
		{
			anchorIds = new ArrayList<Long>();
			anchorsIdsByCanvasId.put(canvas_uuid, anchorIds);
		}
		return anchorIds;
	}

	public void addLink(CCanvasLink link)
	{
		linksById.put(link.getId(), link);

		// CGroupController.groupdb.put(uuid, userImage);
		// CCanvasController.canvasdb.get(cuid).getCamera().addChild(
		// CGroupController.groupdb.get(uuid));
		// CGroupController.groupdb.get(uuid).drawPermTemp(true);
		// CGroupController.no_notify_finish(uuid, false);

		IntentionGraphController.getInstance().addLink(link);
		IntentionCanvasController.getInstance().addLink(link);

		addAnchor(link.getAnchorA());
		addAnchor(link.getAnchorB());

		notifyCanvasContentChange(link);
	}

	private void addAnchor(CCanvasLinkAnchor anchor)
	{
		anchorsById.put(anchor.getId(), anchor);
		if (anchor.getType() == Type.INTENTION_CELL)
		{
			getAnchorIdsByCanvasId(anchor.getCanvasId()).add(anchor.getId());
		}
	}

	public void localMoveLinkAnchor(long anchor_uuid, long canvas_uuid, long group_uuid, int x, int y)
	{
		CCanvasLinkAnchor anchor = anchorsById.get(anchor_uuid);

		if (anchor.getCanvasId() != canvas_uuid)
		{
			// no problem if it gets mapped to the 0 canvas (i.e., it is a floating anchor)
			getAnchorIdsByCanvasId(anchor.getCanvasId()).remove(anchor.getId());
			getAnchorIdsByCanvasId(canvas_uuid).add(anchor.getId());
		}

		anchor.move(canvas_uuid, group_uuid, x, y);

		IntentionGraphController.getInstance().updateLinkArrow(anchor.getLink());
	}

	public void moveLinkAnchor(CCanvasLinkAnchor anchor, Point2D newPosition)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_MOVE_ANCHOR);
		packet.putLong(anchor.getId());
		packet.putLong(anchor.getCanvasId());
		packet.putLong(anchor.getGroupId());
		packet.putInt((int) newPosition.getX());
		packet.putInt((int) newPosition.getY());

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public CCanvasLink getLinkById(long uuid)
	{
		return linksById.get(uuid);
	}

	public void removeLinkById(long uuid)
	{
		CCanvasLink link = linksById.remove(uuid);
		IntentionGraphController.getInstance().removeLink(link);
		IntentionCanvasController.getInstance().removeLink(link);
		removeLinkAnchor(link.getAnchorA().getId());
		removeLinkAnchor(link.getAnchorB().getId());
		notifyCanvasContentChange(link);
	}

	private void removeLinkAnchor(long uuid)
	{
		CCanvasLinkAnchor anchor = anchorsById.remove(uuid);
		getAnchorIdsByCanvasId(anchor.getCanvasId()).remove(anchor.getId());
	}

	private void notifyCanvasContentChange(CCanvasLink link)
	{
		if (link != null)
		{
			notifyCanvasContentChange(link.getAnchorA());
			notifyCanvasContentChange(link.getAnchorB());
		}
	}

	private void notifyCanvasContentChange(CCanvasLinkAnchor anchor)
	{
		if (anchor.getType() == Type.INTENTION_CELL)
		{
			CCanvasController.notifyContentChanged(this, anchor.getCanvasId());
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

	public void createLinkToEmptyCanvas(long fromCanvasId, CCanvasLink.LinkType type)
	{
		long toCanvasId = getNextEmptyCanvas(fromCanvasId);
		if (toCanvasId == 0L)
		{
			System.out.println("Can't create a link to a new canvas of type " + type + " because there are no more empty canvases!");
			return;
		}

		createLink(fromCanvasId, toCanvasId, type);
	}

	public void createOrphanedLink(long fromCanvasId, CCanvasLink.LinkType type, double x, double y)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_CREATE);
		packet.putLong(Calico.uuid());
		packet.putInt(type.ordinal());
		packAnchor(packet, fromCanvasId, IntentionGraphController.getInstance().getArrowAnchorPosition(fromCanvasId, x, y));
		packAnchor(packet, 0L, CCanvasLinkAnchor.Type.FLOATING, (int) x, (int) y);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public void createLink(long fromCanvasId, long toCanvasId, CCanvasLink.LinkType type)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_CREATE);
		packet.putLong(Calico.uuid());
		packet.putInt(type.ordinal());
		packAnchor(packet, fromCanvasId, IntentionGraphController.getInstance().getArrowAnchorPosition(fromCanvasId, toCanvasId));
		packAnchor(packet, toCanvasId, IntentionGraphController.getInstance().getArrowAnchorPosition(toCanvasId, fromCanvasId));

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	private void packAnchor(CalicoPacket packet, long canvas_uuid, Point2D position)
	{
		packAnchor(packet, canvas_uuid, CCanvasLinkAnchor.Type.INTENTION_CELL, (int) position.getX(), (int) position.getY());
	}

	private void packAnchor(CalicoPacket packet, long canvas_uuid, CCanvasLinkAnchor.Type type, int x, int y)
	{
		packet.putLong(Calico.uuid());
		packet.putLong(canvas_uuid);
		packet.putLong(0L);
		packet.putInt(type.ordinal());
		packet.putInt(x);
		packet.putInt(y);
	}

	private void deleteLink(long uuid)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_DELETE);
		packet.putLong(uuid);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	@Override
	public boolean hasContent(long canvas_uuid)
	{
		for (CCanvasLink link : linksById.values())
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

	@Override
	public void clearContent(long canvas_uuid)
	{
		Set<Long> linkIdsToDelete = new HashSet<Long>();
		for (long anchorId : getAnchorIdsByCanvasId(canvas_uuid))
		{
			CCanvasLinkAnchor anchor = anchorsById.get(anchorId);
			linkIdsToDelete.add(anchor.getLink().getId());
		}
		
		for (long linkId : linkIdsToDelete)
		{
			deleteLink(linkId);
		}
	}
}
