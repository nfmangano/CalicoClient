package calico.plugins.iip.components.piemenu;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.plugins.iip.iconsets.CalicoIconManager;

public class GoToCanvasButton extends PieMenuButton
{
	private long canvasId = 0L;

	public GoToCanvasButton()
	{
		super(CalicoIconManager.getIconImage("intention.go-to-canvas"));
	}

	public void setContext(long canvasId)
	{
		this.canvasId = canvasId;
	}

	@Override
	public void onClick()
	{
		if (canvasId == 0L)
		{
			System.out.println("Warning: go-to-canvas button displayed without having been prepared with a canvas id!");
			return;
		}
		
		System.out.println("Go to canvas #" + canvasId);
		CCanvasController.loadCanvas(canvasId);
	}
}
