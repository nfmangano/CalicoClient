package calico.components.bubblemenu.connectors;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.CConnector;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
import calico.controllers.CStrokeController;
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
			CConnectorController.linearize(uuid);
			//CConnectorController.connectors.get(uuid).moveAnchor(50, 50, CConnector.TYPE_HEAD);
		}
		
		
		ev.stop();
		
		Calico.logger.debug("CLICKED LINEARIZE CONNECTOR BUTTON");
		
		isActive = false;
	}
	
}
