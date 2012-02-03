package calico.plugins.iip.components.menus;

import calico.components.menus.CanvasGenericMenuBar;
import calico.components.menus.buttons.ReturnToGrid;
import calico.plugins.iip.components.graph.IntentionGraph;

public class IntentionGraphMenuBar extends CanvasGenericMenuBar
{
	public IntentionGraphMenuBar(int screenPosition)
	{
		super(screenPosition, IntentionGraph.getInstance().getBounds());

		addCap(CanvasGenericMenuBar.ALIGN_START);

		addIcon(new ReturnToGrid());

		addSpacer();
	}
}
