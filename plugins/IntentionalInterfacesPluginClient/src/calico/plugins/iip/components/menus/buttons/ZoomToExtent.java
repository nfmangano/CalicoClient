package calico.plugins.iip.components.menus.buttons;

import calico.components.menus.CanvasMenuButton;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class ZoomToExtent extends CanvasMenuButton
{
	public ZoomToExtent()
	{
		setImage(CalicoIconManager.getIconImage("intention-graph.zoom-to-extent"));
	}
	
	@Override
	public void actionMouseClicked()
	{
		IntentionGraph.getInstance().fitContents();
	}
}
