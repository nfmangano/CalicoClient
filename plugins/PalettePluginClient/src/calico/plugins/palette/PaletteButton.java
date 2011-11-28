package calico.plugins.palette;

import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
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
		

		
		try
		{
			setImage(CalicoIconManager.getIconImage("palette.menubar"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void actionMouseClicked()
	{
		PalettePlugin.togglePaletteBar();
			
	}
	
	

}
