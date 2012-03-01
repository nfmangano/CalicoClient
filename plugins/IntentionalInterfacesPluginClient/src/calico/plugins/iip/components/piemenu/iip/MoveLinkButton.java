package calico.plugins.iip.components.piemenu.iip;

import calico.components.piemenu.PieMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class MoveLinkButton extends PieMenuButton
{
	private CCanvasLink link = null;
	private boolean isOnSideA;
	private boolean enabled;

	public MoveLinkButton()
	{
		super(CalicoIconManager.getIconImage("intention-graph.move-link"));
	}
	
	public void setContext(CCanvasLink link, boolean isOnSideA)
	{
		this.link = link;
		this.isOnSideA = isOnSideA;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	@Override
	public void onClick(InputEventInfo event)
	{
		System.out.println("Move this here link around");

		CreateIntentionArrowPhase.INSTANCE.startMove(link, isOnSideA);
	}
}
