package calico.plugins.iip.components.menus;

import java.awt.Point;
import java.awt.Rectangle;

import calico.components.menus.CanvasGenericMenuBar;
import calico.components.menus.buttons.ReturnToGrid;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.CopyCanvasButton;
import calico.plugins.iip.components.graph.IconifyButton;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.graph.NewCanvasButton;
import calico.plugins.iip.components.menus.buttons.ZoomToExtent;

public class IntentionGraphMenuBar extends CanvasGenericMenuBar
{
	private final Rectangle zoomSliderBounds;
	private final IntentionGraphZoomSlider zoomSlider;

	private boolean draggingZoomKnob = false;

	public IntentionGraphMenuBar(int screenPosition)
	{
		super(screenPosition, IntentionGraph.getInstance().getBounds());

		addCap(CanvasGenericMenuBar.ALIGN_START);

		addIcon(new ZoomToExtent());

		addSpacer();

		zoomSliderBounds = addIcon(IntentionGraphZoomSlider.SPAN);
		zoomSlider = new IntentionGraphZoomSlider();
		addChild(zoomSlider);
		zoomSlider.setBounds(zoomSliderBounds);
		zoomSlider.repaint();
	}

	public void initialize()
	{
		zoomSlider.refreshState();
	}

	public void processEvent(InputEventInfo event)
	{
		switch (event.getAction())
		{
			case InputEventInfo.ACTION_PRESSED:
				draggingZoomKnob = zoomSliderBounds.contains(event.getGlobalPoint());
				clickMenu(event, event.getGlobalPoint());
				break;
			case InputEventInfo.ACTION_RELEASED:
				draggingZoomKnob = false;
				clickMenu(event, event.getGlobalPoint());
				break;
			case InputEventInfo.ACTION_DRAGGED:
				if (draggingZoomKnob && zoomSliderBounds.contains(event.getGlobalPoint()))
				{
					zoomSlider.dragTo(event.getGlobalPoint());
				}
				break;
		}
	}

	@Override
	public void clickMenu(InputEventInfo event, Point point)
	{
		if (zoomSliderBounds.contains(point))
		{
			if (event.getAction() == InputEventInfo.ACTION_PRESSED)
			{
				zoomSlider.click(point);
			}
		}
		else
		{
			super.clickMenu(event, point);
		}
	}
}
