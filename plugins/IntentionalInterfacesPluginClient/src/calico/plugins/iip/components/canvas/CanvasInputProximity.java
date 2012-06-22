package calico.plugins.iip.components.canvas;

import calico.CalicoDataStore;

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
