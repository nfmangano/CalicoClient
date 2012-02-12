package calico.plugins.iip.inputhandlers;

import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;

public class CIntentionCellInputHandler extends CalicoAbstractInputHandler
{
	public static CIntentionCellInputHandler getInstance()
	{
		return INSTANCE;
	}

	private static final CIntentionCellInputHandler INSTANCE = new CIntentionCellInputHandler();

	private long currentCellId;

	public void setCurrentCellId(long currentCellId)
	{
		this.currentCellId = currentCellId;
	}

	@Override
	public void actionDragged(InputEventInfo event)
	{
	}

	@Override
	public void actionPressed(InputEventInfo event)
	{
		if (event.isLeftButtonPressed())
		{
			PieMenu.displayPieMenu(event.getGlobalPoint(), new PieMenuButton[0]);
			// CalicoInputManager.rerouteEvent(this.canvas_uid, e); ???
		}
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
	}
}
