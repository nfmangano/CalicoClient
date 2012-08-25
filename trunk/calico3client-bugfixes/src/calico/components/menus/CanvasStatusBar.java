package calico.components.menus;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.CalicoDraw;
import calico.CalicoOptions;
import calico.components.CConnector;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.composable.ComposableElementController;
import calico.components.composable.connectors.ArrowheadElement;
import calico.components.composable.connectors.CardinalityElement;
import calico.components.composable.connectors.ColorElement;
import calico.components.composable.connectors.HighlightElement;
import calico.components.composable.connectors.LabelElement;
import calico.components.composable.connectors.LineStyleElement;
import calico.components.menus.buttons.EmailButton;
import calico.components.menus.buttons.ExitButton;
import calico.components.menus.buttons.HistoryNavigationForwardButton;
import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
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
//		String canvasIndex = String.valueOf(CCanvasController.canvasdb.get(cuid).getIndex());
		
//		addText(canvasIndex, new Font("Verdana", Font.BOLD, 12));
		
		addSpacer();
		
		if (CCanvasController.canvasdb.get(cuid).getLockValue())
		{
			addText("DO NOT ERASE", new Font("Verdana", Font.BOLD, 12));
		}
		
		//Begin align right
		addTextEndAligned(
				"  Exit  ", 
				new Font("Verdana", Font.BOLD, 12),
				new ExitButton(cuid)
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

		if (!Networking.synchroized)
		{
			addSpacer(ALIGN_CENTER);
			addTextCenterAligned(
					//" LOST SYNC!!  ", 
					" RESYNCING... ",
					new Font("Verdana", Font.BOLD, 12),
					new CanvasTextButton(cuid) {
						public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
							/*if (event.getAction() == InputEventInfo.ACTION_PRESSED)
							{
								isPressed = true;
							}
							else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
							{
								int result = JOptionPane.showOptionDialog(null, "The canvas is not synchronized with the server. Press OK to synchronize", "Out of Sync Alert!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
								if (result == 0)
								{
									Networking.send(NetworkCommand.CONSISTENCY_RESYNC_CANVAS, CCanvasController.getCurrentUUID());
								}
								
								isPressed = false;
							}*/
						}
					}
			);
		}
		
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
		
//		addTextEndAligned(
//				"Arrow", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
//						if (event.getAction() == InputEventInfo.ACTION_PRESSED)
//						{
//							isPressed = true;
//						}
//						else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
//						{
//							isPressed = false;
//							ComposableElementController.addElement(new ArrowheadElement(Calico.uuid(), BubbleMenu.lastUUID, CConnector.TYPE_HEAD, CalicoOptions.arrow.stroke_size, Color.black, Color.black, ArrowheadElement.getDefaultCircle()));
//							ComposableElementController.addElement(new ArrowheadElement(Calico.uuid(), BubbleMenu.lastUUID, CConnector.TYPE_TAIL, CalicoOptions.arrow.stroke_size, Color.black, Color.red, ArrowheadElement.getDefaultArrow()));
//							
//						}
//						
//						//CConnectorController.connectors.get(BubbleMenu.lastUUID).redraw();
//					}
//				}
//		);
//		addSpacer(ALIGN_END);
//		addTextEndAligned(
//				"Card", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
//						if (event.getAction() == InputEventInfo.ACTION_PRESSED)
//						{
//							isPressed = true;
//						}
//						else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
//						{
//							isPressed = false;
//							ComposableElementController.addElement(new CardinalityElement(Calico.uuid(), BubbleMenu.lastUUID, CConnector.TYPE_HEAD, "20", CalicoOptions.group.font));
//							ComposableElementController.addElement(new CardinalityElement(Calico.uuid(), BubbleMenu.lastUUID, CConnector.TYPE_TAIL, "30", CalicoOptions.group.font));
//							
//						}
//						//CConnectorController.connectors.get(BubbleMenu.lastUUID).redraw();
//					}
//				}
//		);
//		addSpacer(ALIGN_END);
//		addTextEndAligned(
//				"Color", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
//						if (event.getAction() == InputEventInfo.ACTION_PRESSED)
//						{
//							isPressed = true;
//						}
//						else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
//						{
//							isPressed = false;
//							ComposableElementController.addElement(new ColorElement(Calico.uuid(), BubbleMenu.lastUUID, Color.red, CConnectorController.connectors.get(BubbleMenu.lastUUID).getColor()));
//							
//						}
//						//CConnectorController.connectors.get(BubbleMenu.lastUUID).redraw();
//					}
//				}
//		);
//		addSpacer(ALIGN_END);
//		addTextEndAligned(
//				"Highlight", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
//						if (event.getAction() == InputEventInfo.ACTION_PRESSED)
//						{
//							isPressed = true;
//						}
//						else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
//						{
//							isPressed = false;
//							ComposableElementController.addElement(new HighlightElement(Calico.uuid(), BubbleMenu.lastUUID, CalicoOptions.stroke.background_transparency, new BasicStroke(CalicoOptions.pen.stroke_size + 8), Color.green));
//							
//						}
//						//CConnectorController.connectors.get(BubbleMenu.lastUUID).redraw();
//					}
//				}
//		);
//		addSpacer(ALIGN_END);
//		addTextEndAligned(
//				"Label", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
//						if (event.getAction() == InputEventInfo.ACTION_PRESSED)
//						{
//							isPressed = true;
//						}
//						else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
//						{
//							isPressed = false;
//							ComposableElementController.addElement(new LabelElement(Calico.uuid(), BubbleMenu.lastUUID, "Test Annotation", CalicoOptions.group.font));
//						}
//						
//						//CConnectorController.connectors.get(BubbleMenu.lastUUID).redraw();
//					}
//				}
//		);
//		addSpacer(ALIGN_END);
//		addTextEndAligned(
//				"Line", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
//						if (event.getAction() == InputEventInfo.ACTION_PRESSED)
//						{
//							isPressed = true;
//						}
//						else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
//						{
//							isPressed = false;
//							ComposableElementController.addElement(new LineStyleElement(Calico.uuid(), BubbleMenu.lastUUID, 
//									new BasicStroke(CalicoOptions.group.stroke_size,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f}, 0.0f), 
//									CConnectorController.connectors.get(BubbleMenu.lastUUID).getStroke()));
//						}
//						
//						//CConnectorController.connectors.get(BubbleMenu.lastUUID).redraw();
//					}
//				}
//		);
//		addSpacer(ALIGN_END);
//		addTextEndAligned(
//				"Remove All", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox) {
//						if (event.getAction() == InputEventInfo.ACTION_PRESSED)
//						{
//							isPressed = true;
//						}
//						else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
//						{
//							isPressed = false;
//							CConnectorController.connectors.get(BubbleMenu.lastUUID).removeAllElements();
//						}
//						
//						//CConnectorController.connectors.get(BubbleMenu.lastUUID).redraw();
//
//					}
//				}
//		);
		
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
