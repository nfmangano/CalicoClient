package calico.inputhandlers;

import edu.umd.cs.piccolo.event.*;

/**
 * This is a default input handler. It ignores all mouse events
 * This should be extended, to allow you to only extend the methods
 * that you actually need.
 * 
 * @author Mitch Dempsey
 * @deprecated
 * @see CalicoAbstractInputHandler
 */
public class IgnorantInputHandler extends PBasicInputEventHandler
{
	public void mouseDragged(InputEventInfo e){}
	public void mouseClicked(InputEventInfo e){}
	public void mousePressed(InputEventInfo e){}
	public void mouseReleased(InputEventInfo e){}
	//public void mouseDragged(InputEventInfo e){}
	//public void mouseDragged(InputEventInfo e){}
	
	public void mousePressed(PInputEvent e)
	{
		e.setHandled(true);
		super.mousePressed(e);
	}

	public void mouseClicked(PInputEvent e)
	{
		e.setHandled(true);
		super.mouseClicked(e);
	}
	public void mouseEntered(PInputEvent e)
	{
		e.setHandled(true);
		super.mouseEntered(e);
	}
	public void mouseDragged(PInputEvent e)
	{
		e.setHandled(true);
		super.mouseDragged(e);
	}
	public void mouseExited(PInputEvent e)
	{
		e.setHandled(true);
		super.mouseExited(e);
	}

	public void mouseReleased(PInputEvent e)
	{
		e.setHandled(true);
		super.mouseReleased(e);
	}

	public void mouseMoved(PInputEvent e)
	{
		e.setHandled(true);
		super.mouseMoved(e);
	}
	public void keyTyped(PInputEvent e)
	{
		e.setHandled(true);
		super.keyTyped(e);
	}
	
	
	
	
	//////
	public void mousePressed(PInputEvent e,boolean handled)
	{
		e.setHandled(handled);
		super.mousePressed(e);
	}

	public void mouseClicked(PInputEvent e,boolean handled)
	{
		e.setHandled(handled);
		super.mouseClicked(e);
	}
	public void mouseEntered(PInputEvent e,boolean handled)
	{
		e.setHandled(handled);
		super.mouseEntered(e);
	}
	public void mouseDragged(PInputEvent e,boolean handled)
	{
		e.setHandled(handled);
		super.mouseDragged(e);
	}
	public void mouseExited(PInputEvent e,boolean handled)
	{
		e.setHandled(handled);
		super.mouseExited(e);
	}

	public void mouseReleased(PInputEvent e,boolean handled)
	{
		e.setHandled(handled);
		super.mouseReleased(e);
	}

	public void mouseMoved(PInputEvent e,boolean handled)
	{
		e.setHandled(handled);
		super.mouseMoved(e);
	}
	
	public void keyTyped(PInputEvent e,boolean handled)
	{
		e.setHandled(handled);
		super.keyTyped(e);
	}


}