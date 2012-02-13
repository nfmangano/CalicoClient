package calico.plugins.iip;

import calico.networking.netstuff.CalicoPacket;

public class IntentionalInterfacesNetworkCommands
{
	// According to current policy, the client should receive but never send this event. Only the server creates CICs.
	@Deprecated
	public static final int CIC_CREATE = Command.CIC_CREATE.id;

	// This should never occur, according to current policy. The set of CICs is static, one per canvas.
	@Deprecated
	public static final int CIC_DELETE = Command.CIC_DELETE.id;

	public static final int CIC_MOVE = Command.CIC_MOVE.id;
	public static final int CLINK_CREATE = Command.CLINK_CREATE.id;
	public static final int CLINK_RETYPE = Command.CLINK_RETYPE.id;
	public static final int CLINK_MOVE = Command.CLINK_MOVE.id;
	public static final int CLINK_DELETE = Command.CLINK_DELETE.id;

	public enum Command
	{
		/**
		 * Create a new CIntentionCell
		 */
		CIC_CREATE,
		/**
		 * Move a CIntentionCell's (x,y) position
		 */
		CIC_MOVE,
		/**
		 * Delete a CIntentionCell
		 */
		CIC_DELETE,
		/**
		 * Create a new CCanvasLink
		 */
		CLINK_CREATE,
		/**
		 * Change the type of a CCanvasLink
		 */
		CLINK_RETYPE,
		/**
		 * Move one enpoint of a CCanvasLink
		 */
		CLINK_MOVE,
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
