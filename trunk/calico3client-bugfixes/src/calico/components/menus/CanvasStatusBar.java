package calico.components.menus;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.CalicoDraw;
import calico.components.menus.buttons.EmailButton;
import calico.controllers.CCanvasController;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.NetworkCommand;

public class CanvasStatusBar extends CanvasGenericMenuBar
{
	private static final long serialVersionUID = 1L;

	private long cuid = 0L;
	
	private static ObjectArrayList<Class<?>> externalButtons = new ObjectArrayList<Class<?>>();
	private static ObjectArrayList<Class<?>> externalButtons_rightAligned = new ObjectArrayList<Class<?>>();
	
	public CanvasStatusBar(long c)
	{
		super(CanvasGenericMenuBar.POSITION_BOTTOM, CCanvasController.canvasdb.get(c).getBounds());		
		
		cuid = c;
		
		String coordtxt = CCanvasController.canvasdb.get(cuid).getGridCoordTxt();
		
		addText(coordtxt, new Font("Verdana", Font.BOLD, 12));
		
		addSpacer();
		
		if (CCanvasController.canvasdb.get(cuid).getLockValue())
		{
			addText("DO NOT ERASE", new Font("Verdana", Font.BOLD, 12));
		}
		
		//Begin align right
		addTextEndAligned(
				"  Exit  ", 
				new Font("Verdana", Font.BOLD, 12),
				new CanvasTextButton(cuid) {
					public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
						if (event.getAction() == InputEventInfo.ACTION_PRESSED)
						{
							isPressed = true;
						}
						else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
						{
							isPressed = false;
							Calico.exit();
						}
					}
				}
		);
		
		addSpacer(ALIGN_END);
		addIconRightAligned(new EmailButton(cuid));
		
		if (Networking.connectionState == Networking.ConnectionState.Connecting)
		{
			addSpacer(ALIGN_END);
			addTextEndAligned(
					" reconnect...   ", 
					new Font("Verdana", Font.BOLD, 12),
					new CanvasTextButton(cuid) {
						public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
							//Do nothing
						}
					}
			);
			
			addTextEndAligned(
					" Attempting to", 
					new Font("Verdana", Font.BOLD, 12),
					new CanvasTextButton(cuid) {
						public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
							//Do nothing
						}
					}
			);
			
			addTextEndAligned(
					"   Disconnected!", 
					new Font("Verdana", Font.BOLD, 12),
					new CanvasTextButton(cuid) {
						public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
							//Do nothing
						}
					}
			);

		}

// 		BY MOTTA.LRD: FOR THE MOMENT I DO NOT WANT TO BE SYNCHRONIZED
//		if (!Networking.synchroized)
//		{
//			addSpacer(ALIGN_CENTER);
//			addTextCenterAligned(
//					" LOST SYNC!!  ", 
//					new Font("Verdana", Font.BOLD, 12),
//					new CanvasTextButton(cuid) {
//						public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
//							if (event.getAction() == InputEventInfo.ACTION_PRESSED)
//							{
//								isPressed = true;
//							}
//							else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
//							{
//								int result = JOptionPane.showOptionDialog(null, "The canvas is not synchronized with the server. Press OK to synchronize", "Out of Sync Alert!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
//								if (result == 0)
//								{
//									Networking.send(NetworkCommand.CONSISTENCY_RESYNC_CANVAS, CCanvasController.getCurrentUUID());
//								}
//								
//								isPressed = false;
//							}
//						}
//					}
//			);
//		}
		
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
		
		//this.invalidatePaint();
		CalicoDraw.invalidatePaint(this);
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
