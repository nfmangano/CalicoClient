package calico.components.bubblemenu.strokes;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.InputEventInfo;

public class StrokeMakeConnectorButton extends PieMenuButton
{
	private boolean isActive = false;
	
	public StrokeMakeConnectorButton(long uuid)
	{
		super("connector.create");
		this.uuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
		if (!CStrokeController.exists(uuid) || isActive)
		{
			return;
		}
		
		isActive = true;
		super.onPressed(ev);
	}
	
	public void onReleased(InputEventInfo ev)
	{
		if (CStrokeController.exists(uuid))
		{
			long new_uuid = Calico.uuid();
			//Create the connector
			CConnectorController.create(new_uuid, CCanvasController.getCurrentUUID(), CalicoDataStore.PenColor, 
					CalicoDataStore.PenThickness, CStrokeController.strokes.get(uuid).getRawPolygon(), 0l, 0l);
			CStrokeController.strokes.get(uuid).highlight_off();
			CStrokeController.delete(uuid);

			
			CConnectorController.show_stroke_bubblemenu(new_uuid, false);
			
		}
		
		
		ev.stop();
		
		Calico.logger.debug("CLICKED MAKE CONNECTOR BUTTON");
		
		isActive = false;
	}
	
}
