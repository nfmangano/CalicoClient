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



public class MBSizeButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private long cuid = 0L;
	private float thickness = 1.0f;
		
	public MBSizeButton(long c, float thickness, String iconPath, Rectangle2D bounds)
	{
		super();
		cuid = c;
		this.thickness = thickness;
		iconString = iconPath;
		try
		{
			//Color curColor = CalicoOptions.getColor("pen.default_color");
			//setImage(CalicoOptions.getColorImage(color));
			
			if(CalicoDataStore.PenThickness == thickness && CalicoDataStore.Mode == CInputMode.EXPERT)
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
			Calico.logger.debug("Pressed Size button "+ thickness);
			CalicoDataStore.PenThickness = thickness;
			CalicoDataStore.LastDrawingThickness = thickness;
			
			CalicoDataStore.set_Mode(CInputMode.EXPERT);
			CCanvasController.redrawMenuBars();
			isPressed = false;
		}
	}
	
}