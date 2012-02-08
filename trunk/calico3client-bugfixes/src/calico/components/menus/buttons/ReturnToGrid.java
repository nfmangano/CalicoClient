package calico.components.menus.buttons;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import calico.*;
import calico.components.*;
import calico.components.grid.*;
import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.modules.*;
import calico.networking.*;
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
		try
		{
			setImage( CalicoIconManager.getIconImage("grid.return_to") );
		}
		catch(Exception e)
		{	
		}
	}
	
	
	
	public void actionMouseClicked()
	{
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
		
	}
	public void actionMousePressed()
	{
		actionMouseClicked();
	}
	
	
	
}
