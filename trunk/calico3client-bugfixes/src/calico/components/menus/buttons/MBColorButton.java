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
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import edu.umd.cs.piccolo.event.*;



public class MBColorButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private long cuid = 0L;
	private Color color = Color.BLACK;
		
	public MBColorButton(long c, Color col, Rectangle2D bounds)
	{
		super();
		cuid = c;
		color = col;
		try
		{
			//Color curColor = CalicoOptions.getColor("pen.default_color");
			//setImage(CalicoOptions.getColorImage(color));
			
			if(CalicoDataStore.PenColor.equals(color))
			{
				setSelected(true);
			}
			
			setImage(CalicoOptions.getColorImageRect(color, 16));
			setBounds(bounds);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	public void actionMouseClicked()
	{
		//Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_REDO, cuid));
		Calico.logger.debug("Pressed Color button "+color.toString());
		CalicoDataStore.PenColor = color;
		CCanvasController.redrawMenuBars();
	}
	
}