package calico.plugins.iip.components.canvas;

import calico.CalicoDataStore;

/**
 * Trivial enum specifying whether an input event occurred on the left or right half of the screen. So far it is only
 * used for choosing which side of the screen to show the tag panel.
 * 
 * @author Byron Hawkins
 */
public enum CanvasInputProximity
{
	LEFT,
	RIGHT,
	NONE;

	public static CanvasInputProximity forPosition(double x)
	{
		if (((int) x) < (CalicoDataStore.ScreenWidth / 2))
		{
			return CanvasInputProximity.LEFT;
		}
		else
		{
			return CanvasInputProximity.RIGHT;
		}
	}
}
