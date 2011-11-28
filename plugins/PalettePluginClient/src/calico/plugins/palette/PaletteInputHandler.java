package calico.plugins.palette;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.palette.menuitems.*;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PImage;

public class PaletteInputHandler extends CalicoAbstractInputHandler {
	
	
	PaletteBar paletteBar;
	long paletteUUID;
	long paletteItemUUID = 0;
	PImage ghost;
	int menuItemIndex = -1;
	
	
	public PaletteInputHandler(PaletteBar bar)
	{
		this.paletteBar = bar;
	}

	@Override
	public void actionDragged(InputEventInfo ev) {
		if (ghost != null)
		{
			ghost.setOffset(ev.getX() - ghost.getBounds().width/2, ev.getY() - ghost.getBounds().height/2);
			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera().repaint();
		}
		
	}

	@Override
	public void actionPressed(InputEventInfo ev) { 
		paletteItemUUID = 0;
		menuItemIndex = -1;
		
		for (int i = 0; i < paletteBar.getChildrenCount(); i++)
		{
			if (paletteBar.getChild(i) instanceof PaletteBarItem
					&& ((PaletteBarItem)paletteBar.getChild(i)).getGlobalBounds().contains(ev.getPoint()))
			{
				PaletteBarItem item = (PaletteBarItem)paletteBar.getChild(i);
				paletteItemUUID = item.getUUID();
				if (paletteItemUUID == 0)
					continue;
				
				BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
				img.getGraphics().setColor(Color.black);
				img.getGraphics().fillRect(0, 0, 30, 30);
				ghost = new PImage();
				ghost.setImage(item.getImage());
				ghost.setBounds(item.getBounds());
				ghost.setOffset(ev.getX() - ghost.getBounds().width/2, ev.getY() - ghost.getBounds().height/2);
				ghost.setTransparency(1.0f);
				CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera().addChild(ghost);
				ghost.setVisible(true);
				ghost.repaint();
				return;	
			}
			else if(paletteBar.getChild(i) instanceof PaletteBarMenuItem
					&& ((PaletteBarMenuItem)paletteBar.getChild(i)).getGlobalBounds().contains(ev.getPoint()))
			{
				
				menuItemIndex = i;
			}
		}
		
	}

	@Override
	public void actionReleased(InputEventInfo ev) {
		if (paletteItemUUID > 0)
		{
			PalettePlugin.pastePaletteItem(PalettePlugin.getActivePaletteUUID(), this.paletteItemUUID, CCanvasController.getCurrentUUID(), ev.getX(), ev.getY());
			paletteItemUUID = 0;
			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera().removeChild(ghost);
			ghost = null;
		}
		else if (menuItemIndex > -1)
		{
			((PaletteBarMenuItem)paletteBar.getChild(menuItemIndex)).onClick(ev);
			menuItemIndex = -1;
			
		}
		CalicoInputManager.unlockHandlerIfMatch(paletteBar.getUUID());

	}
	
}
