package calico.components.bubblemenu.connectors;

import calico.Calico;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CConnectorController;
import calico.inputhandlers.InputEventInfo;

public class ConnectorLinearizeButton extends PieMenuButton
{
	private boolean isActive = false;
	
	public ConnectorLinearizeButton(long uuid)
	{
		super("arrow.status");
		this.uuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
		if (!CConnectorController.exists(uuid) || isActive)
		{
			return;
		}
		
		isActive = true;
		super.onPressed(ev);
	}
	
	public void onReleased(InputEventInfo ev)
	{
		if (CConnectorController.exists(uuid))
		{
			CConnectorController.connectors.get(uuid).highlight_off();
			CConnectorController.linearize(uuid);
			CConnectorController.connectors.get(uuid).highlight_on();
			BubbleMenu.moveIconPositions(CConnectorController.connectors.get(uuid).getBounds());
		}
		
		ev.stop();
		
		Calico.logger.debug("CLICKED LINEARIZE CONNECTOR BUTTON");
		
		isActive = false;
	}
	
}
