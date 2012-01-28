package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
import calico.plugins.iip.components.CCanvasLinkBadge;

public class CanvasPerspectiveController
{
	public static CanvasPerspectiveController getInstance()
	{
		return INSTANCE;
	}

	private static final CanvasPerspectiveController INSTANCE = new CanvasPerspectiveController();

	private static Long2ReferenceArrayMap<CCanvasLinkBadge> badges = new Long2ReferenceArrayMap<CCanvasLinkBadge>();

	public void addBadge(CCanvasLinkBadge badge)
	{
		badges.put(badge.getId(), badge);
	}

	public CCanvasLinkBadge getBadgeById(long uuid)
	{
		return badges.get(uuid);
	}

	public void removeBadgeById(long uuid)
	{
		badges.remove(uuid);
	}
}
