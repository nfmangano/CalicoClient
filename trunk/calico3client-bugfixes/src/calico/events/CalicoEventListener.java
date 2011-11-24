package calico.events;

import calico.networking.netstuff.CalicoPacket;

public interface CalicoEventListener {
	
	public void handleCalicoEvent(int event, CalicoPacket p);

}
