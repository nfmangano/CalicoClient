package calico.components.menus.buttons;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import calico.*;
import calico.components.*;
import calico.components.grid.*;
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



public class MenuStaticIconButton extends PImage
{
	private static final long serialVersionUID = 1L;

	public MenuStaticIconButton(String iconStr)
	{
		this(iconStr, new Rectangle(0,0,16,16));
	}
	
	public MenuStaticIconButton(String iconStr, Rectangle2D bounds)
	{
		super();
		try
		{
			setImage( CalicoIconManager.getIconImage(iconStr));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	public void actionMouseClicked()
	{
		// Does nothing
	}
	
}