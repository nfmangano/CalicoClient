package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import calico.Calico;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoInputManager;
import calico.networking.Networking;
import calico.networking.PacketHandler;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.CIntentionType;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

public class CIntentionCellController
{
	public static CIntentionCellController getInstance()
	{
		return INSTANCE;
	}

	public static void initialize()
	{
		INSTANCE = new CIntentionCellController();
	}

	private static CIntentionCellController INSTANCE;

	private static Long2ReferenceArrayMap<CIntentionCell> cells = new Long2ReferenceArrayMap<CIntentionCell>();
	private static Long2ReferenceArrayMap<CIntentionCell> cellsByCanvasId = new Long2ReferenceArrayMap<CIntentionCell>();

	public void clearCell(long cellId)
	{
		CIntentionCell cell = cells.get(cellId);
		for (CIntentionType type : IntentionCanvasController.getInstance().getActiveIntentionTypes())
		{
			if (cell.hasIntentionType(type.getId()))
			{
				toggleCellIntentionType(cellId, type.getId(), false, true);
			}
		}
		
		if (cell.hasUserTitle())
		{
			setCellTitle(cellId, CIntentionCell.DEFAULT_TITLE, true);
		}
	}

	public long getCellAt(Point point)
	{
		for (CIntentionCell cell : cells.values())
		{
			if (cell.isVisible() && cell.contains(point))
			{
				return cell.getId();
			}
		}
		return -1L;
	}

	public void initializeDisplay()
	{
		for (CIntentionCell cell : cells.values())
		{
			cell.initialize();
		}
	}

	public int countIntentionTypeUsage(long typeId)
	{
		int count = 0;
		for (CIntentionCell cell : cells.values())
		{
			if (cell.hasIntentionType(typeId))
			{
				count++;
			}
		}
		return count;
	}

	public void removeIntentionTypeReferences(long typeId)
	{
		for (CIntentionCell cell : cells.values())
		{
			cell.removeIntentionType(typeId);
		}
	}

	public void activateIconifyMode(boolean b)
	{
		IntentionGraph.getInstance().activateIconifyMode(b);

		for (CIntentionCell cell : cells.values())
		{
			cell.updateIconification();
		}
	}

	// The cells are all created at server startup, so new cells should never be created. If the policy changes at some
	// point, this method will create a new cell, but until then it should not be used.
	@Deprecated
	private void createNewCell(long canvas_uuid)
	{
		int x = 0, y = 0;

		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIC_CREATE);
		packet.putLong(Calico.uuid());
		packet.putLong(canvas_uuid);
		packet.putInt(x);
		packet.putInt(y);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public void setInUse(long cellId, boolean inUse, boolean local)
	{
		CIntentionCell cell = cells.get(cellId);

		if (cell.isInUse() == inUse)
		{
			return;
		}

		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIC_MARK_IN_USE);
		packet.putLong(cellId);
		packet.putBoolean(inUse);

		packet.rewind();
		PacketHandler.receive(packet);
		if (!local)
		{
			Networking.send(packet);
		}
	}

	public void moveCellLocal(long cellId, double x, double y)
	{
		cells.get(cellId).setLocation(x, y);
		IntentionGraphController.getInstance().localUpdateAttachedArrows(cellId, x, y);
	}

	public void moveCell(long cellId, double x, double y)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIC_MOVE);
		packet.putLong(cellId);
		packet.putInt((int) x);
		packet.putInt((int) y);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public void setCellTitle(long cellId, String title, boolean local)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIC_SET_TITLE);
		packet.putLong(cellId);
		packet.putString(title);

		packet.rewind();
		PacketHandler.receive(packet);
		if (!local)
		{
			Networking.send(packet);
		}
	}

	public void toggleCellIntentionType(long cellId, long typeId, boolean add, boolean local)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(add ? IntentionalInterfacesNetworkCommands.CIC_TAG : IntentionalInterfacesNetworkCommands.CIC_UNTAG);
		packet.putLong(cellId);
		packet.putLong(typeId);

		packet.rewind();
		PacketHandler.receive(packet);
		if (!local)
		{
			Networking.send(packet);
		}
	}

	// The set of cells is static according to current policy: one per canvas. If that changes, this method may become
	// useful. Until then, it should not be called.
	@Deprecated
	private void deleteCell(long cell_uuid)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIC_DELETE);
		packet.putLong(cell_uuid);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public void addCell(CIntentionCell cell)
	{
		cells.put(cell.getId(), cell);
		cellsByCanvasId.put(cell.getCanvasId(), cell);

		CalicoInputManager.addCustomInputHandler(cell.getId(), CIntentionCellInputHandler.getInstance());
	}

	public CIntentionCell getCellById(long uuid)
	{
		return cells.get(uuid);
	}

	public CIntentionCell getCellByCanvasId(long canvas_uuid)
	{
		return cellsByCanvasId.get(canvas_uuid);
	}

	public String listVisibleCellAddresses()
	{
		StringBuilder buffer = new StringBuilder("{");
		List<CIntentionCell> orderedCells = new ArrayList<CIntentionCell>(cells.values());
		Collections.sort(orderedCells, new CellAddressSorter());

		for (CIntentionCell cell : orderedCells)
		{
			if (!cell.isVisible())
			{
				continue;
			}

			buffer.append(CCanvasController.canvasdb.get(cell.getCanvasId()).getGridCoordTxt());
			buffer.append(", ");
		}
		if (buffer.length() > 1)
		{
			buffer.setLength(buffer.length() - 2);
		}
		buffer.append("}");

		return buffer.toString();
	}

	private static class CellAddressSorter implements Comparator<CIntentionCell>
	{
		public int compare(CIntentionCell first, CIntentionCell second)
		{
			return CCanvasController.canvasdb.get(first.getCanvasId()).getGridCoordTxt()
					.compareTo(CCanvasController.canvasdb.get(second.getCanvasId()).getGridCoordTxt());
		}
	}
}
