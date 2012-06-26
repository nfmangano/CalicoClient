package calico.components.menus.buttons;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CHistoryController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class HistoryNavigationBackButton extends CanvasMenuButton
{
	
	public HistoryNavigationBackButton()
	{
		super();
		
		iconString = "arrow.left";
		try
		{
			setImage(CalicoIconManager.getIconImage(iconString));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public HistoryNavigationBackButton(long canvasId)
	{
		this();

		cuid = canvasId;
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
