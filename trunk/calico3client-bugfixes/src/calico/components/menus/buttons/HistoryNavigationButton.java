package calico.components.menus.buttons;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CHistoryController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

public class HistoryNavigationButton extends CanvasMenuButton
{
	public enum Type
	{
		BACK("left"),
		FORWARD("right");
		
		String iconId;

		private Type(String iconId)
		{
			this.iconId = iconId;
		}
	}
	
	private final Type type;
	
	public HistoryNavigationButton(Type type)
	{
		this.type = type;
		iconString = "arrow." + type.iconId;
		
		setImage(CalicoIconManager.getIconImage(iconString));
	}
	
	public HistoryNavigationButton(long canvasId, Type type)
	{
		this(type);
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
			switch (type)
			{
				case BACK:
					CHistoryController.getInstance().back();
					break;
				case FORWARD:
					CHistoryController.getInstance().forward();
					break;
			}
			super.onMouseUp();
		}
	}
}
