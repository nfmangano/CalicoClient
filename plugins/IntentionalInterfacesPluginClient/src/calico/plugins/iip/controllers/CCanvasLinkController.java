package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkArrow;
import calico.plugins.iip.components.CCanvasLinkBadge;

public class CCanvasLinkController
{
	public static CCanvasLinkController getInstance()
	{
		return INSTANCE;
	}

	private static final CCanvasLinkController INSTANCE = new CCanvasLinkController();

	private static Long2ReferenceArrayMap<CCanvasLink> links = new Long2ReferenceArrayMap<CCanvasLink>();

	public void addLink(CCanvasLink link)
	{
		links.put(link.getId(), link);

		// CGroupController.groupdb.put(uuid, userImage);
		// CCanvasController.canvasdb.get(cuid).getCamera().addChild(
		// CGroupController.groupdb.get(uuid));
		// CGroupController.groupdb.get(uuid).drawPermTemp(true);
		// CGroupController.no_notify_finish(uuid, false);
		
		IntentionPerspectiveController.getInstance().addArrow(new CCanvasLinkArrow(link));
		CanvasPerspectiveController.getInstance().addBadge(new CCanvasLinkBadge(link.getAnchorA()));
		CanvasPerspectiveController.getInstance().addBadge(new CCanvasLinkBadge(link.getAnchorB()));
	}

	public CCanvasLink getLinkById(long uuid)
	{
		return links.get(uuid);
	}

	public void removeLinkById(long uuid)
	{
		links.remove(uuid);
	}
}
