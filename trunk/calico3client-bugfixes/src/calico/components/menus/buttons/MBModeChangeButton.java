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


public class MBModeChangeButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
		
	private long cuid = 0L;
	
	private int type = 0;
		
	public MBModeChangeButton(long c, int t)
	{
		super();
		cuid = c;
		type = t;
		try
		{
			//setPaint(Color.BLACK);//CalicoOptions.getColor("canvas.menubar.button.background_color"));
			
			if(CalicoDataStore.Mode==type)
			{
				setSelected(true);
				//setTransparency(CalicoOptions.menu.menubar.transparency_disabled);
			}
			
			switch(type)
			{
				case Calico.MODE_ARROW:
					setImage(CalicoIconManager.getIconImage("mode.arrow"));
					break;
					
				case Calico.MODE_DELETE:
					setImage(CalicoIconManager.getIconImage("mode.delete"));
					
					break;
					
				case Calico.MODE_EXPERT:
					setImage(CalicoIconManager.getIconImage("mode.expert"));
					break;
					
				case Calico.MODE_SCRAP:
					setImage(CalicoIconManager.getIconImage("mode.scrap"));
					break;
					
				case Calico.MODE_STROKE:
					setImage(CalicoIconManager.getIconImage("mode.stroke"));
					break;
					
				case Calico.MODE_POINTER:
					setImage(CalicoIconManager.getIconImage("mode.pointer"));
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
		Calico.logger.debug("Changing Mode to "+type);
		
		if(type==Calico.MODE_DELETE)
		{
			//CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getBlobs();
			//return;
		}
		
		if(CalicoDataStore.Mode==type)
		{
			switch(type)
			{
				case Calico.MODE_ARROW:MessageObject.showError("Already in Arrow Mode");break;
				case Calico.MODE_DELETE:MessageObject.showError("Already in Eraser Mode");break;
				case Calico.MODE_EXPERT:MessageObject.showError("Already in Expert Mode");break;
				case Calico.MODE_SCRAP:MessageObject.showError("Already in Scrap Mode");break;
				case Calico.MODE_STROKE:MessageObject.showError("Already in Stroke Mode");break;
				case Calico.MODE_POINTER:MessageObject.showError("Already in Pointer Mode");break;
			}
		}
		else
		{
		
			//CalicoDataStore.Mode = type;
			CalicoDataStore.set_Mode(type);
			CCanvasController.redrawMenuBars();
			
			
			switch(type)
			{
				case Calico.MODE_ARROW:MessageObject.showNotice("Switching to Arrow Mode");break;
				case Calico.MODE_DELETE:MessageObject.showNotice("Switching to Eraser Mode");break;
				case Calico.MODE_EXPERT:MessageObject.showNotice("Switching to Expert Mode");/*StatusMessage.popup("You are entering expert mode. Be advised.");*/break;
				case Calico.MODE_SCRAP:MessageObject.showNotice("Switching to Scrap Mode");break;
				case Calico.MODE_STROKE:MessageObject.showNotice("Switching to Stroke Mode");break;
				case Calico.MODE_POINTER:MessageObject.showNotice("Switching to Pointer Mode");break;
			}
			
		}
		
	}
	

	
}