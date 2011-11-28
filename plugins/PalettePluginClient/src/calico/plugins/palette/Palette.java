package calico.plugins.palette;

import java.util.ArrayList;
import calico.Calico;
import calico.events.CalicoEventHandler;
import calico.networking.Networking;
import calico.networking.netstuff.ByteUtils;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

public class Palette {
	
	long uuid;
	ArrayList<CalicoPacket> paletteItems;
	
	public Palette(long paletteUUID)
	{
		this.uuid = paletteUUID;
		paletteItems = new ArrayList<CalicoPacket>();
	}
	
	public void setPaletteItems(CalicoPacket[] items)
	{
		
	}
	
	public void setPaletteItem(CalicoPacket p, int index)
	{
		
	}
	
	public void addPaletteItemToPalette(CalicoPacket p)
	{
		paletteItems.add(p);
	}
	
	public ArrayList<CalicoPacket> getPaletteItems()
	{
		return paletteItems;
	}

	public long getUUID() {

		return uuid;
		
	}

	public boolean contains(long itemUUID) {
		for (CalicoPacket p : paletteItems)
		{
			p.rewind();
			p.getInt();
			if (p.getLong() == itemUUID)
				return true;
		}
		return false;
	}

	public CalicoPacket getItem(long paletteItemUUID) {
		for (CalicoPacket p : paletteItems)
		{
			p.rewind();
			p.getInt();
			if (p.getLong() == paletteItemUUID)
				return p;
		}
		return null;
	}
	
	public CalicoPacket getUpdatePacket()
	{
		//get size of packet
		int size = ByteUtils.SIZE_OF_INT * 2 + ByteUtils.SIZE_OF_LONG;
		for (CalicoPacket cp : paletteItems)
		{
			size += ByteUtils.SIZE_OF_INT;
			size += cp.getLength();
		}

		//Construct packet
		CalicoPacket p = new CalicoPacket(size);
		p.putInt(PaletteNetworkCommands.PALETTE_LOAD);
		p.putLong(uuid);
		p.putInt(paletteItems.size());
		for (CalicoPacket cp : paletteItems)
		{
			p.putInt(cp.getLength());
			p.putByte(cp.getBuffer());
		}
		
		return p;
	}
	
	

}
