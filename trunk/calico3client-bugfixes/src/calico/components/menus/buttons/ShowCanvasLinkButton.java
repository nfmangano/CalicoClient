package calico.components.menus.buttons;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;

import edu.umd.cs.piccolo.event.*;

public class ShowCanvasLinkButton extends CanvasMenuButton implements ClipboardOwner
{
	private static final long serialVersionUID = 1L;
	
	private long cuid = 0L;
	
	
	public ShowCanvasLinkButton(long c)
	{
		super();
		cuid = c;
		iconString = "link.canvas";
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
		
		String link = "http://" + CalicoDataStore.ServerHost + ":" + (CalicoDataStore.ServerPort-1) + "/canvas/getimage?uuid=" + cuid;
		String message = "You may view this canvas from a web browser using the following link:\n" + link; 
		
		Object[] options = {"Copy to clipboard", "Cancel"};
		int n = JOptionPane.showOptionDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent(),
				message,
				"Canvas hyperlink",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,     //do not use a custom Icon
				options,  //the titles of buttons
				options[0]); //default button title
		
//		JOptionPane.showMessageDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent(), link);
		if (n == 0)
		{
			setClipboardContents(link);
		}
		
		super.onMouseUp();

	}
	
	  public void setClipboardContents( String aString ){
		    StringSelection stringSelection = new StringSelection( aString );
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents( stringSelection, this );
		}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// do nothing
		
	}
		
}