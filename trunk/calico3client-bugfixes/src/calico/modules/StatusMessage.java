package calico.modules;

import calico.*;
import calico.components.*;

import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PComposite;
import edu.umd.cs.piccolo.*;

import java.awt.Font;
import java.awt.Color;

import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;
import java.awt.*;

/**
 * This class handles all processing of error messages
 * 
 * @author Mitch Dempsey
 */
public class StatusMessage// extends Thread
{
	public static final int STATUS = 1;
	public static final int ERROR = 2;
	
	
	public StatusMessage()
	{
	}
	
	public void run()
	{
	
	}
	
	/**
	 * This adds a message to the queue, it will displayed ASAP
	 * 
	 * @param msg the text of the message to be shown.
	 */
	public static void msg(String msg)
	{
		Calico.logger.debug("STATUS CURRENT CONTENT PANE: "+CalicoDataStore.calicoObj.getContentPane().getComponent(0).getClass().getName());
		Calico.logger.info(msg);
	}
	
	public static void popup(String msg)
	{
		Calico.logger.info(msg);
		JFrame errorFrame = new JFrame();
		JOptionPane.showMessageDialog(errorFrame,msg,"Client Message",JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * @deprecated
	 * @see MessageObject#showNotice(String)
	 * @param msg
	 */
	public static void notice(String msg)
	{
		MessageObject.showNotice(msg);
	}
	
}