package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.Color;
import java.util.Collection;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.components.menus.CanvasStatusBar;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.networking.Networking;
import calico.networking.PacketHandler;
import calico.networking.netstuff.CalicoPacket;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.CIntentionType;
import calico.plugins.iip.components.IntentionPanelLayout;
import calico.plugins.iip.components.canvas.CanvasTagPanel;
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
		CanvasStatusBar.addMenuButtonRightAligned(ShowIntentionGraphButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(CopyCanvasButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(NewCanvasButton.class);
	}

	private static IntentionCanvasController INSTANCE;

	// kind of a hack here, would be better to ask the menubar what dimensions it is using
	private static final double MENUBAR_WIDTH = (CalicoOptions.menu.menubar.defaultIconDimension + (CalicoOptions.menu.menubar.iconBuffer * 2));

	private final Long2ReferenceArrayMap<CIntentionType> activeIntentionTypes = new Long2ReferenceArrayMap<CIntentionType>();

	private long currentCanvasId = 0L;

	private boolean tagPanelVisible = false;
	private boolean linkPanelVisible = false;

	private final TagPanelBounds tagPanelBounds = new TagPanelBounds();

	private void initializeComponents()
	{
		CanvasTagPanel.getInstance().setLayout(tagPanelBounds);
	}

	public void localAddIntentionType(CIntentionType type)
	{
		activeIntentionTypes.put(type.getId(), type);

		CanvasTagPanel.getInstance().updateIntentionTypes();
	}

	public void localRenameIntentionType(long typeId, String name)
	{
		activeIntentionTypes.get(typeId).setName(name);

		CanvasTagPanel.getInstance().updateIntentionTypes();
	}

	public void localSetIntentionTypeColor(long typeId, int colorIndex)
	{
		activeIntentionTypes.get(typeId).setColorIndex(colorIndex);

		if (CanvasPerspective.getInstance().isActive())
		{
			CanvasTagPanel.getInstance().refresh();
		}
	}

	public void localRemoveIntentionType(long typeId)
	{
		activeIntentionTypes.remove(typeId);

		CanvasTagPanel.getInstance().updateIntentionTypes();
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
		if (!tagPanelVisible)
		{
			if (CIntentionCellController.getInstance().getCellByCanvasId(currentCanvasId) == null)
			{
				return; // not showing the tag panel if there is no CIC for the current canvas
			}
		}
		
		tagPanelVisible = !tagPanelVisible;
		CanvasTagPanel.getInstance().setVisible(tagPanelVisible);
	}

	public Collection<CIntentionType> getActiveIntentionTypes()
	{
		return activeIntentionTypes.values();
	}

	public CIntentionType getIntentionType(long typeId)
	{
		return activeIntentionTypes.get(typeId);
	}

	public Color getIntentionTypeColor(long typeId)
	{
		if (typeId < 0L)
		{
			return Color.black;
		}
		else
		{
			return getIntentionType(typeId).getColor();
		}
	}

	public void canvasChanged(long canvas_uuid)
	{
		currentCanvasId = canvas_uuid;
		CanvasTagPanel.getInstance().moveTo(canvas_uuid);

		CCanvasLinkController.getInstance().showingCanvas(canvas_uuid);

		CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid);
		if ((cell != null) && !cell.hasIntentionType())
		{
			showTagPanel();
		}
	}

	private boolean isCurrentlyDisplayed(long groupId)
	{
		if (!CanvasPerspective.getInstance().isActive())
		{
			return false;
		}
		return (CGroupController.groupdb.get(groupId).getCanvasUID() == CCanvasController.getCurrentUUID());
	}

	private class TagPanelBounds implements IntentionPanelLayout
	{
		private final int X_MARGIN = 20;
		private final int Y_MARGIN = 20;

		double currentRightEdgePosition = 0.0;

		@Override
		public void updateBounds(PNode node, double width, double height)
		{
			double x = X_MARGIN + MENUBAR_WIDTH;
			currentRightEdgePosition = x + width;
			double y = CalicoDataStore.ScreenHeight
					- (Y_MARGIN + (CalicoOptions.menu.menubar.defaultIconDimension + (CalicoOptions.menu.menubar.iconBuffer * 2)) + height);
			node.setBounds(x, y, width, height);
		}
	}
}
