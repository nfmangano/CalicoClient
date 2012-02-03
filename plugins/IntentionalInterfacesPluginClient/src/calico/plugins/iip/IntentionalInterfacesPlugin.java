package calico.plugins.iip;

import calico.CalicoOptions;
import calico.components.menus.CanvasStatusBar;
import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.plugins.CalicoPlugin;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.canvas.CanvasIntentionToolBarButton;
import calico.plugins.iip.components.graph.ShowIntentionGraphButton;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
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
		System.out.println("Fire it up!");
		
		// register for palette events
		CanvasStatusBar.addMenuButtonRightAligned(CanvasIntentionToolBarButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(ShowIntentionGraphButton.class);
		// CGroup.registerPieMenuButton(SaveToPaletteButton.class);
		CalicoEventHandler.getInstance().addListener(NetworkCommand.VIEWING_SINGLE_CANVAS, this, CalicoEventHandler.PASSIVE_LISTENER);
		for (Integer event : this.getNetworkCommands())
		{
			CalicoEventHandler.getInstance().addListener(event.intValue(), this, CalicoEventHandler.ACTION_PERFORMER_LISTENER);
		}
	}

	@Override
	public void handleCalicoEvent(int event, CalicoPacket p)
	{
		if (event == NetworkCommand.VIEWING_SINGLE_CANVAS)
		{
			
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
			case CLINK_MOVE:
				CLINK_MOVE(p);
				break;
			case CLINK_DELETE:
				CLINK_DELETE(p);
				break;
		}
	}

	private static void CIC_CREATE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_CREATE.verify(p);

		long uuid = p.getLong();
		long canvas_uuid = p.getLong();
		int x = p.getInt();
		int y = p.getInt();

		CIntentionCell cell = new CIntentionCell(uuid, canvas_uuid, x, y);
		CIntentionCellController.getInstance().addCell(cell);
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
	}

	private static void CIC_DELETE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CIC_DELETE.verify(p);

		long uuid = p.getLong();
		CIntentionCellController.getInstance().removeCellById(uuid);
	}

	private static CCanvasLinkAnchor unpackAnchor(CalicoPacket p)
	{
		long uuid = p.getLong();
		long canvas_uuid = p.getLong();
		long group_uuid = p.getLong();
		CCanvasLinkAnchor.Type type = CCanvasLinkAnchor.Type.values()[p.getInt()];
		int x = p.getInt();
		int y = p.getInt();
		return new CCanvasLinkAnchor(uuid, canvas_uuid, group_uuid, type, x, y);
	}

	private static void CLINK_CREATE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CLINK_CREATE.verify(p);

		long uuid = p.getLong();
		CCanvasLink.LinkType type = CCanvasLink.LinkType.values()[p.getInt()];
		CCanvasLinkAnchor anchorA = unpackAnchor(p);
		CCanvasLinkAnchor anchorB = unpackAnchor(p);
		CCanvasLink link = new CCanvasLink(uuid, type, anchorA, anchorB);
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

	private static void CLINK_MOVE(CalicoPacket p)
	{
		p.rewind();
		IntentionalInterfacesNetworkCommands.Command.CLINK_MOVE.verify(p);

		long uuid = p.getLong();
		CCanvasLink link = CCanvasLinkController.getInstance().getLinkById(uuid);

		boolean isEndpointA = p.getBoolean();
		long canvas_uuid = p.getLong();
		long group_uuid = p.getLong();
		int x = p.getInt();
		int y = p.getInt();

		if (isEndpointA)
		{
			if (canvas_uuid == 0L)
			{
				link.getAnchorA().move(x, y);
			}
			else
			{
				link.getAnchorA().move(canvas_uuid, group_uuid);
			}
		}
		else
		{
			if (canvas_uuid == 0L)
			{
				link.getAnchorB().move(x, y);
			}
			else
			{
				link.getAnchorB().move(canvas_uuid, group_uuid);
			}
		}
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
