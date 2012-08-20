package calico.plugins.iip;

import java.awt.Point;

import calico.Calico;
import calico.CalicoOptions;
import calico.components.menus.CanvasMenuBar;
import calico.components.menus.buttons.HistoryNavigationBackButton;
import calico.components.menus.buttons.HistoryNavigationForwardButton;
import calico.components.menus.buttons.SpacerButton;
import calico.controllers.CCanvasController;
import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.perspectives.CanvasPerspective;
import calico.plugins.CalicoPlugin;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.CIntentionType;
import calico.plugins.iip.components.canvas.CanvasTagPanel;
import calico.plugins.iip.components.canvas.CanvasTitlePanel;
import calico.plugins.iip.components.canvas.CopyCanvasButton;
import calico.plugins.iip.components.canvas.NewCanvasButton;
import calico.plugins.iip.components.canvas.ShowIntentionGraphButton;
import calico.plugins.iip.components.graph.CIntentionTopology;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.CIntentionCellFactory;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.controllers.IntentionGraphController;
import calico.plugins.iip.controllers.IntentionalInterfacesCanvasContributor;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.perspectives.IntentionalInterfacesPerspective;

/**
 * Integration point for the Intention View with the Calico plugin mechanism. All network commands are received and
 * initially processed in <code>handleCalicoEvent()</code>.
 * 
 * @author Byron Hawkins
 */
public class IntentionalInterfacesClientPlugin extends CalicoPlugin implements CalicoEventListener
{
	public IntentionalInterfacesClientPlugin()
	{
		super();

		PluginInfo.name = "Intentional Interfaces";
		CalicoIconManager.setIconTheme(this.getClass(), CalicoOptions.core.icontheme);
	}

	/**
	 * Registers for network command notification, initializes controllers, adds buttons to the Canvas View's menu bar.
	 */
	public void onPluginStart()
	{
		CalicoEventHandler.getInstance().addListener(NetworkCommand.VIEWING_SINGLE_CANVAS, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(NetworkCommand.CONSISTENCY_FINISH, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(NetworkCommand.PRESENCE_CANVAS_USERS, this, CalicoEventHandler.PASSIVE_LISTENER);
		for (Integer event : this.getNetworkCommands())
		{
			CalicoEventHandler.getInstance().addListener(event.intValue(), this, CalicoEventHandler.ACTION_PERFORMER_LISTENER);
		}

		Networking.send(CalicoPacket.command(NetworkCommand.UUID_GET_BLOCK));

		while (Calico.numUUIDs() == 0)
		{
			try
			{
				Thread.sleep(100L);
			}
			catch (InterruptedException e)
			{
			}
		}

		IntentionalInterfacesCanvasContributor.initialize();
		CCanvasLinkController.initialize();
		CIntentionCellController.initialize();
		IntentionGraphController.initialize();
		IntentionCanvasController.initialize();
		IntentionalInterfacesPerspective.getInstance(); // load the class

		CanvasMenuBar.addMenuButtonPreAppend(NewCanvasButton.class);
		CanvasMenuBar.addMenuButtonPreAppend(CopyCanvasButton.class);
		CanvasMenuBar.addMenuButtonPreAppend(SpacerButton.class);

		CanvasMenuBar.addMenuButtonPreAppend(HistoryNavigationBackButton.class);
		CanvasMenuBar.addMenuButtonPreAppend(HistoryNavigationForwardButton.class);
		CanvasMenuBar.addMenuButtonPreAppend(ShowIntentionGraphButton.class);
		CanvasMenuBar.addMenuButtonPreAppend(SpacerButton.class);

	}

	@Override
	public void handleCalicoEvent(int event, CalicoPacket p)
	{
		switch (event)
		{
			case NetworkCommand.VIEWING_SINGLE_CANVAS:
				VIEWING_SINGLE_CANVAS(p);
				return;
			case NetworkCommand.CONSISTENCY_FINISH:
				CCanvasLinkController.getInstance().initializeArrowColors();
				return;
			case NetworkCommand.PRESENCE_CANVAS_USERS:
				CIntentionCellController.getInstance().updateUserLists();
				return;
		}

		switch (IntentionalInterfacesNetworkCommands.Command.forId(event))
		{
			case CIC_CREATE:
				CIC_CREATE(p);
				break;
			case CIC_MOVE:
				CIC_MOVE(p);
				break;
			case CIC_SET_TITLE:
				CIC_SET_TITLE(p);
				break;
			case CIC_TAG:
				CIC_TAG(p);
				break;
			case CIC_UNTAG:
				CIC_UNTAG(p);
				break;
			case CIC_DELETE:
				CIC_DELETE(p);
				break;
			case CIC_TOPOLOGY:
				CIC_TOPOLOGY(p);
				break;
			case CIT_CREATE:
				CIT_CREATE(p);
				break;
			case CIT_RENAME:
				CIT_RENAME(p);
				break;
			case CIT_SET_COLOR:
				CIT_SET_COLOR(p);
				break;
			case CIT_DELETE:
				CIT_DELETE(p);
				break;
			case CLINK_CREATE:
				CLINK_CREATE(p);
				break;
			case CLINK_MOVE_ANCHOR:
				CLINK_MOVE_ANCHOR(p);
				break;
			case CLINK_LABEL:
				CLINK_LABEL(p);
				break;
			case CLINK_DELETE:
				CLINK_DELETE(p);
				break;
		}
	}

	private static void VIEWING_SINGLE_CANVAS(CalicoPacket p)
	{
		p.rewind();
		p.getInt();
		long cuid = p.getLong();

		IntentionCanvasController.getInstance().canvasChanged(cuid);
	}

	private static void CIC_CREATE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_CREATE.verify(p);

		long uuid = p.getLong();
		long canvas_uuid = p.getLong();
		int x = p.getInt();
		int y = p.getInt();
		String title = p.getString();

		CIntentionCell cell = new CIntentionCell(uuid, canvas_uuid, new Point(x, y), title);
		CIntentionCellController.getInstance().addCell(cell);
		CIntentionCellFactory.getInstance().cellCreated(cell);
		IntentionGraph.getInstance().repaint();
	}

	private static void CIC_MOVE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_MOVE.verify(p);

		long uuid = p.getLong();
		CIntentionCell cell = CIntentionCellController.getInstance().getCellById(uuid);

		int x = p.getInt();
		int y = p.getInt();
		cell.setLocation(x, y);

		IntentionGraphController.getInstance().cellMoved(cell.getId(), x, y);
	}

	private static void CIC_SET_TITLE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_SET_TITLE.verify(p);

		long uuid = p.getLong();
		CIntentionCell cell = CIntentionCellController.getInstance().getCellById(uuid);

		cell.setTitle(p.getString());

		if (CanvasPerspective.getInstance().isActive() && (CCanvasController.getCurrentUUID() == cell.getCanvasId()))
		{
			CanvasTitlePanel.getInstance().refresh();
		}
		IntentionalInterfacesCanvasContributor.getInstance().notifyContentChanged(cell.getCanvasId());
	}

	private static void CIC_TAG(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_TAG.verify(p);

		long uuid = p.getLong();
		long typeId = p.getLong();
		CIntentionCell cell = CIntentionCellController.getInstance().getCellById(uuid);

		cell.setIntentionType(typeId);
		CCanvasLinkController.getInstance().canvasIntentionTypeChanged(cell);

		if (CanvasPerspective.getInstance().isActive() && (CCanvasController.getCurrentUUID() == cell.getCanvasId()))
		{
			CanvasTagPanel.getInstance().refresh();
		}
		IntentionalInterfacesCanvasContributor.getInstance().notifyContentChanged(cell.getCanvasId());
	}

	private static void CIC_UNTAG(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_UNTAG.verify(p);

		long uuid = p.getLong();
		long typeId = p.getLong();
		CIntentionCell cell = CIntentionCellController.getInstance().getCellById(uuid);

		cell.clearIntentionType();

		if (CanvasPerspective.getInstance().isActive() && (CCanvasController.getCurrentUUID() == cell.getCanvasId()))
		{
			CanvasTagPanel.getInstance().refresh();
		}
		IntentionalInterfacesCanvasContributor.getInstance().notifyContentChanged(cell.getCanvasId());
	}

	private static void CIC_DELETE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_DELETE.verify(p);

		long cellId = p.getLong();

		CIntentionCellController.getInstance().localDeleteCell(cellId);
	}

	private static void CIC_TOPOLOGY(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_TOPOLOGY.verify(p);

		CIntentionTopology topology = new CIntentionTopology(p.getString());
		IntentionGraph.getInstance().setTopology(topology);
	}

	private static void CIT_CREATE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIT_CREATE.verify(p);

		long uuid = p.getLong();
		String name = p.getString();
		int colorIndex = p.getInt();
		CIntentionType type = new CIntentionType(uuid, name, colorIndex);

		IntentionCanvasController.getInstance().localAddIntentionType(type);
	}

	private static void CIT_RENAME(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIT_CREATE.verify(p);

		long uuid = p.getLong();
		String name = p.getString();

		IntentionCanvasController.getInstance().localRenameIntentionType(uuid, name);
	}

	private static void CIT_SET_COLOR(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIT_CREATE.verify(p);

		long uuid = p.getLong();
		int colorIndex = p.getInt();

		IntentionCanvasController.getInstance().localSetIntentionTypeColor(uuid, colorIndex);

		if (IntentionalInterfacesPerspective.getInstance().isActive())
		{
			IntentionGraph.getInstance().repaint();
		}
	}

	private static void CIT_DELETE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIT_CREATE.verify(p);

		long uuid = p.getLong();

		IntentionCanvasController.getInstance().localRemoveIntentionType(uuid);
		CIntentionCellController.getInstance().removeIntentionTypeReferences(uuid);

		if (IntentionalInterfacesPerspective.getInstance().isActive())
		{
			IntentionGraph.getInstance().repaint();
		}
	}

	private static CCanvasLinkAnchor unpackAnchor(CalicoPacket p)
	{
		long uuid = p.getLong();
		long canvas_uuid = p.getLong();
		CCanvasLinkAnchor.ArrowEndpointType type = CCanvasLinkAnchor.ArrowEndpointType.values()[p.getInt()];
		int x = p.getInt();
		int y = p.getInt();

		CCanvasLinkAnchor anchor;
		switch (type)
		{
			case FLOATING:
				anchor = new CCanvasLinkAnchor(uuid, x, y);
				break;
			case INTENTION_CELL:
				anchor = new CCanvasLinkAnchor(uuid, canvas_uuid, x, y);
				break;
			default:
				throw new IllegalArgumentException("Unknown link type " + type);
		}

		anchor.setGroupId(p.getLong());

		return anchor;
	}

	private static void CLINK_CREATE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CLINK_CREATE.verify(p);

		long uuid = p.getLong();
		CCanvasLinkAnchor anchorA = unpackAnchor(p);
		CCanvasLinkAnchor anchorB = unpackAnchor(p);
		String label = p.getString();
		CCanvasLink link = new CCanvasLink(uuid, anchorA, anchorB, label);

		CCanvasLinkController.getInstance().addLink(link);
		IntentionGraphController.getInstance().updateLinkArrow(link);
	}

	private static void CLINK_MOVE_ANCHOR(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CLINK_MOVE_ANCHOR.verify(p);

		long anchor_uuid = p.getLong();
		long canvas_uuid = p.getLong();
		CCanvasLinkAnchor.ArrowEndpointType type = CCanvasLinkAnchor.ArrowEndpointType.values()[p.getInt()];
		int x = p.getInt();
		int y = p.getInt();

		CCanvasLinkController.getInstance().localMoveLinkAnchor(anchor_uuid, canvas_uuid, type, x, y);
	}

	private static void CLINK_LABEL(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CLINK_LABEL.verify(p);

		long uuid = p.getLong();
		CCanvasLink link = CCanvasLinkController.getInstance().getLinkById(uuid);
		link.setLabel(p.getString());

		IntentionGraphController.getInstance().getArrowByLinkId(uuid).redraw();
	}

	private static void CLINK_DELETE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CLINK_DELETE.verify(p);

		long uuid = p.getLong();
		CCanvasLinkController.getInstance().removeLinkById(uuid);
	}

	public Class<?> getNetworkCommandsClass()
	{
		return IntentionalInterfacesNetworkCommands.class;
	}
}
