package calico.components.composable.connectors;

import java.awt.Color;

import calico.components.CConnector;
import calico.components.composable.Composable;
import calico.components.composable.ComposableElement;
import calico.controllers.CConnectorController;
import calico.networking.netstuff.ByteUtils;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

public class ColorElement extends ComposableElement {

	private Color newColor, originalColor;
	
	public ColorElement(long uuid, long cuuid, Color newColor, Color originalColor) {
		super(uuid, cuuid);
		
		this.newColor = newColor;
		this.originalColor = originalColor;
	}

	@Override
	public void applyElement() {
		if (!CConnectorController.exists(cuuid))
			return;
		
		CConnector connector = CConnectorController.connectors.get(cuuid);
		connector.setColor(newColor);
	}

	@Override
	public void removeElement() {
		if (!CConnectorController.exists(cuuid))
			return;
		
		CConnector connector = CConnectorController.connectors.get(cuuid);
		connector.setColor(originalColor);
	}
	
	public Composable getComposable()
	{
		return CConnectorController.connectors.get(cuuid);
	}
	
	public CalicoPacket getPacket(long uuid, long cuuid)
	{
		int packetSize = ByteUtils.SIZE_OF_INT + ByteUtils.SIZE_OF_INT + (2 * ByteUtils.SIZE_OF_LONG) + (2 * ByteUtils.SIZE_OF_INT);
		
		CalicoPacket packet = new CalicoPacket(packetSize);
		
		packet.putInt(NetworkCommand.ELEMENT_ADD);
		packet.putInt(ComposableElement.TYPE_COLOR);
		packet.putLong(uuid);
		packet.putLong(cuuid);
		packet.putColor(newColor);
		packet.putColor(originalColor);
		
		return packet;
	}
	
	public CalicoPacket getPacket()
	{
		return getPacket(this.uuid, this.cuuid);
	}
}
