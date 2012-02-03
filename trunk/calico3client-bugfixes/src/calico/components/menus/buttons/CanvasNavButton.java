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

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import edu.umd.cs.piccolo.event.*;



public class CanvasNavButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private long canvasuid = 0L;
	private int button_type = 1;
	
	public static final int TYPE_UP		= 1 << 0;
	public static final int TYPE_DOWN	= 1 << 1;
	public static final int TYPE_LEFT	= 1 << 2;
	public static final int TYPE_RIGHT	= 1 << 3;
		
	public CanvasNavButton(long cuid, int type)
	{
		super();
		
		button_type = type;
		canvasuid = cuid;
		
		try
		{
			switch(type)
			{
				case CanvasNavButton.TYPE_DOWN:
					setImage(CalicoIconManager.getIconImage("arrow.down"));
					break;
					
				case CanvasNavButton.TYPE_UP:
					setImage(CalicoIconManager.getIconImage("arrow.up"));
					break;
					
				case CanvasNavButton.TYPE_LEFT:
					setImage(CalicoIconManager.getIconImage("arrow.left"));
					break;
					
				case CanvasNavButton.TYPE_RIGHT:
					setImage(CalicoIconManager.getIconImage("arrow.right"));
					break;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private static void loadCanvas(int x, int y)
	{
		long cuid = CCanvasController.getCanvasAtPos(x, y);
		
		if(cuid==0L)
		{
			// Error
			return;
		}
		
		CCanvasController.loadCanvas(cuid);
	}
	
	public void actionMouseClicked()//long cuid, int type)
	{
		// Grid Size
		int gridx = CalicoDataStore.GridCols-1;
		int gridy = CalicoDataStore.GridRows-1;
				
		// Canvas Coords
		int xpos = CCanvasController.canvasdb.get(canvasuid).getGridCol();
		int ypos = CCanvasController.canvasdb.get(canvasuid).getGridRow();
		
		if(CViewportCanvas.PERSPECTIVE.isActive()){			
			CGrid.getInstance().moveViewPort(button_type);
			CViewportCanvas.getInstance().rezoomCamera();
			return;
		}		
		switch(button_type)
		{
			case CanvasNavButton.TYPE_DOWN:
				if((ypos+1)<=gridy)
				{
					
					
					loadCanvas(xpos,ypos+1);
					
				}
				else
				{
					loadCanvas(xpos,0);
				}
				break;
				
			case CanvasNavButton.TYPE_UP:
				if((ypos-1)>=0)
				{
					loadCanvas(xpos,ypos-1);
				}
				else
				{
					loadCanvas(xpos,gridy);
				}
				break;
				
			case CanvasNavButton.TYPE_LEFT:
				if((xpos-1)>=0)
				{
					loadCanvas(xpos-1,ypos);
				}
				else
				{
					loadCanvas(gridx,ypos);
				}
				break;
				
			case CanvasNavButton.TYPE_RIGHT:
				if((xpos+1)<=gridx)
				{
					loadCanvas(xpos+1,ypos);
				}
				else
				{
					loadCanvas(0,ypos);
				}
				break;
		}
	}
	public void actionMousePressed()//long cuid, int type)
	{
		actionMouseClicked();
	}
	
}