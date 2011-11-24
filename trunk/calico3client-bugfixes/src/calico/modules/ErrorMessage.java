package calico.modules;

import calico.*;
import calico.components.*;

import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.*;

import java.awt.Font;
import java.awt.Color;

import java.util.*;
import java.util.concurrent.*;


import javax.swing.*;


/**
 * This class handles all processing of error messages
 * 
 * @author Mitch Dempsey
 */
public class ErrorMessage// extends Thread
{
	public ErrorMessage()
	{
		
	}
	
	public void run()
	{
		
	}
	
	/**
	 * This adds a message to the queue, it will displayed ASAP
	 * 
	 * @param msg the text of the message to be shown.
	 * @deprecated
	 * @see MessageObject#showError(String)
	 */
	public static void msg(String msg)
	{	
		Calico.logger.error(msg);
	}
	
	public static void popup(String msg)
	{
		Calico.logger.error(msg);
		JFrame errorFrame = new JFrame();
		JOptionPane.showMessageDialog(errorFrame,msg,"Client Message",JOptionPane.ERROR_MESSAGE);
	}
	
	public static void fatal(String msg)
	{
		Calico.logger.fatal(msg);
		popup(msg);
		System.exit(1);
	}
}