package calico.plugins.iip.components.piemenu;

import java.awt.Point;
import java.util.TimerTask;

import calico.CalicoOptions;
import calico.inputhandlers.CalicoAbstractInputHandler.MenuAnimation;
import edu.umd.cs.piccolo.PLayer;

/**
 * Pie menu animation task.
 *
 * @author Byron Hawkins
 */
public abstract class PieMenuTimerTask extends TimerTask
{
	private PLayer layer;
	private Point point;

	protected abstract void animationCompleted();

	protected void startAnimation(PLayer layer, Point point)
	{
		this.layer = layer;
		this.point = point;

		Animation animation = new Animation();
		animation.start();
	}

	private class Animation extends MenuAnimation
	{
		Animation()
		{
			super(layer, CalicoOptions.pen.press_and_hold_menu_animation_duration, CalicoOptions.core.max_hold_distance, point);

			setStartTime(System.currentTimeMillis());
			setStepRate(CalicoOptions.pen.press_and_hold_menu_animation_tick_rate);
		}

		@Override
		protected void activityStep(long elapsedTime)
		{
			super.activityStep(elapsedTime);

			if (animateStep(elapsedTime))
			{
				terminate();
			}
		}

		@Override
		protected void activityFinished()
		{
			cleanup();
			animationCompleted();
		}
	}
}
