package calico.components.menus.buttons;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import calico.*;
import calico.components.*;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.grid.*;
import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import calico.modules.*;
import calico.networking.*;
import calico.networking.netstuff.NetworkCommand;
import calico.perspectives.GridPerspective;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import edu.umd.cs.piccolo.event.*;



public class ReturnToGrid extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	public ReturnToGrid()
	{
		super();
		iconString = "grid.return_to";
		try
		{
			setImage( CalicoIconManager.getIconImage(iconString) );
		}
		catch(Exception e)
		{	
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

			CCanvasController.unloadCanvasImages(CCanvasController.getCurrentUUID());
			Networking.send(NetworkCommand.PRESENCE_LEAVE_CANVAS, CCanvasController.getCurrentUUID(), CCanvasController.getCurrentUUID());
			CCanvasController.setCurrentUUID(0l);
			
			CalicoDataStore.gridObject = CGrid.getInstance();
			CalicoDataStore.gridObject.refreshCells();
			CalicoDataStore.calicoObj.getContentPane().removeAll();
			
			Component[] comps = CalicoDataStore.calicoObj.getContentPane().getComponents();
			
			CalicoDataStore.gridObject.drawBottomToolbar();
			CalicoDataStore.calicoObj.getContentPane().add( CalicoDataStore.gridObject.getComponent() );
			
			for (int i = 0; i < comps.length; i++)
				CalicoDataStore.calicoObj.getContentPane().remove(comps[i]);
			
			CalicoDataStore.calicoObj.pack();
			CalicoDataStore.calicoObj.setVisible(true);
			CalicoDataStore.calicoObj.repaint();
			GridPerspective.getInstance().activate();

			super.onMouseUp();

		}
		
	}
	/*public void actionMousePressed()
	{
		actionMouseClicked();
	}*/
	
	
	
}
