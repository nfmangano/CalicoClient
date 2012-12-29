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
import calico.inputhandlers.InputEventInfo;
import calico.modules.*;
import calico.networking.*;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import edu.umd.cs.piccolo.event.*;



public class CanvasNavButtonLeft extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private long canvasuid = 0L;
		
	public CanvasNavButtonLeft(long cuid)
	{
		super();
		
		canvasuid = cuid;
		iconString = "arrow.left";
		setImage(CalicoIconManager.getIconImage(iconString));

	}
	
	private static void loadCanvas(int x, int y)
	{
		long cuid = CGrid.getCanvasAtPos(x, y);
		
		if(cuid==0L)
		{
			// Error
			return;
		}
		CCanvasController.unloadCanvasImages(CCanvasController.getCurrentUUID());
		CCanvasController.loadCanvas(cuid);
	}
	
	public void actionMouseClicked(InputEventInfo event)//long cuid, int type)
	{
		if (event.getAction() == InputEventInfo.ACTION_PRESSED)
		{
			super.onMouseDown();
		}
		else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
		{
			// Grid Size
			int gridx = CGrid.GridCols-1;
			int gridy = CGrid.GridRows-1;
					
			// Canvas Coords
			int xpos = CGrid.getCanvasColumn(canvasuid);
			int ypos = CGrid.getCanvasRow(canvasuid);
			
			if((xpos-1)>=0)
			{
				loadCanvas(xpos-1,ypos);
			}
			else
			{
				loadCanvas(gridx,ypos);
			}

			
			super.onMouseUp();
		}
	}
	/*public void actionMousePressed()//long cuid, int type)
	{
		actionMouseClicked();
	}*/
	
}