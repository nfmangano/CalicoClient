package calico.plugins.iip;

import calico.networking.netstuff.CalicoPacket;

/**
 * Defines the network commands for the Intention View. See the server plugin's peer of this class for more details
 * about individual commands.
 * 
 * @author Byron Hawkins
 */
public class IntentionalInterfacesNetworkCommands
{
	public static final int CIC_CREATE = Command.CIC_CREATE.id;
	public static final int CIC_DELETE = Command.CIC_DELETE.id;
	public static final int CIC_MOVE = Command.CIC_MOVE.id;
	public static final int CIC_SET_TITLE = Command.CIC_SET_TITLE.id;
	public static final int CIC_TAG = Command.CIC_TAG.id;
	public static final int CIC_UNTAG = Command.CIC_UNTAG.id;
	public static final int CIC_TOPOLOGY = Command.CIC_TOPOLOGY.id;
	public static final int CIT_CREATE = Command.CIT_CREATE.id;
	public static final int CIT_RENAME = Command.CIT_RENAME.id;
	public static final int CIT_SET_COLOR = Command.CIT_SET_COLOR.id;
	public static final int CIT_DELETE = Command.CIT_DELETE.id;
	public static final int CLINK_CREATE = Command.CLINK_CREATE.id;
	public static final int CLINK_MOVE_ANCHOR = Command.CLINK_MOVE_ANCHOR.id;
	public static final int CLINK_LABEL = Command.CLINK_LABEL.id;
	public static final int CLINK_DELETE = Command.CLINK_DELETE.id;

	public enum Command
	{
		CIC_CREATE,
		CIC_MOVE,
		CIC_SET_TITLE,
		CIC_TAG,
		CIC_UNTAG,
		CIC_DELETE,
		CIC_TOPOLOGY,
		CIC_CLUSTER_GRAPH,
		CIT_CREATE,
		CIT_RENAME,
		CIT_SET_COLOR,
		CIT_DELETE,
		CLINK_CREATE,
		CLINK_MOVE_ANCHOR,
		CLINK_LABEL,
		CLINK_DELETE;

		public final int id;

		private Command()
		{
			this.id = ordinal() + OFFSET;
		}

		public boolean verify(CalicoPacket p)
		{
			return forId(p.getInt()) == this;
		}

		private static final int OFFSET = 2300;

		public static Command forId(int id)
		{
			if ((id < OFFSET) || (id > (OFFSET + Command.values().length)))
			{
				return null;
			}
			return Command.values()[id - OFFSET];
		}
	}
}
