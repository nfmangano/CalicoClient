package calico.plugins.iip.components.canvas;

import java.awt.Color;
import java.awt.Image;

import calico.components.menus.CanvasMenuButton;
import calico.iconsets.CalicoIconManager;

public abstract class CanvasIntentionToolBarItem extends CanvasMenuButton
{
	protected long canvas_uuid;

	public CanvasIntentionToolBarItem(String imageId)
	{
		// TODO: it may not work to use the client's icon manager
		Image image = CalicoIconManager.getIconImage(imageId);
		setImage(image);
		setPaint(Color.white);
	}
	
	protected abstract void onClick();
	
	@Override
	public final void actionMouseClicked()
	{
		onClick();
	}
	
	public void setCanvasId(long canvas_uuid)
	{
		this.canvas_uuid = canvas_uuid;
	}
}
