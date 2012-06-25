package calico.components.menus.buttons;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CHistoryController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class HistoryNavigationForwardButton extends CanvasMenuButton
{

	
	public HistoryNavigationForwardButton()
	{
		setImage(CalicoIconManager.getIconImage("arrow.right"));
	}
	
	public HistoryNavigationForwardButton(long canvasId)
	{
		this();
	}
	
	@Override
	public void actionMouseClicked(InputEventInfo event)
	{
		if (event.getAction() == InputEventInfo.ACTION_PRESSED)
		{
			super.onMouseDown();
		}
		else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
		{
			CHistoryController.getInstance().forward();
			super.onMouseUp();
		}
	}
}
