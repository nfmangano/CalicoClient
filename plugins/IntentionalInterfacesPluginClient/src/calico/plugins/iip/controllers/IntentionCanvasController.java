package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import calico.plugins.iip.components.canvas.CCanvasLinkBadge;
import calico.plugins.iip.components.canvas.CCanvasLinkToken;
import calico.plugins.iip.components.canvas.CanvasBadgeRow;
import calico.plugins.iip.components.canvas.CanvasLinkPanel;
import calico.plugins.iip.components.canvas.CanvasTagPanel;
import calico.plugins.iip.components.canvas.LinkPanelToolBarButton;
import calico.plugins.iip.components.canvas.TagPanelToolBarButton;
import calico.plugins.iip.components.graph.NewIdeaButton;
import calico.plugins.iip.components.graph.ShowIntentionGraphButton;
import edu.umd.cs.piccolo.PNode;

public class IntentionCanvasController implements CGroupController.Listener
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
		CanvasStatusBar.addMenuButtonRightAligned(NewIdeaButton.class);
	}

	private static IntentionCanvasController INSTANCE;

	private final Long2ReferenceArrayMap<CIntentionType> activeIntentionTypes = new Long2ReferenceArrayMap<CIntentionType>();

	// remove these
	// need tokens for 
	private final Long2ReferenceArrayMap<CCanvasLinkBadge> badgesByAnchorId = new Long2ReferenceArrayMap<CCanvasLinkBadge>();
	private final Long2ReferenceArrayMap<CCanvasLinkToken> tokensByAnchorId = new Long2ReferenceArrayMap<CCanvasLinkToken>();
	private final Long2ReferenceArrayMap<CanvasBadgeRow> badgeRowsByGroupId = new Long2ReferenceArrayMap<CanvasBadgeRow>();

	private long currentCanvasId = 0L;

	private Comparator<CCanvasLinkToken> sorter = new DefaultSorter();

	private boolean tagPanelVisible = false;
	private boolean linkPanelVisible = false;

	private IntentionCanvasController()
	{
		CGroupController.addListener(this);
	}
	
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

	public void removeBadgeRow(long groupId)
	{
		badgeRowsByGroupId.remove(groupId);
	}

	private void addBadge(CCanvasLinkAnchor anchor)
	{
		CCanvasLinkBadge badge = new CCanvasLinkBadge(anchor);
		badgesByAnchorId.put(badge.getLinkAnchor().getId(), badge);

		CanvasBadgeRow row = getBadgeRow(anchor.getGroupId());
		row.addBadge(badge);
	}

	private CanvasBadgeRow getBadgeRow(long groupId)
	{
		CanvasBadgeRow row = badgeRowsByGroupId.get(groupId);
		if (row == null)
		{
			row = new CanvasBadgeRow(groupId);
			badgeRowsByGroupId.put(groupId, row);
			row.setVisible(tagPanelVisible);
		}
		return row;
	}

	private void removeBadge(CCanvasLinkAnchor anchor)
	{
		CCanvasLinkBadge badge = badgesByAnchorId.remove(anchor.getId());
		CanvasBadgeRow row = badgeRowsByGroupId.get(anchor.getGroupId());
		row.removeBadge(badge);
	}

	private void addToken(CCanvasLinkAnchor anchor)
	{
		CCanvasLinkToken token = new CCanvasLinkToken(anchor);
		tokensByAnchorId.put(token.getLinkAnchor().getId(), token);

		if (CanvasPerspective.getInstance().isActive() && (anchor.getCanvasId() == currentCanvasId))
		{
			// add to link panel
		}
	}

	private void removeToken(CCanvasLinkAnchor anchor)
	{
		removeToken(anchor.getId(), anchor.getCanvasId(), anchor.getLink().getAnchorA() == anchor);
	}

	private void removeToken(long anchorId, long canvasId, boolean isAnchorA)
	{
		tokensByAnchorId.remove(anchorId);
		CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(canvasId).remove(anchorId);

		if (CanvasPerspective.getInstance().isActive() && (canvasId == currentCanvasId))
		{
			// remove from link panel
		}
	}

	public int getTokenCount(long canvas_uuid)
	{
		return CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(canvas_uuid).size();
	}

	public int getTokenCount(long canvas_uuid, CCanvasLink.LinkDirection direction)
	{
		int count = 0;
		for (long tokenAnchorId : CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(canvas_uuid))
		{
			if (CCanvasLinkController.getInstance().getAnchor(tokenAnchorId).hasGroup())
			{
				// it's a badge on a CGroup, don't put it in the link bay
				continue;
			}

			CCanvasLinkToken token = tokensByAnchorId.get(tokenAnchorId);
			if (token.getDirection() == direction)
			{
				count++;
			}
		}
		return count;
	}

	public void populateTokens(long canvas_uuid, CCanvasLink.LinkDirection direction, List<CCanvasLinkToken> tokens)
	{
		tokens.clear();

		for (Long tokenAnchorId : CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(canvas_uuid))
		{
			if (CCanvasLinkController.getInstance().getAnchor(tokenAnchorId).hasGroup())
			{
				// it's a badge on a CGroup, don't put it in the link bay
				continue;
			}

			CCanvasLinkToken token = this.tokensByAnchorId.get(tokenAnchorId);
			if (token.getDirection() == direction)
			{
				tokens.add(token);
			}
		}
		Collections.sort(tokens, sorter);
	}

	public void setSorter(Comparator<CCanvasLinkToken> sorter)
	{
		this.sorter = sorter;
	}

	public void canvasChanged(long canvas_uuid)
	{
		currentCanvasId = canvas_uuid;
		CanvasTagPanel.getInstance().moveTo(canvas_uuid);
		CanvasLinkPanel.getInstance().moveTo(canvas_uuid);

		CCanvasLinkController.getInstance().showingCanvas(canvas_uuid);

		for (long groupId : CCanvasController.canvasdb.get(canvas_uuid).getChildGroups())
		{
			/**
			 * *** update this ***
			 * 
			 * <pre>
			CanvasBadgeRow row = badgeRowsByGroupId.get(groupId);
			if (row != null)
			{
				row.updateContextHighlight();
			}
			 */
		}
	}

	@Override
	public void groupDeleted(long uuid)
	{
		CCanvasLinkBadge badge = getBadgeForGroup(uuid);
		if (badge == null)
		{
			return;
		}
		CCanvasLinkController.getInstance().deleteLink(badge.getLinkAnchor().getLink().getId());
	}

	@Override
	public void groupMoved(long groupId)
	{
		CanvasBadgeRow row = badgeRowsByGroupId.get(groupId);
		if ((row == null) || !isCurrentlyDisplayed(groupId))
		{
			return;
		}
		row.refreshDisplay();
	}

	private boolean isCurrentlyDisplayed(long groupId)
	{
		if (!CanvasPerspective.getInstance().isActive())
		{
			return false;
		}
		return (CGroupController.groupdb.get(groupId).getCanvasUID() == CCanvasController.getCurrentUUID());
	}

	private CCanvasLinkBadge getBadgeForGroup(long group_uuid)
	{
		for (CCanvasLinkBadge badge : badgesByAnchorId.values())
		{
			if (badge.getLinkAnchor().getGroupId() == group_uuid)
			{
				return badge;
			}
		}
		return null;
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

	private class DefaultSorter implements Comparator<CCanvasLinkToken>
	{
		@Override
		public int compare(CCanvasLinkToken first, CCanvasLinkToken second)
		{
			int comparison = first.getLinkAnchor().getArrowEndpointType().compareTo(second.getLinkAnchor().getArrowEndpointType());
			if (comparison == 0)
			{
				return (int) (first.getLinkAnchor().getCanvasId() - second.getLinkAnchor().getCanvasId());
			}
			return comparison;
		}
	}
}
