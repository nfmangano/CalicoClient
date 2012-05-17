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

public class ConnectorMakeStrokeButton extends PieMenuButton
{
	private boolean isActive = false;
	
	public ConnectorMakeStrokeButton(long uuid)
	{
		super("connector.strokify");
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
			CConnectorController.make_stroke(uuid);
		}
		
		
		ev.stop();
		
		Calico.logger.debug("CLICKED MAKE STROKE BUTTON");
		
		isActive = false;
	}
	
}
