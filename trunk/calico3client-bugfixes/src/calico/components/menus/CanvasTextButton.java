package calico.components.menus;

import java.awt.*;
import java.awt.image.*;
import java.net.MalformedURLException;
import java.net.URL;

import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;

import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PComposite;


public class CanvasTextButton
{	
	private static final long serialVersionUID = 1L;
	private long cuid = 0L;
	protected boolean isPressed = false;
	
	public CanvasTextButton(long cuid)
	{
		this.cuid = cuid;
	}
	
	public void actionMouseClicked(Rectangle boundingBox)
	{
		
	}
	
	public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox)
	{
		
	}
	
}
