package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import calico.Calico;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoInputManager;
import calico.networking.Networking;
import calico.networking.PacketHandler;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CCanvasLinkAnchor.ArrowEndpointType;
import calico.plugins.iip.components.CCanvasLinkArrow;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.inputhandlers.CCanvasLinkInputHandler;

public class CCanvasLinkController
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

	private long traversedLinkSourceCanvas = 0L;
	private long traversedLinkDestinationCanvas = 0L;
	
	private boolean arrowColorsInitialized = false;

	public void initializeArrowColors()
	{
		for (CCanvasLinkAnchor anchor : anchorsById.values())
		{
			if (anchor.getLink().getAnchorA() == anchor)
			{
				continue;
			}
			
			long canvasIntentionTypeId = CIntentionCellController.getInstance().getCellByCanvasId(anchor.getCanvasId()).getIntentionTypeId();
			Color color = IntentionCanvasController.getInstance().getIntentionTypeColor(canvasIntentionTypeId);
			CCanvasLinkArrow arrow = IntentionGraphController.getInstance().getArrowByLinkId(anchor.getLink().getId());
			arrow.setColor(color);
			arrow.redraw();
		}
		
		arrowColorsInitialized = true;
	}
	
	public boolean hasTraversedLink()
	{
		return traversedLinkSourceCanvas > 0L;
	}

	public long getTraversedLinkSourceCanvas()
	{
		return traversedLinkSourceCanvas;
	}

	public void traverseLinkToCanvas(CCanvasLinkAnchor anchor)
	{
		this.traversedLinkSourceCanvas = anchor.getCanvasId();
		this.traversedLinkDestinationCanvas = anchor.getOpposite().getCanvasId();
		CCanvasController.loadCanvas(traversedLinkDestinationCanvas);
	}

	public void showingCanvas(long canvasId)
	{
		if (canvasId != traversedLinkDestinationCanvas)
		{
			traversedLinkSourceCanvas = traversedLinkDestinationCanvas = 0L;
		}
	}
	
	public void canvasIntentionTypeChanged(CIntentionCell cell)
	{
		if (!arrowColorsInitialized)
		{
			return;
		}
		
		Color color = IntentionCanvasController.getInstance().getIntentionTypeColor(cell.getIntentionTypeId());
		List<Long> anchorIds = anchorsIdsByCanvasId.get(cell.getCanvasId());
		for (Long anchorId : anchorIds)
		{
			CCanvasLinkAnchor anchor = anchorsById.get(anchorId);
			if (anchor.getLink().getAnchorB() == anchor)
			{
				CCanvasLinkArrow arrow = IntentionGraphController.getInstance().getArrowByLinkId(anchor.getLink().getId());
				arrow.setColor(color);
				arrow.redraw();
			}
		}
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

	public long getLinkAt(Point point)
	{
		for (CCanvasLink link : linksById.values())
		{
			if (link.contains(point))
			{
				return link.getId();
			}
		}
		return -1L;
	}

	public void addLink(CCanvasLink link)
	{
		linksById.put(link.getId(), link);

		addAnchor(link.getAnchorA());
		addAnchor(link.getAnchorB());

		IntentionGraphController.getInstance().addLink(link);

		CalicoInputManager.addCustomInputHandler(link.getId(), CCanvasLinkInputHandler.getInstance());

		notifyCanvasContentChange(link);
	}

	private void addAnchor(CCanvasLinkAnchor anchor)
	{
		anchorsById.put(anchor.getId(), anchor);
		if (anchor.getArrowEndpointType() == ArrowEndpointType.INTENTION_CELL)
		{
			getAnchorIdsByCanvasId(anchor.getCanvasId()).add(anchor.getId());
		}
	}

	public void localMoveLinkAnchor(long anchor_uuid, long canvas_uuid, CCanvasLinkAnchor.ArrowEndpointType type, int x, int y)
	{
		CCanvasLinkAnchor anchor = anchorsById.get(anchor_uuid);
		long originalCanvasId = anchor.getCanvasId();

		if (anchor.getCanvasId() != canvas_uuid)
		{
			// no problem if it gets mapped to the 0 canvas (i.e., it is a floating anchor)
			getAnchorIdsByCanvasId(anchor.getCanvasId()).remove(anchor.getId());
			getAnchorIdsByCanvasId(canvas_uuid).add(anchor.getId());
		}

		anchor.move(canvas_uuid, type, x, y);

		IntentionGraphController.getInstance().updateLinkArrow(anchor.getLink());
	}

	public void moveLinkAnchor(CCanvasLinkAnchor anchor, Point2D newPosition)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_MOVE_ANCHOR);
		packet.putLong(anchor.getId());
		packet.putLong(anchor.getCanvasId());

		packet.putInt(anchor.getArrowEndpointType().ordinal());
		if (anchor.getArrowEndpointType() == ArrowEndpointType.FLOATING)
		{
			packet.putInt((int) newPosition.getX());
			packet.putInt((int) newPosition.getY());
		}
		else
		{
			packet.putInt(0);
			packet.putInt(0);
		}

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public CCanvasLink getLinkById(long uuid)
	{
		return linksById.get(uuid);
	}

	public boolean isNearestSideA(long uuid, Point2D point)
	{
		CCanvasLink link = linksById.get(uuid);
		if (link == null)
		{
			return false;
		}

		point = IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).globalToLocal(new Point2D.Double(point.getX(), point.getY()));
		double a = point.distance(link.getAnchorA().getPoint());
		double b = point.distance(link.getAnchorB().getPoint());
		return a < b;
	}

	public void removeLinkById(long uuid)
	{
		CCanvasLink link = linksById.remove(uuid);
		IntentionGraphController.getInstance().removeLink(link);
		removeLinkAnchor(link.getAnchorA().getId());
		removeLinkAnchor(link.getAnchorB().getId());

		CalicoInputManager.removeCustomInputHandler(uuid);
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
		if (anchor.getArrowEndpointType() == ArrowEndpointType.INTENTION_CELL)
		{
			IntentionalInterfacesCanvasContributor.getInstance().notifyContentChanged(anchor.getCanvasId());
		}
	}

	public void createLinkToEmptyCanvas(long fromCanvasId, double xLinkEndpoint, double yLinkEndpoint, boolean copy)
	{
		long toCanvasId = createLinkToEmptyCanvas(fromCanvasId, copy);
		if (toCanvasId == 0L)
		{
			return;
		}

		Point2D cellOrigin = IntentionGraphController.getInstance().alignCellEdgeAtLinkEndpoint(fromCanvasId, xLinkEndpoint, yLinkEndpoint);

		CIntentionCellController.getInstance().moveCell(CIntentionCellController.getInstance().getCellByCanvasId(toCanvasId).getId(), cellOrigin.getX(),
				cellOrigin.getY());
	}

	public long createLinkToEmptyCanvas(long fromCanvasId, boolean copy)
	{
		long toCanvasId = CIntentionCellFactory.getInstance().createNewCell().getCanvasId();
		createLink(fromCanvasId, toCanvasId);
		if (copy)
		{
			copyCanvas(fromCanvasId, toCanvasId);
		}

		return toCanvasId;
	}

	public void copyCanvas(long sourceCanvasId, long targetCanvasId)
	{
		Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_COPY, sourceCanvasId, targetCanvasId));
	}

	public void orphanLink(CCanvasLinkAnchor anchor, double x, double y)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_MOVE_ANCHOR);
		packet.putLong(anchor.getId());
		packet.putLong(0L);
		packet.putInt(CCanvasLinkAnchor.ArrowEndpointType.FLOATING.ordinal());
		packet.putInt((int) x);
		packet.putInt((int) y);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public void moveLink(CCanvasLinkAnchor anchor, long canvasId)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_MOVE_ANCHOR);
		packet.putLong(anchor.getId());
		packet.putLong(canvasId);
		packet.putInt(CCanvasLinkAnchor.ArrowEndpointType.INTENTION_CELL.ordinal());
		packet.putInt(0);
		packet.putInt(0);
		
		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public void createOrphanedLink(long fromCanvasId, double x, double y)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_CREATE);
		packet.putLong(Calico.uuid());
		packAnchor(packet, fromCanvasId, IntentionGraphController.getInstance().getArrowAnchorPosition(fromCanvasId, x, y));
		packAnchor(packet, 0L, CCanvasLinkAnchor.ArrowEndpointType.FLOATING, (int) x, (int) y);
		packet.putString(""); // empty label

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public void createLink(long fromCanvasId, long toCanvasId)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_CREATE);
		packet.putLong(Calico.uuid());
		packAnchor(packet, fromCanvasId, IntentionGraphController.getInstance().getArrowAnchorPosition(fromCanvasId, toCanvasId));
		packAnchor(packet, toCanvasId, IntentionGraphController.getInstance().getArrowAnchorPosition(toCanvasId, fromCanvasId));
		packet.putString(""); // empty label

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	private void packAnchor(CalicoPacket packet, long canvas_uuid, Point2D position)
	{
		packAnchor(packet, canvas_uuid, CCanvasLinkAnchor.ArrowEndpointType.INTENTION_CELL, (int) position.getX(), (int) position.getY(), 0L);
	}

	private void packAnchor(CalicoPacket packet, long canvas_uuid, Point2D position, long group_uuid)
	{
		packAnchor(packet, canvas_uuid, CCanvasLinkAnchor.ArrowEndpointType.INTENTION_CELL, (int) position.getX(), (int) position.getY(), group_uuid);
	}

	private void packAnchor(CalicoPacket packet, long canvas_uuid, CCanvasLinkAnchor.ArrowEndpointType type, int x, int y)
	{
		packAnchor(packet, canvas_uuid, type, x, y, 0L);
	}

	private void packAnchor(CalicoPacket packet, long canvas_uuid, CCanvasLinkAnchor.ArrowEndpointType type, int x, int y, long group_uuid)
	{
		packet.putLong(Calico.uuid());
		packet.putLong(canvas_uuid);

		packet.putInt(type.ordinal());
		if (type == ArrowEndpointType.FLOATING)
		{
			packet.putInt(x);
			packet.putInt(y);
		}
		else
		{
			packet.putInt(0);
			packet.putInt(0);
		}

		packet.putLong(group_uuid);
	}

	public void setLinkLabel(long uuid, String label)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_LABEL);
		packet.putLong(uuid);
		packet.putString(label);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public void deleteLink(long uuid, boolean local)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CLINK_DELETE);
		packet.putLong(uuid);

		packet.rewind();
		PacketHandler.receive(packet);
		if (!local)
		{
			Networking.send(packet);
		}
	}

	boolean hasLinks(long canvas_uuid)
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

	void clearLinks(long canvas_uuid)
	{
		Set<Long> linkIdsToDelete = new HashSet<Long>();
		for (long anchorId : getAnchorIdsByCanvasId(canvas_uuid))
		{
			CCanvasLinkAnchor anchor = anchorsById.get(anchorId);
			linkIdsToDelete.add(anchor.getLink().getId());
		}

		for (long linkId : linkIdsToDelete)
		{
			deleteLink(linkId, true);
		}
	}
}
