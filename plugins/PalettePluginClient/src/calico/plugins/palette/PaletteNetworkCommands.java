package calico.plugins.palette;

import java.awt.Image;

import calico.networking.netstuff.CalicoPacket;

public class PaletteNetworkCommands {
	
	public static final int PALETTE_PACKET = Command.PALETTE_PACKET.id;
	public static final int PALETTE_SUB_GROUP = Command.PALETTE_SUB_GROUP.id;
	public static final int PALETTE_PASTE = Command.PALETTE_PASTE.id;
	public static final int PALETTE_PASTE_ITEM = Command.PALETTE_PASTE_ITEM.id;
	public static final int PALETTE_SWITCH_VISIBLE_PALETTE = Command.PALETTE_SWITCH_VISIBLE_PALETTE.id;
	public static final int PALETTE_HIDE_MENU_BAR_ICONS = Command.PALETTE_HIDE_MENU_BAR_ICONS.id;
	public static final int PALETTE_SHOW_MENU_BAR_ICONS = Command.PALETTE_SHOW_MENU_BAR_ICONS.id;
	public static final int PALETTE_ITEM_LOAD = Command.PALETTE_ITEM_LOAD.id;
	public static final int PALETTE_LOAD = Command.PALETTE_LOAD.id;
	public static final int PALETTE_DELETE = Command.PALETTE_DELETE.id;
	
	private static final PaletteNetworkCommands instance = new PaletteNetworkCommands();
	
	private PaletteNetworkCommands() { }
	
	public static PaletteNetworkCommands getInstance()
	{
		return instance;
	}

	public enum Command
	{
		PALETTE_PACKET,
		PALETTE_SUB_GROUP,
		PALETTE_PASTE,
		PALETTE_PASTE_ITEM,
		PALETTE_SWITCH_VISIBLE_PALETTE,
		PALETTE_HIDE_MENU_BAR_ICONS,
		PALETTE_SHOW_MENU_BAR_ICONS,
		PALETTE_ITEM_LOAD,
		PALETTE_LOAD,
		PALETTE_DELETE;

		private static final int OFFSET = 2200;
		public final int id;

		private Command()
		{
			this.id = ordinal() + OFFSET;
		}

		public boolean verify(CalicoPacket p)
		{
			return forId(p.getInt()) == this;
		}

		public static Command forId(int id)
		{
			if ((id < OFFSET) || (id > (OFFSET + Command.values().length)))
			{
				return null;
			}
			return Command.values()[id - OFFSET];
		}
	}
	
	public class PALETTE_PACKET extends NetworkCommand {
		long paletteItemUUID;
		long paletteUUID;
		Image img;
		
		public PALETTE_PACKET(CalicoPacket p) {
			super(p);
			
			p.rewind();
			p.getInt();
			this.paletteItemUUID = p.getLong();
			this.paletteUUID = p.getLong();
			this.img = p.getBufferedImage();
		}
		
	}
	
	private abstract class NetworkCommand {
		public NetworkCommand(CalicoPacket p) {
		}
	}
	
}
