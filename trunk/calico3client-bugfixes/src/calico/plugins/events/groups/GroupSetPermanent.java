package calico.plugins.events.groups;

import calico.plugins.events.CalicoEvent;

public class GroupSetPermanent extends CalicoEvent
{
	public long uuid = 0L;
	public GroupSetPermanent(long uuid)
	{
		this.uuid = uuid;
	}
}
