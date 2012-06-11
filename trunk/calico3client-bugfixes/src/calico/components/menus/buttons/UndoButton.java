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
import calico.events.CalicoEventHandler;
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

import javax.swing.ProgressMonitor;

import edu.umd.cs.piccolo.event.*;



public class UndoButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private long cuid = 0L;
	public static final String undoMessage = "Loading previous state from server, please wait... ";
	public static ProgressMonitor progressMonitor;
	
	public UndoButton(long c)
	{
		super();
		cuid = c;
		iconString = "undo";
		try
		{
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
			CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE_START, CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE_START, 0d, 1d, undoMessage));
			CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE, CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE, 0.1d, 1d, undoMessage));
			/*progressMonitor = new ProgressMonitor(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent(),
					undoMessage,
	                "something", 0, 100);
			progressMonitor.setProgress(0);
			progressMonitor.setMillisToPopup(1);
			progressMonitor.setMillisToDecideToPopup(1);*/
			Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_UNDO, cuid));
			Calico.logger.debug("SENDING UNDO COMMAND");
			//StatusMessage.popup("Not yet implemented");
			super.onMouseUp();
		}
	}
	
}