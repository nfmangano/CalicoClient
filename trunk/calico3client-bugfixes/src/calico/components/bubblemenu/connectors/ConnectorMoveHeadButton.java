package calico.components.bubblemenu.connectors;

import java.awt.Point;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.CConnector;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.decorators.CGroupDecorator;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.InputEventInfo;

public class ConnectorMoveHeadButton extends PieMenuButton
{
	private boolean isActive = false;

	private Point prevPoint, mouseDownPoint;
	private CConnector tempConnector;
	private long tempGuuid;
	
	public ConnectorMoveHeadButton(long uuid)
	{
		super("connector.point");
		draggable = true;
		this.uuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
		if (!CConnectorController.exists(uuid) || isActive)
		{
			return;
		}
		
		prevPoint = new Point();
		
		prevPoint.x = 0;
		prevPoint.y = 0;
		mouseDownPoint = null;
		tempConnector = CConnectorController.connectors.get(uuid);
		tempGuuid = 0l;
		
		ev.stop();
		BubbleMenu.isPerformingBubbleMenuAction = true;
		
		
		isActive = true;
		super.onPressed(ev);
	}
	
	public void onDragged(InputEventInfo ev)
	{
		if (mouseDownPoint == null)
		{
			prevPoint.x = ev.getPoint().x;
			prevPoint.y = ev.getPoint().y;
			mouseDownPoint = ev.getPoint();
			CConnectorController.move_group_anchor_start(uuid, CConnector.TYPE_HEAD);
		}

		CConnectorController.move_group_anchor(uuid, CConnector.TYPE_HEAD, (int)(ev.getPoint().x - prevPoint.x), ev.getPoint().y - prevPoint.y);
			
		//Change the highlight of the group associated with point B
		long guuid = CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), this.tempConnector.getHead());

		if (guuid != tempGuuid)
		{
			if (tempGuuid != 0l)
			{
				CGroupController.groupdb.get(tempGuuid).highlight_off();
				CGroupController.groupdb.get(tempGuuid).highlight_repaint();
			}
			if (guuid != 0l && !(CGroupController.groupdb.get(guuid) instanceof CGroupDecorator))
			{
				CGroupController.groupdb.get(guuid).highlight_on();
				CGroupController.groupdb.get(guuid).highlight_repaint();
			}
			tempGuuid = guuid;
		}
		
		prevPoint.x = ev.getPoint().x;
		prevPoint.y = ev.getPoint().y;
		ev.stop();
	}
	
	public void onReleased(InputEventInfo ev)
	{
		if (tempGuuid != 0l)
		{
			CGroupController.groupdb.get(tempGuuid).highlight_off();
			CGroupController.groupdb.get(tempGuuid).highlight_repaint();
		}
		
		CConnectorController.move_group_anchor_end(uuid, CConnector.TYPE_HEAD);
		
		ev.stop();
		
		Calico.logger.debug("CLICKED MOVE HEAD BUTTON");
		
		isActive = false;
	}
	
	@Override
	public Point getPreferredPosition()
	{
		return CConnectorController.connectors.get(uuid).getHead();
	}
	
}
