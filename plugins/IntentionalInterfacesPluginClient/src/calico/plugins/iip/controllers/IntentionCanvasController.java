package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import calico.CalicoDataStore;
import calico.components.menus.CanvasStatusBar;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.canvas.CCanvasLinkBadge;
import calico.plugins.iip.components.canvas.CCanvasLinkToken;
import calico.plugins.iip.components.canvas.CanvasBadgeRow;
import calico.plugins.iip.components.canvas.CanvasIntentionToolBar;
import calico.plugins.iip.components.canvas.CanvasIntentionToolBarButton;
import calico.plugins.iip.components.canvas.CanvasLinkBay;
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

		CanvasStatusBar.addMenuButtonRightAligned(CanvasIntentionToolBarButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(ShowIntentionGraphButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(NewIdeaButton.class);
	}

	private static IntentionCanvasController INSTANCE;

	private Long2ReferenceArrayMap<CCanvasLinkBadge> badgesByAnchorId = new Long2ReferenceArrayMap<CCanvasLinkBadge>();
	private Long2ReferenceArrayMap<CCanvasLinkToken> tokensByAnchorId = new Long2ReferenceArrayMap<CCanvasLinkToken>();
	private Long2ReferenceArrayMap<CanvasBadgeRow> badgeRowsByGroupId = new Long2ReferenceArrayMap<CanvasBadgeRow>();

	private long currentCanvasId = 0L;
	private final CanvasLinkBay incomingLinkBay = new CanvasLinkBay(currentCanvasId, CCanvasLink.LinkDirection.INCOMING, new UpperLeftLayout());
	private final CanvasLinkBay outgoingLinkBay = new CanvasLinkBay(currentCanvasId, CCanvasLink.LinkDirection.OUTGOING, new LowerRightLayout());

	private Comparator<CCanvasLinkToken> sorter = new DefaultSorter();

	private boolean linksVisible = false;

	private IntentionCanvasController()
	{
		CGroupController.addListener(this);
	}

	public void toggleLinkVisibility()
	{
		linksVisible = !linksVisible;

		incomingLinkBay.setVisible(linksVisible);
		outgoingLinkBay.setVisible(linksVisible);

		for (CanvasBadgeRow row : badgeRowsByGroupId.values())
		{
			row.setVisible(linksVisible);
		}
	}

	public void addLink(CCanvasLink link)
	{
		switch (link.getLinkType())
		{
			case DESIGN_INSIDE:
				addBadge(link.getAnchorA());
				addToken(link.getAnchorB());
				break;
			default:
				addToken(link.getAnchorA());
				addToken(link.getAnchorB());
				break;
		}
	}

	public void removeLink(CCanvasLink link)
	{
		switch (link.getLinkType())
		{
			case DESIGN_INSIDE:
				removeBadge(link.getAnchorA());
				removeToken(link.getAnchorB());
				break;
			default:
				removeToken(link.getAnchorA());
				removeToken(link.getAnchorB());
				break;
		}
	}

	public void moveLinkAnchor(CCanvasLinkAnchor anchor, long previousCanvasId)
	{
		if (anchor.getCanvasId() == previousCanvasId)
		{
			return;
		}

		if (anchor.hasGroup())
		{
			badgeRowsByGroupId.get(anchor.getGroupId()).updateBadgeCoordinates();
		}

		// the "design inside" source anchor can't be moved, so assume anchor.getLink().getLinkType() != DESIGN_INSIDE
		removeToken(anchor.getId(), previousCanvasId, anchor.getLink().getAnchorA() == anchor);
		addToken(anchor);
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
			row.setVisible(linksVisible);
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
			if (anchor.getLink().getAnchorA() == anchor)
			{
				outgoingLinkBay.refreshLayout();
			}
			else
			{
				incomingLinkBay.refreshLayout();
			}
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
			if (isAnchorA)
			{
				outgoingLinkBay.refreshLayout();
			}
			else
			{
				incomingLinkBay.refreshLayout();
			}
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
		CanvasIntentionToolBar.getInstance().moveTo(canvas_uuid);

		incomingLinkBay.moveTo(canvas_uuid);
		outgoingLinkBay.moveTo(canvas_uuid);

		/**
		 * <pre> not sure this is necessary anymore, badge rows are sticky items
			for (long anchorId : CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(canvas_uuid))
			{
				CCanvasLinkAnchor anchor = CCanvasLinkController.getInstance().getAnchor(anchorId);
				if (!anchor.hasGroup())
				{
					// not all anchors have badges
					continue;
				}

				CCanvasLinkBadge badge = badgesByAnchorId.get(anchorId);
				if (badge == null)
				{
					System.out.println("Warning: badge missing for design-inside anchor " + anchorId);
					continue;
				}

				CCanvasController.canvasdb.get(canvas_uuid).getLayer(CCanvas.Layer.TOOLS).addChild(badge.getImage());
				badge.updatePosition();
			}
		</pre>
		 */
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

	public CanvasLinkBay getIncomingLinkBay()
	{
		return incomingLinkBay;
	}

	public CanvasLinkBay getOutgoingLinkBay()
	{
		return outgoingLinkBay;
	}

	private class UpperLeftLayout implements CanvasLinkBay.Layout
	{
		@Override
		public void updateBounds(PNode node, double width, double height)
		{
			node.setBounds(CanvasLinkBay.BAY_INSET_X, CanvasLinkBay.BAY_INSET_Y, width, height);
		}
	}

	private class LowerRightLayout implements CanvasLinkBay.Layout
	{
		@Override
		public void updateBounds(PNode node, double width, double height)
		{
			double x = CalicoDataStore.ScreenWidth - (CanvasLinkBay.BAY_INSET_X + width);
			double y = CalicoDataStore.ScreenHeight - (CanvasLinkBay.BAY_INSET_Y + height);
			node.setBounds(x, y, width, height);
			
			System.out.println("Positioning link bay at y = " + y);
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
