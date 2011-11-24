package calico.plugins.events.groups;

import calico.plugins.events.CalicoEvent;

public class GroupDelete extends CalicoEvent
{
	public long uuid = 0L;
	public GroupDelete(long uuid)
	{
		this.uuid = uuid;
	}
}
