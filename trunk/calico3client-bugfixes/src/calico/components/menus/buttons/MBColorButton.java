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
import calico.input.CInputMode;
import calico.inputhandlers.InputEventInfo;
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
		
	public MBColorButton(long c, Color col, String iconPath, Rectangle2D bounds)
	{
		super();
		cuid = c;
		color = col;
		iconString = iconPath;
		try
		{
			//Color curColor = CalicoOptions.getColor("pen.default_color");
			//setImage(CalicoOptions.getColorImage(color));
			
			if(CalicoDataStore.PenColor.equals(color) && (CalicoDataStore.Mode == CInputMode.EXPERT || CalicoDataStore.Mode == CInputMode.ARROW))
			{
				setSelected(true);
			}
			
			setImage(CalicoIconManager.getIconImage(iconPath));
			setBounds(bounds);
			
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
			//Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_REDO, cuid));
			Calico.logger.debug("Pressed Color button "+color.toString());
			CalicoDataStore.PenColor = color;
			CalicoDataStore.LastDrawingColor = color;
			
			if (CalicoDataStore.Mode != CInputMode.ARROW)
				CalicoDataStore.set_Mode(CInputMode.EXPERT);
			CCanvasController.redrawMenuBars();
			isPressed = false;
		}
	}
	
}