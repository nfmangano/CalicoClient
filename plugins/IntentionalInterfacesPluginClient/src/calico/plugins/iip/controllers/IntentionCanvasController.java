package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.util.Collection;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.menus.CanvasStatusBar;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.networking.Networking;
import calico.networking.PacketHandler;
import calico.networking.netstuff.CalicoPacket;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CIntentionType;
import calico.plugins.iip.components.IntentionPanelLayout;
import calico.plugins.iip.components.canvas.CanvasLinkPanel;
import calico.plugins.iip.components.canvas.CanvasTagPanel;
import calico.plugins.iip.components.canvas.LinkPanelToolBarButton;
import calico.plugins.iip.components.canvas.TagPanelToolBarButton;
import calico.plugins.iip.components.graph.CopyCanvasButton;
import calico.plugins.iip.components.graph.NewCanvasButton;
import calico.plugins.iip.components.graph.ShowIntentionGraphButton;
import edu.umd.cs.piccolo.PNode;

public class IntentionCanvasController 
{
	public static IntentionCanvasController getInstance()
	{
		return INSTANCE;
	}

	public static void initialize()
	{
		INSTANCE = new IntentionCanvasController();

		INSTANCE.initializeComponents();
		
		CanvasStatusBar.addMenuButtonRightAligned(TagPanelToolBarButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(LinkPanelToolBarButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(ShowIntentionGraphButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(CopyCanvasButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(NewCanvasButton.class);
	}

	private static IntentionCanvasController INSTANCE;

	private final Long2ReferenceArrayMap<CIntentionType> activeIntentionTypes = new Long2ReferenceArrayMap<CIntentionType>();

	private long currentCanvasId = 0L;

	private boolean tagPanelVisible = false;
	private boolean linkPanelVisible = false;

	private void initializeComponents()
	{
		CanvasTagPanel.getInstance().setLayout(new LowerLeftLayout());
		CanvasLinkPanel.getInstance().setLayout(new LowerLeftLayout());
	}
	
	public void localAddIntentionType(CIntentionType type)
	{
		activeIntentionTypes.put(type.getId(), type);

		CanvasTagPanel.getInstance().updateIntentionTypes();
		CanvasLinkPanel.getInstance().updateIntentionTypes();
	}

	public void localRenameIntentionType(long typeId, String name)
	{
		activeIntentionTypes.get(typeId).setName(name);

		CanvasTagPanel.getInstance().updateIntentionTypes();
		CanvasLinkPanel.getInstance().updateIntentionTypes();
	}

	public void localSetIntentionTypeColor(long typeId, int colorIndex)
	{
		activeIntentionTypes.get(typeId).setColorIndex(colorIndex);

		if (CanvasPerspective.getInstance().isActive())
		{
			CanvasTagPanel.getInstance().refresh();
			CanvasLinkPanel.getInstance().refresh();
		}
	}

	public void localRemoveIntentionType(long typeId)
	{
		activeIntentionTypes.remove(typeId);

		CanvasTagPanel.getInstance().updateIntentionTypes();
		CanvasLinkPanel.getInstance().updateIntentionTypes();
	}
	
	public void addIntentionType(String name)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIT_CREATE);
		packet.putLong(Calico.uuid());
		packet.putString(name);

		packet.rewind();
		Networking.send(packet);
	}
	
	public void renameIntentionType(long typeId, String name)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIT_RENAME);
		packet.putLong(typeId);
		packet.putString(name);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}
	
	public void setIntentionTypeColorIndex(long typeId, int colorIndex)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIT_SET_COLOR);
		packet.putLong(typeId);
		packet.putInt(colorIndex);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}
	
	public void removeIntentionType(long typeId)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIT_DELETE);
		packet.putLong(typeId);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}
	
	public void showTagPanel()
	{
		if (!tagPanelVisible)
		{
			toggleTagPanelVisibility();
		}
	}

	public void toggleTagPanelVisibility()
	{
		CanvasLinkPanel.getInstance().setVisible(linkPanelVisible = false);
		
		tagPanelVisible = !tagPanelVisible;
		CanvasTagPanel.getInstance().setVisible(tagPanelVisible);
	}

	public void toggleLinkPanelVisibility()
	{
		CanvasTagPanel.getInstance().setVisible(tagPanelVisible = false);
		
		linkPanelVisible = !linkPanelVisible;
		CanvasLinkPanel.getInstance().setVisible(linkPanelVisible);
	}
	
	public Collection<CIntentionType> getActiveIntentionTypes()
	{
		return activeIntentionTypes.values();
	}

	public void addLink(CCanvasLink link)
	{
		CanvasLinkPanel.getInstance().updateLinks();
	}

	public void removeLink(CCanvasLink link)
	{
		CanvasLinkPanel.getInstance().updateLinks();
	}

	public void moveLinkAnchor(CCanvasLinkAnchor anchor, long previousCanvasId)
	{
		CanvasLinkPanel.getInstance().updateLinks();
	}

	public void canvasChanged(long canvas_uuid)
	{
		currentCanvasId = canvas_uuid;
		CanvasTagPanel.getInstance().moveTo(canvas_uuid);
		CanvasLinkPanel.getInstance().moveTo(canvas_uuid);

		CCanvasLinkController.getInstance().showingCanvas(canvas_uuid);
	}

	private boolean isCurrentlyDisplayed(long groupId)
	{
		if (!CanvasPerspective.getInstance().isActive())
		{
			return false;
		}
		return (CGroupController.groupdb.get(groupId).getCanvasUID() == CCanvasController.getCurrentUUID());
	}

	private class LowerLeftLayout implements IntentionPanelLayout
	{
		@Override
		public void updateBounds(PNode node, double width, double height)
		{
			double x = CanvasLinkPanel.PANEL_INSET_X;
			double y = CalicoDataStore.ScreenHeight - (CanvasLinkPanel.PANEL_INSET_Y + height);
			node.setBounds(x, y, width, height);
		}
	}
}
