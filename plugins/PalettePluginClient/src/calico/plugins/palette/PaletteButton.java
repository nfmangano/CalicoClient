package calico.plugins.palette;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.palette.iconsets.CalicoIconManager;
import edu.umd.cs.piccolo.PCamera;

public class PaletteButton extends CanvasMenuButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public PaletteButton(long c)
	{
		super();
		cuid = c;
		

		iconString = "palette.menubar";
		try
		{
			setImage(CalicoIconManager.getIconImage(iconString));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void actionMouseClicked(InputEventInfo event)
	{
		if (event.getAction() == InputEventInfo.ACTION_PRESSED)
		{
			super.onMouseDown();
		}
		else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
		{
			PalettePlugin.togglePaletteBar();
			super.onMouseUp();
		}
			
	}
	
	

}
