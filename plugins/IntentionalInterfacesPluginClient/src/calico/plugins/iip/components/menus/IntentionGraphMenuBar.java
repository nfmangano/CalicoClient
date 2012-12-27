package calico.plugins.iip.components.menus;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

import calico.CalicoDraw;
import calico.components.menus.CanvasGenericMenuBar;
import calico.components.menus.buttons.ExitButton;
import calico.components.menus.buttons.HistoryNavigationBackButton;
import calico.components.menus.buttons.HistoryNavigationForwardButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.menus.buttons.NewClusterButton;
import calico.plugins.iip.components.menus.buttons.ZoomToExtent;

/**
 * Menu bar for the Intention View. Includes simple buttons and a zoom slider with +/- buttons on either end. Input
 * events intersecting with the slider are forwarded to it.
 * 
 * @author Byron Hawkins
 */
public class IntentionGraphMenuBar extends CanvasGenericMenuBar
{
	private final Rectangle zoomSliderBounds;
	private final IntentionGraphZoomSlider zoomSlider;

	private boolean draggingZoomKnob = false;

	public IntentionGraphMenuBar(int screenPosition)
	{
		super(screenPosition, IntentionGraph.getInstance().getBounds());

		addCap(CanvasGenericMenuBar.ALIGN_START);

//		addIcon(new ZoomToExtent());
		addIcon(new NewClusterButton());

		addSpacer();

		zoomSliderBounds = addIcon(IntentionGraphZoomSlider.SPAN);
		zoomSlider = new IntentionGraphZoomSlider();
//		CalicoDraw.addChildToNode(this, zoomSlider);
//		addChild(zoomSlider);
//		CalicoDraw.setNodeBounds(zoomSlider, zoomSliderBounds);
//		zoomSlider.setBounds(zoomSliderBounds);
//		CalicoDraw.repaintNode(zoomSlider);
//		zoomSlider.repaint();

		addTextEndAligned("  Exit  ", new Font("Verdana", Font.BOLD, 12), new ExitButton());
		addSpacer(ALIGN_END);
//		addIconRightAligned(new HistoryNavigationForwardButton());
//		addIconRightAligned(new HistoryNavigationBackButton());

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

	// @Override
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
