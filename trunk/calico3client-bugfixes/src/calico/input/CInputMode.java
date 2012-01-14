package calico.input;

import java.awt.Image;

import calico.iconsets.CalicoIconManager;

public enum CInputMode
{
	ARROW("mode.arrow"),
	DELETE("mode.delete"),
	EXPERT("mode.expert"),
	SCRAP("mode.scrap"),
	STROKE("mode.stroke"),
	POINTER("mode.pointer");
	
	public final Image image;
	
	private CInputMode(String imageId)
	{
		image = CalicoIconManager.getIconImage(imageId);
	}
}
