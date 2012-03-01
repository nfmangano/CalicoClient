package calico.plugins.iip;

import calico.Calico;
import calico.CalicoOptions;
import calico.components.CGroup;
import calico.controllers.CCanvasController;
import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.plugins.CalicoPlugin;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.piemenu.canvas.CreateDesignInsideLinkButton;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionCanvasController;
import calico.plugins.iip.controllers.IntentionGraphController;
import calico.plugins.iip.controllers.IntentionalInterfacesCanvasContributor;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class IntentionalInterfacesPlugin extends CalicoPlugin implements CalicoEventListener
{
	public IntentionalInterfacesPlugin()
	{
		super();

		PluginInfo.name = "Intentional Interfaces";
		CalicoIconManager.setIconTheme(this.getClass(), CalicoOptions.core.icontheme);
	}

	public void onPluginStart()
	{
		// register for palette events
		CGroup.registerPieMenuButton(CreateDesignInsideLinkButton.class);
		CalicoEventHandler.getInstance().addListener(NetworkCommand.VIEWING_SINGLE_CANVAS, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(NetworkCommand.CONSISTENCY_FINISH, this, CalicoEventHandler.PASSIVE_LISTENER);
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
				// not using this command anymore
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
			case CIC_DELETE:
				CIC_DELETE(p);
				break;
			case CLINK_CREATE:
				CLINK_CREATE(p);
				break;
			case CLINK_RETYPE:
				CLINK_RETYPE(p);
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
		boolean inUse = p.getBoolean();
		int x = p.getInt();
		int y = p.getInt();

		CIntentionCell cell = new CIntentionCell(uuid, canvas_uuid, inUse, x, y);
		CIntentionCellController.getInstance().addCell(cell);
		cell.setVisible(CCanvasController.hasContent(canvas_uuid));
		IntentionGraph.getInstance().repaint();
	}

	private static void CIC_MOVE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_MOVE.verify(p);

		long uuid = p.getLong();
		CIntentionCell cell = CIntentionCellController.getInstance().getCellById(uuid);

		boolean wasInUse = cell.isInUse();
		cell.setInUse(p.getBoolean());

		int x = p.getInt();
		int y = p.getInt();
		cell.setLocation(x, y);

		IntentionGraphController.getInstance().updateAttachedArrows(cell.getId(), x, y);

		if (wasInUse != cell.isInUse())
		{
			IntentionalInterfacesCanvasContributor.getInstance().notifyContentChanged(cell.getCanvasId());
		}
	}

	private static void CIC_DELETE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_DELETE.verify(p);

		// long uuid = p.getLong();
		// CIntentionCellController.getInstance().removeCellById(uuid);
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
		CCanvasLink.LinkType type = CCanvasLink.LinkType.values()[p.getInt()];
		CCanvasLinkAnchor anchorA = unpackAnchor(p);
		CCanvasLinkAnchor anchorB = unpackAnchor(p);
		String label = p.getString();
		CCanvasLink link = new CCanvasLink(uuid, type, anchorA, anchorB, label);
		CCanvasLinkController.getInstance().addLink(link);
	}

	private static void CLINK_RETYPE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CLINK_RETYPE.verify(p);

		long uuid = p.getLong();
		CCanvasLink link = CCanvasLinkController.getInstance().getLinkById(uuid);

		CCanvasLink.LinkType type = CCanvasLink.LinkType.values()[p.getInt()];
		link.setLinkType(type);
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
		// IntentionGraph.getInstance().repaint(); // is this required?
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
