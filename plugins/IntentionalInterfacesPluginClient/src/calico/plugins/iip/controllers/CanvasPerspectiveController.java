package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import calico.CalicoDataStore;
import calico.controllers.CCanvasController;
import calico.plugins.iip.components.CCanvasLinkBadge;
import calico.plugins.iip.components.canvas.CanvasIntentionToolBar;
import calico.plugins.iip.components.canvas.CanvasLinkBay;

public class CanvasPerspectiveController
{
	public static CanvasPerspectiveController getInstance()
	{
		return INSTANCE;
	}

	private static final CanvasPerspectiveController INSTANCE = new CanvasPerspectiveController();

	private Long2ReferenceArrayMap<CCanvasLinkBadge> badges = new Long2ReferenceArrayMap<CCanvasLinkBadge>();
	private Long2ReferenceArrayMap<List<Long>> badgesByCanvasId = new Long2ReferenceArrayMap<List<Long>>();

	private long currentCanvasId = 0L;
	private CanvasIntentionToolBar toolBar = null;
	private CanvasLinkBay incomingLinkBay;
	private CanvasLinkBay outgoingLinkBay;

	private Comparator<CCanvasLinkBadge> sorter = new DefaultSorter();

	public void addBadge(CCanvasLinkBadge badge)
	{
		badges.put(badge.getId(), badge);
		getBadgesByCanvasId(badge.getCanvasUID()).add(badge.getId());
	}

	public CCanvasLinkBadge getBadgeById(long uuid)
	{
		return badges.get(uuid);
	}

	public void removeBadgeById(long uuid)
	{
		CCanvasLinkBadge badge = badges.remove(uuid);
		getBadgesByCanvasId(badge.getCanvasUID()).remove(badge.getId());
	}

	private List<Long> getBadgesByCanvasId(long canvas_uuid)
	{
		List<Long> badges = badgesByCanvasId.get(canvas_uuid);
		if (badges == null)
		{
			badges = new ArrayList<Long>();
			badgesByCanvasId.put(canvas_uuid, badges);
		}
		return badges;
	}

	public int getBadgeCount(long canvas_uuid)
	{
		return getBadgesByCanvasId(canvas_uuid).size();
	}

	public int getBadgeCount(long canvas_uuid, CCanvasLinkBadge.Type type)
	{
		int count = 0;
		for (long badgeId : getBadgesByCanvasId(canvas_uuid))
		{
			CCanvasLinkBadge badge = badges.get(badgeId);
			if (badge.getType() == type)
			{
				count++;
			}
		}
		return count;
	}

	public void populateBadges(long canvas_uuid, CCanvasLinkBadge.Type type, List<CCanvasLinkBadge> badges)
	{
		badges.clear();

		for (Long badge_uuid : getBadgesByCanvasId(canvas_uuid))
		{
			CCanvasLinkBadge badge = this.badges.get(badge_uuid);
			if (badge.getType() == type)
			{
				badges.add(badge);
			}
		}
		Collections.sort(badges, sorter);
	}

	public void setSorter(Comparator<CCanvasLinkBadge> sorter)
	{
		this.sorter = sorter;
	}

	public void canvasIntentionToolBarCreated(CanvasIntentionToolBar toolBar)
	{
		this.toolBar = toolBar;
		currentCanvasId = toolBar.getCanvasId();

		incomingLinkBay = new CanvasLinkBay(currentCanvasId, CCanvasLinkBadge.Type.INCOMING, new UpperLeftLayout());
		incomingLinkBay.install(CCanvasController.canvasdb.get(currentCanvasId));

		outgoingLinkBay = new CanvasLinkBay(currentCanvasId, CCanvasLinkBadge.Type.OUTGOING, new LowerRightLayout());
		outgoingLinkBay.install(CCanvasController.canvasdb.get(currentCanvasId));
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

	private class DefaultSorter implements Comparator<CCanvasLinkBadge>
	{
		@Override
		public int compare(CCanvasLinkBadge first, CCanvasLinkBadge second)
		{
			int comparison = first.getLink().getType().compareTo(second.getLink().getType());
			if (comparison == 0)
			{
				return (int) (first.getLink().getCanvasId() - second.getLink().getCanvasId());
			}
			return comparison;
		}
	}
}
