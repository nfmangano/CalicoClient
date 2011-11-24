package calico.plugins.events.groups;

import calico.plugins.events.CalicoEvent;

public class GroupCreate extends CalicoEvent
{
	public long uuid = 0L;
	public GroupCreate(long uuid)
	{
		this.uuid = uuid;
	}
}
