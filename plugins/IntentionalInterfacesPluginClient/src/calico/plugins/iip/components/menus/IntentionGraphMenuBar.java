package calico.plugins.iip.components.menus;

import java.awt.Point;
import java.awt.Rectangle;

import calico.components.menus.CanvasGenericMenuBar;
import calico.components.menus.buttons.ReturnToGrid;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.graph.NewIdeaButton;
import calico.plugins.iip.components.menus.buttons.ZoomToExtent;

public class IntentionGraphMenuBar extends CanvasGenericMenuBar
{
	private final Rectangle zoomSliderBounds;
	private final IntentionGraphZoomSlider zoomSlider;

	public IntentionGraphMenuBar(int screenPosition)
	{
		super(screenPosition, IntentionGraph.getInstance().getBounds());

		addCap(CanvasGenericMenuBar.ALIGN_START);

		addIcon(new ReturnToGrid());
		addSpacer();
		addIcon(new ZoomToExtent());
		addSpacer();
		addIcon(new NewIdeaButton());

		addSpacer();

		zoomSliderBounds = addIcon(IntentionGraphZoomSlider.SPAN);
		zoomSlider = new IntentionGraphZoomSlider();
		addChild(zoomSlider);
		zoomSlider.setBounds(zoomSliderBounds);
		zoomSlider.repaint();

		addSpacer();
	}

	public void processEvent(InputEventInfo event)
	{
		switch (event.getAction())
		{
			case InputEventInfo.ACTION_RELEASED:
				clickMenu(event.getGlobalPoint());
				break;
			case InputEventInfo.ACTION_DRAGGED:
				if (zoomSliderBounds.contains(event.getGlobalPoint()))
				{
					zoomSlider.dragTo(event.getGlobalPoint());
				}
				break;
		}
	}

	@Override
	public void clickMenu(Point point)
	{
		if (zoomSliderBounds.contains(point))
		{
			zoomSlider.click(point);
		}
		else
		{
			super.clickMenu(point);
		}
	}
}
