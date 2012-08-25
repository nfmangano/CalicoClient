package calico.components.menus;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import calico.*;
import calico.components.*;
import calico.components.grid.*;
import calico.components.menus.buttons.MBColorButton;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.*;
import calico.modules.*;
import calico.networking.*;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.*;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import edu.umd.cs.piccolo.event.*;



public class CanvasTopMenuBar extends CanvasGenericMenuBar
{
	private static final long serialVersionUID = 1L;
	
	private Rectangle rect_default = new Rectangle(0,0,16,16);
	
	private long cuid = 0L;
	
	
	
	
	public CanvasTopMenuBar(long c)
	{
		super(CanvasGenericMenuBar.POSITION_TOP, CCanvasController.canvasdb.get(c).getBounds());
		cuid = c;
		
		
		// GRID COORDINATES
//		String canvasIndex = String.valueOf(CCanvasController.canvasdb.get(cuid).getIndex());
		
		
		addCap(CanvasGenericMenuBar.ALIGN_START);
		
//		addText(canvasIndex, new Font("Verdana", Font.BOLD, 12));
		
		addSpacer();
		
//		addIcon(new MBColorButton(0L, CalicoDataStore.PenColor, rect_default));

		addSpacer();
		
				
		switch(CalicoDataStore.Mode)
		{
//			case ARROW:canvasIndex="Arrow Mode";break;
//			case DELETE:canvasIndex="Eraser Mode";break;
//			case EXPERT:canvasIndex="Expert Mode";break;
//			case SCRAP:canvasIndex="Scrap Mode";break;
//			case STROKE:canvasIndex="Stroke Mode";break;
		}
		
//		addText(canvasIndex, new Font("Verdana", Font.BOLD, 12));
		
		addCap(CanvasGenericMenuBar.ALIGN_END);
		

		//this.invalidatePaint();
		CalicoDraw.invalidatePaint(this);

	}
			
}
