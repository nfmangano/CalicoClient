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

public class EmailButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private long cuid = 0L;
	
	
	public EmailButton(long c)
	{
		super();
		cuid = c;
		iconString = "email.canvas";
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
			String response = JOptionPane.showInputDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent(),
					  "Please enter the email address(es) you wish to send this canvas to",
					  "Email Canvas",
					  JOptionPane.QUESTION_MESSAGE);
			if (response != null)
			{
				try
				{
					Calendar cal = new GregorianCalendar();
					
					// Get the components of the time
					int hour12 = cal.get(Calendar.HOUR);            // 0..11
					int min = cal.get(Calendar.MINUTE);             // 0..59
					int ampm = cal.get(Calendar.AM_PM);             // 0=AM, 1=PM
					String time = "" + hour12 + ":" + min + " " + ((ampm==0)?"AM":"PM") + ", " + cal.get(Calendar.MONTH)+1 + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);
					
					// Send a test message
			        send("smtp.gmail.com", 465, CalicoDataStore.Username + " <ucicalicodev@gmail.com>", response,
			        		"Calico Canvas " + CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getGridCoordTxt() + " - " + time, "Screenshot of Calico Canvas\n\n");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			super.onMouseUp();
		}
		
		//StatusMessage.popup("Not yet implemented");
	}
	
    public static void send(String smtpHost, int smtpPort,
            String from, String to,
            String subject, String content)
		throws AddressException, MessagingException {
		// Create a mail session
		java.util.Properties props = new java.util.Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", ""+smtpPort);
		props.put("mail.smtps.auth", "true");
		Session session = Session.getDefaultInstance(props, null);
		
		// Construct the message
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
		msg.setSubject(subject);
//		msg.setText(content);
		
		String attachment = getImageAttachment();
		MimeBodyPart messagePart = new MimeBodyPart();
		messagePart.setText(content);
		             
		
		MimeBodyPart attachmentPart = new MimeBodyPart();
		FileDataSource fileDataSource = new FileDataSource(attachment) {
		   @Override
		   public String getContentType() {
			   return "application/octet-stream";
		   }
		};
		attachmentPart.setDataHandler(new DataHandler(fileDataSource));
		attachmentPart.setFileName(attachment);
		
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messagePart);
		multipart.addBodyPart(attachmentPart);
		
		msg.setContent(multipart);
		
		// Send the message
		Transport t = session.getTransport("smtps");
	    try {
			t.connect(smtpHost, "ucicalicodev@gmail.com", "calico99");
			t.sendMessage(msg, msg.getAllRecipients());
	    } finally {
	    	t.close();
	    	JOptionPane.showMessageDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent(), "Email sent successfully");
	    }

	}
    
    public static String getImageAttachment()
    {
    	BufferedImage bIMG = new BufferedImage(CalicoDataStore.ScreenWidth, CalicoDataStore.ScreenHeight, BufferedImage.TYPE_INT_ARGB);
    	RenderedImage img = (RenderedImage)CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer().toImage(bIMG, Color.white);
    	String fileName = "CalicoCanvas_" + CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getGridCoordTxt() + ".png";
    	
    	saveImage(img, fileName);
    	return fileName;
    }
    
	public static void saveImage(RenderedImage img, String fileName) {
		// Write generated image to a file
	    try {
	        // Save as JPEG
	        File file = new File(fileName);
	        ImageIO.write(img, "png", file);
	        System.out.println("Path: " + file.getAbsolutePath());
	    } 

	    catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
}