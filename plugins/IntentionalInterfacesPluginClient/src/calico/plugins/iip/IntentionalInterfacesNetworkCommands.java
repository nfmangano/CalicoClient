package calico.plugins.iip;

import calico.networking.netstuff.CalicoPacket;

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
		/**
		 * Create a new CIntentionCell
		 */
		CIC_CREATE,
		/**
		 * Move a CIntentionCell's (x,y) position and set its inUse flag
		 */
		CIC_MOVE,
		/**
		 * Set the title of a CIntentionCell
		 */
		CIC_SET_TITLE,
		/**
		 * Tag a CIntentionCell with a CIntentionType
		 */
		CIC_TAG,
		/**
		 * Untag a CIntentionCell with a CIntentionType
		 */
		CIC_UNTAG,
		/**
		 * Delete a CIntentionCell
		 */
		CIC_DELETE,
		/**
		 * Describe the topology of the CIC clusters
		 */
		CIC_TOPOLOGY,
		/**
		 * Create a new CIntentionType
		 */
		CIT_CREATE,
		/**
		 * Rename a new CIntentionType
		 */
		CIT_RENAME,
		/**
		 * Set the color of a new CIntentionType
		 */
		CIT_SET_COLOR,
		/**
		 * Delete a CIntentionType
		 */
		CIT_DELETE,
		/**
		 * Create a new CCanvasLink
		 */
		CLINK_CREATE,
		/**
		 * Move one enpoint of a CCanvasLink
		 */
		CLINK_MOVE_ANCHOR,
		/**
		 * Set the label of a CCanvasLink
		 */
		CLINK_LABEL,
		/**
		 * Delete a CCanvasLink
		 */
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
