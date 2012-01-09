package calico.components.menus;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import calico.*;
import calico.components.*;
import calico.components.grid.*;
import calico.components.menus.buttons.*;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.*;
import calico.modules.*;
import calico.networking.*;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.*;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import javax.swing.JOptionPane;

import edu.umd.cs.piccolo.event.*;



public class CanvasMenuBar extends CanvasGenericMenuBar
{
	private static final long serialVersionUID = 1L;
	
	private long cuid = 0L;
	
	private PImage lockButton;
	int setLock_button_array_index;
	Rectangle setLock_bounds;
	
	private PImage clients;
	
	private static ObjectArrayList<Class<?>> externalButtons = new ObjectArrayList<Class<?>>();
	private static ObjectArrayList<Class<?>> externalButtons_rightAligned = new ObjectArrayList<Class<?>>();
	
	public CanvasMenuBar(long c)
	{		
		super(CanvasGenericMenuBar.POSITION_LEFT, CCanvasController.canvasdb.get(c).getBounds());		
		
		cuid = c;
		
		Rectangle rect_default = new Rectangle(0,0,20,20);
		
		addCap(CanvasGenericMenuBar.ALIGN_START);
		
		addIcon(new ReturnToGrid());
		
		
		String coordtxt = CCanvasController.canvasdb.get(cuid).getGridCoordTxt();
		
		addSpacer();
		
		addText(coordtxt, new Font("Verdana", Font.BOLD, 12));
		
		addSpacer();
		
//		clients = addText(
//				CCanvasController.canvasdb.get(cuid).getClients().length+" clients", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(Rectangle boundingBox) {
//						CCanvasController.canvasdb.get(cuid).drawClientList(boundingBox);
//					}
//				}
//				);
		
//		addSpacer();
		
		
		// viewport buttons
//		addIcon(new ViewportChangeButton(cuid, ViewportChangeButton.BUT_ZOOMIN));
//		addIcon(new ViewportChangeButton(cuid, ViewportChangeButton.BUT_ZOOMOUT));
//		addIcon(new ViewportChangeButton(cuid, ViewportChangeButton.BUT_ZOOMTOCANVAS));
		
		
//		addSpacer();
		
		addIcon(new CanvasNavButton(cuid,CanvasNavButton.TYPE_UP));
		addIcon(new CanvasNavButton(cuid,CanvasNavButton.TYPE_DOWN));
		addIcon(new CanvasNavButton(cuid,CanvasNavButton.TYPE_LEFT));
		addIcon(new CanvasNavButton(cuid,CanvasNavButton.TYPE_RIGHT));
		

		//addCap();
		addSpacer();
				
		addIcon(new ClearButton(cuid));
		addSpacer();
		
		setLock_button_array_index = text_button_array_index++;
		setLock();
		addSpacer();
		
		addIcon(new UndoButton(cuid));
		addIcon(new RedoButton(cuid));
		addSpacer();
		
		for(int i=0;i<CalicoOptions.menu.colorlist.length;i++)
		{
			addIcon(new MBColorButton(cuid, CalicoOptions.menu.colorlist[i], rect_default));
		}
		addSpacer();
		
		// Mode buttons
		addIcon(new MBModeChangeButton(cuid, Calico.MODE_DELETE));
		addIcon(new MBModeChangeButton(cuid, Calico.MODE_ARROW));

		addIcon(new MBModeChangeButton(cuid, Calico.MODE_EXPERT));
		addIcon(new MBModeChangeButton(cuid, Calico.MODE_POINTER));
		
		//Begin align right
		addSpacer(ALIGN_END);
		addIconRightAligned(new EmailButton(cuid));
		
	//	addSpacer();addSpacer();
		//addIcon(new MBDeveloperButton(cuid, "canvas_clear"));
		
		try
		{
			for (Class<?> button : externalButtons)
			{
				addSpacer();
				addIcon((CanvasMenuButton) button.getConstructor(long.class).newInstance(cuid));
			}
			
			for (Class<?> button : externalButtons_rightAligned)
			{
				addSpacer(ALIGN_END);
				addIconRightAligned((CanvasMenuButton) button.getConstructor(long.class).newInstance(cuid));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		this.invalidatePaint();
	}
	
	private void setLock()
	{
		String text = (CCanvasController.canvasdb.get(cuid).getLockValue())?"DO NOT ERASE":"   CAN ERASE   ";
		
		
		
		setLock(text, new Font("Verdana", Font.BOLD, 12),
				new CanvasTextButton(cuid) {
			public void actionMouseClicked(Rectangle boundingBox) {
				boolean lockValue = CCanvasController.canvasdb.get(cuid).getLockValue();
				long time = (new Date()).getTime();
				CCanvasController.lock_canvas(cuid, !lockValue, CalicoDataStore.Username, time);
//				setLock();
				CCanvasController.canvasdb.get(cuid).drawMenuBars();
			}
		});
		
	}
	
	protected void setLock(String text, Font font, CanvasTextButton buttonHandler)
	{
		/*PText gct = new PText(text);
		gct.setConstrainWidthToTextWidth(true);
		gct.setConstrainHeightToTextHeight(true);
		gct.setFont(font);//new Font("Monospaced", Font.BOLD, 20));
		Rectangle rect_coordtxt = addIcon(gct.getBounds().getBounds());
		gct.setBounds(rect_coordtxt);
		addChild(0,gct);
		*/
		
		if (lockButton != null)
		{
			removeChild(lockButton);
		}
		
		Image img = getTextImage(text,font);

		int imgSpan = 0;
		switch (this.position)
		{
			case POSITION_TOP:
			case POSITION_BOTTOM:
				imgSpan = img.getWidth(null);
				break;
			case POSITION_LEFT:
			case POSITION_RIGHT:
				imgSpan = img.getHeight(null);
				break;
		}		
		
		if (setLock_bounds == null)
			setLock_bounds = addIcon(imgSpan);
		
		switch (this.position)
		{
			case POSITION_TOP:
			case POSITION_BOTTOM:
				setLock_bounds.setSize(setLock_bounds.width, setLock_bounds.height);
				break;
			case POSITION_LEFT:
			case POSITION_RIGHT:
				setLock_bounds.setSize(setLock_bounds.width, setLock_bounds.height);
				break;
		}		
		
		lockButton = new PImage();
		
		lockButton.setImage(img);
		
		lockButton.setBounds(setLock_bounds);
		
		super.text_rect_array[setLock_button_array_index] = setLock_bounds;

		text_button_array[setLock_button_array_index] = buttonHandler;
		
		addChild(0,lockButton);
	}
	
	private void changeLock()
	{
		
	}
	
	
	
	
	public void redrawColorIcon()
	{
		
	}
	
	
	public void redrawArrowIndicator()
	{

	}

	public void redrawClients() {
		
		Rectangle bounds = clients.getBounds().getBounds();
		
		
		
		PImage newClients = new PImage();
		newClients.setImage(getTextImage(CCanvasController.canvasdb.get(cuid).getClients().length+" clients", 
				new Font("Verdana", Font.BOLD, 12)));
		newClients.setBounds(bounds);
		addChild(0, newClients);
		removeChild(clients);
		
		clients = newClients;
		
	}
	
	public static void addMenuButton(Class<?> button)
	{
		externalButtons.add(button);
	}
	
	public static void addMenuButtonRightAligned(Class<?> button)
	{
		externalButtons_rightAligned.add(button);
	}
	
	public static void removeMenuButton(Class<?> button)
	{
		externalButtons.remove(button);
	}
	
		
}
