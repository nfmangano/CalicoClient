package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;

public class MoveLinkButton extends PieMenuButton
{
	public MoveLinkButton()
	{
		super(CalicoIconManager.getIconImage("intention-graph.move-link"));
	}
	
	@Override
	public void onClick(InputEventInfo event)
	{
		System.out.println("Move this here link around");
	}
}
