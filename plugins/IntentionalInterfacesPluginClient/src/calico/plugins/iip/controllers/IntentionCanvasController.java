package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import calico.CalicoDataStore;
import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLink.LinkType;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.canvas.CCanvasLinkBadge;
import calico.plugins.iip.components.canvas.CCanvasLinkToken;
import calico.plugins.iip.components.canvas.CanvasIntentionToolBar;
import calico.plugins.iip.components.canvas.CanvasLinkBay;

public class IntentionCanvasController implements CGroupController.Listener
{
	public static IntentionCanvasController getInstance()
	{
		return INSTANCE;
	}

	public static void initialize()
	{
		INSTANCE = new IntentionCanvasController();
	}

	private static IntentionCanvasController INSTANCE;

	private Long2ReferenceArrayMap<CCanvasLinkBadge> badgesByAnchorId = new Long2ReferenceArrayMap<CCanvasLinkBadge>();
	private Long2ReferenceArrayMap<CCanvasLinkToken> tokensByAnchorId = new Long2ReferenceArrayMap<CCanvasLinkToken>();

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
		
		for (CCanvasLinkBadge badge : badgesByAnchorId.values())
		{
			badge.setVisible(linksVisible);
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

	private void addBadge(CCanvasLinkAnchor anchor)
	{
		CCanvasLinkBadge badge = new CCanvasLinkBadge(anchor);
		badgesByAnchorId.put(badge.getLinkAnchor().getId(), badge);
		badge.setVisible(linksVisible);

		CCanvasController.canvasdb.get(anchor.getCanvasId()).getLayer(CCanvas.Layer.TOOLS).addChild(badge.getImage());
		badge.updatePosition();
	}

	public CCanvasLinkBadge getBadgeByAnchorId(long anchor_uuid)
	{
		return badgesByAnchorId.get(anchor_uuid);
	}

	private void removeBadge(CCanvasLinkAnchor anchor)
	{
		CCanvasLinkBadge badge = badgesByAnchorId.remove(anchor.getId());
		removeBadgeFromCanvas(badge);
	}

	private void removeBadgeFromCanvas(CCanvasLinkBadge badge)
	{
		CCanvasController.canvasdb.get(badge.getLinkAnchor().getCanvasId()).getLayer(CCanvas.Layer.TOOLS).removeChild(badge.getImage());
		badge.cleanup();
	}

	private void addToken(CCanvasLinkAnchor anchor)
	{
		CCanvasLinkToken token = new CCanvasLinkToken(anchor);
		tokensByAnchorId.put(token.getLinkAnchor().getId(), token);

		if (anchor.getCanvasId() == currentCanvasId)
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
		tokensByAnchorId.remove(anchor.getId());
		CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(anchor.getCanvasId()).remove(anchor.getId());

		if (anchor.getCanvasId() == currentCanvasId)
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
	public void groupMovedBy(long uuid)
	{
		CCanvasLinkBadge badge = getBadgeForGroup(uuid);
		if ((badge == null) || !isCurrentlyDisplayed(badge))
		{
			return;
		}
		badge.updatePosition();
	}

	private boolean isCurrentlyDisplayed(CCanvasLinkBadge badge)
	{
		if (!CanvasPerspective.getInstance().isActive())
		{
			return false;
		}
		return (badge.getLinkAnchor().getCanvasId() == CCanvasController.getCurrentUUID());
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
		public void updateBounds(Rectangle2D bounds, double width, double height)
		{
			bounds.setRect(CanvasLinkBay.BAY_INSET_X, CanvasLinkBay.BAY_INSET_Y, width, height);
		}
	}

	private class LowerRightLayout implements CanvasLinkBay.Layout
	{
		@Override
		public void updateBounds(Rectangle2D bounds, double width, double height)
		{
			double x = CalicoDataStore.ScreenWidth - (CanvasLinkBay.BAY_INSET_X + width);
			double y = CalicoDataStore.ScreenHeight - (CanvasLinkBay.BAY_INSET_Y + height);
			bounds.setRect(x, y, width, height);
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
