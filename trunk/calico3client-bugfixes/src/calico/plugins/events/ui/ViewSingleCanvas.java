package calico.plugins.events.ui;

import calico.plugins.events.CalicoEvent;

public class ViewSingleCanvas extends CalicoEvent
{
	public long canvas_uuid = 0L;
	
	public ViewSingleCanvas(long uuid)
	{
		this.canvas_uuid = uuid;
	}
}
