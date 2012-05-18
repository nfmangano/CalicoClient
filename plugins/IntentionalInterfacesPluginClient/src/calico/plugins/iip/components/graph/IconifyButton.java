package calico.plugins.iip.components.graph;

import calico.components.menus.CanvasMenuButton;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.iconsets.CalicoIconManager;
import edu.umd.cs.piccolo.util.PBounds;

public class IconifyButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;

	private long currentCanvasId;

	private boolean active = true;

	public IconifyButton()
	{
		this(0L);
	}

	/**
	 * Invoked via reflection in CanvasStatusBar
	 */
	public IconifyButton(long canvas_uuid)
	{
		try
		{
			this.currentCanvasId = canvas_uuid;

			updateImage();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void actionMouseClicked()
	{
		active = !active;
		updateImage();

		CIntentionCellController.getInstance().activateIconifyMode(active);
	}

	private void updateImage()
	{
		PBounds bounds = getBounds();

		if (active)
		{
			setImage(CalicoIconManager.getIconImage("intention.iconify-on-button"));
		}
		else
		{
			setImage(CalicoIconManager.getIconImage("intention.iconify-off-button"));
		}

		setBounds(bounds);
	}
}
