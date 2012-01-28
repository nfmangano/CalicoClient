package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
import calico.plugins.iip.components.CCanvasLinkArrow;

public class IntentionPerspectiveController
{
	public static IntentionPerspectiveController getInstance()
	{
		return INSTANCE;
	}

	private static final IntentionPerspectiveController INSTANCE = new IntentionPerspectiveController();

	private static Long2ReferenceArrayMap<CCanvasLinkArrow> arrows = new Long2ReferenceArrayMap<CCanvasLinkArrow>();

	public void addArrow(CCanvasLinkArrow arrow)
	{
		arrows.put(arrow.getId(), arrow);
	}

	public CCanvasLinkArrow getArrowById(long uuid)
	{
		return arrows.get(uuid);
	}

	public void removeArrowById(long uuid)
	{
		arrows.remove(uuid);
	}
}
