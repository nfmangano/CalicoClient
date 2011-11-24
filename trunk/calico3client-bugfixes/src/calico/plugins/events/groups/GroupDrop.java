package calico.plugins.events.groups;

import calico.plugins.events.CalicoEvent;

public class GroupDrop extends CalicoEvent
{
	public long uuid = 0L;
	public GroupDrop(long uuid)
	{
		this.uuid = uuid;
	}
}
