package calico.plugins.iip.components.menus;

import java.awt.Point;

import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.iconsets.CalicoIconManager;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;

public class IntentionGraphZoomSlider extends PComposite
{
	public static final int SPAN = 400;

	private final PImage knob;
	private final PImage zoomOutButton;
	private final PImage slider;
	private final PImage zoomInButton;

	private double buttonSpan;

	public IntentionGraphZoomSlider()
	{
		knob = new PImage(CalicoIconManager.getIconImage("intention-graph.zoom-knob"));
		zoomOutButton = new PImage(CalicoIconManager.getIconImage("intention-graph.zoom-out"));
		slider = new PImage(CalicoIconManager.getIconImage("intention-graph.zoom-slider"));
		zoomInButton = new PImage(CalicoIconManager.getIconImage("intention-graph.zoom-in"));

		addChild(zoomOutButton);
		addChild(slider);
		addChild(zoomInButton);
		addChild(knob);
	}

	public void dragTo(Point point)
	{
		PBounds bounds = getBounds();
		double x = (point.x - bounds.x);
		if ((x > buttonSpan) && (x < (bounds.width - buttonSpan)))
		{
			double scale = convertSlidePointToScale(point);
			IntentionGraph.getInstance().getLayer().setScale(scale);
			updateKnobPosition();

			System.out.println("zoom to " + scale);
		}
	}

	public void click(Point point)
	{
		PBounds bounds = getBounds();
		double x = (point.x - bounds.x);

		double scale = IntentionGraph.getInstance().getLayer().getScale();

		if (x < buttonSpan)
		{
			if (scale <= 0.2)
			{
				scale = 0.1;
			}
			else if (scale <= 1.0)
			{
				scale -= 0.1;
			}
			else if (scale < 1.5)
			{
				scale = 0.9;
			}
			else
			{
				scale -= 1.0;
			}

			System.out.println("zoom out a bit");
		}
		else if (x > (bounds.width - buttonSpan))
		{
			if (scale >= 9.0)
			{
				scale = 10.0;
			}
			else if (scale <= 0.9)
			{
				scale += 0.1;
			}
			else if (scale < 0.95)
			{
				scale = 1.5;
			}
			else
			{
				scale += 1.0;
			}
			System.out.println("zoom in a bit");
		}
		else
		{
			scale = convertSlidePointToScale(point);
			System.out.println("zoom to " + scale);
		}

		IntentionGraph.getInstance().getLayer().setScale(scale);
		updateKnobPosition();
	}

	private double convertSlidePointToScale(Point point)
	{
		double x = (point.x - getBounds().x);
		double sliderPosition = (x - buttonSpan);
		double sliderWidth = slider.getBounds().width;
		double sliderCenter = sliderWidth / 2;
		double ratio;
		if (sliderPosition < sliderCenter)
		{
			ratio = sliderPosition / sliderCenter;
		}
		else if (sliderPosition > sliderCenter)
		{
			ratio = 1.0 + (((sliderPosition - sliderCenter) / sliderCenter) * 9.0);
		}
		else
		{
			ratio = 1.0;
		}

		return ratio;
	}

	@Override
	protected void layoutChildren()
	{
		PBounds bounds = getBounds();

		buttonSpan = bounds.height;
		zoomOutButton.setBounds(bounds.x, bounds.y, buttonSpan, buttonSpan);
		zoomInButton.setBounds(bounds.x + bounds.width - buttonSpan, bounds.y, buttonSpan, buttonSpan);

		double sliderWidth = bounds.width - (2 * buttonSpan);
		slider.setBounds(bounds.x + buttonSpan, bounds.y, sliderWidth, bounds.height);

		updateKnobPosition();
	}

	public void updateKnobPosition()
	{
		double knobHeight = getBounds().height / 2.0;
		double knobWidth = knob.getImage().getWidth(null) * (knobHeight / knob.getImage().getHeight(null));

		double scale = IntentionGraph.getInstance().getLayer().getScale();

		// limit extremes
		if (scale < 0.0)
		{
			scale = 0.0;
		}
		else if (scale > 10.0)
		{
			scale = 10.0;
		}

		double ratio = scale;
		if (scale > 1.0)
		{
			ratio = scale / 10.0; // invert to [0.1 - 1.0]
			ratio = (ratio - 0.1) / 0.9; // normalize to [0.0 - 1.0]
		}

		double sliderHalfWidth = (slider.getBounds().width / 2.0);
		double knobHalfWidth = (knobWidth / 2.0);
		double xCenter = sliderHalfWidth - knobHalfWidth;
		double knobCenter;
		if (scale < 1.0)
		{
			double distanceFromCenter = sliderHalfWidth - (sliderHalfWidth * ratio);
			knobCenter = xCenter - distanceFromCenter;
		}
		else if (scale > 1.0)
		{
			double distanceFromCenter = sliderHalfWidth * ratio;
			knobCenter = xCenter + distanceFromCenter;
		}
		else
		{
			knobCenter = xCenter;
		}

		double yOffset = (getBounds().height - knobHeight) / 2.0;
		knob.setBounds(slider.getBounds().x + (knobCenter - knobHalfWidth), getBounds().y + yOffset, knobWidth, knobHeight);
	}
}