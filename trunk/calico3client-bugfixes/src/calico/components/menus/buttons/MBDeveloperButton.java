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
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import edu.umd.cs.piccolo.event.*;


public class MBDeveloperButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
		
	private long cuid = 0L;
	
	private String command = "";
		
	public MBDeveloperButton(long c, String command)
	{
		super();
		cuid = c;
		this.command = command;
		iconString = "mode.expert";
		try
		{
			setPaint(Color.BLACK);
			setImage(CalicoIconManager.getIconImage(iconString));
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
			Calico.logger.debug("RUN COMMAND: "+this.command);
			
			if(this.command.equals("canvas_clear"))
			{
				CCanvasController.clear(this.cuid);
			}
			isPressed = false;
		}
		
	}
	
}