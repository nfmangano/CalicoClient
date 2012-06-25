package calico.components.menus.buttons;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CHistoryController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class HistoryNavigationBackButton extends CanvasMenuButton
{
	
	public HistoryNavigationBackButton()
	{
		setImage(CalicoIconManager.getIconImage("arrow.left"));
	}
	
	public HistoryNavigationBackButton(long canvasId)
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
			CHistoryController.getInstance().back();
			super.onMouseUp();
		}
	}
}
