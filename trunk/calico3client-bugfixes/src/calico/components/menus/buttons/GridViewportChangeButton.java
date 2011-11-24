package calico.components.menus.buttons;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import calico.*;
import calico.components.*;
import calico.components.grid.*;
import calico.controllers.CCanvasController;
import calico.controllers.CViewportController;
import calico.iconsets.CalicoIconManager;
import calico.modules.*;
import calico.networking.*;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import edu.umd.cs.piccolo.event.*;



public class GridViewportChangeButton extends GridMenuButton
{
	private static final long serialVersionUID = 1L;
	public static final int BUT_PLUS=0;
	public static final int BUT_MINUS=1;
	//public static final int BUT_ZOOM=2;
	
	private int type;
			
	public GridViewportChangeButton( int type)
	{
		super();		
		this.type=type;
		setPaint(Color.BLACK);//CalicoOptions.getColor("canvas.menubar.button.background_color"));
		try
		{
			switch(type)
			{
				case BUT_MINUS:
					setImage(CalicoIconManager.getIconImage("viewport.minus"));
					break;
					
				case BUT_PLUS:
					setImage(CalicoIconManager.getIconImage("viewport.plus"));					
					break;
					
				
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void actionMouseClicked()
	{			
		CViewportController.dumpImages();
		CViewportController.changeViewPortSize(type, true);
		
	}
	
}