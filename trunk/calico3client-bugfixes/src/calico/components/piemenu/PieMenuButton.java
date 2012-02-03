package calico.components.piemenu;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import edu.umd.cs.piccolo.nodes.*;
import calico.*;
import calico.components.*;
import calico.controllers.CCanvasController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.*;

//addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosing(WindowEvent winEvt) {Calico.exit();}});

public class PieMenuButton
{

	public static int SHOWON = 0;
	public static final int SHOWON_SCRAP_CREATE = 1 << 1;
	public static final int SHOWON_SCRAP_MENU = 1 << 2;
	
	
	protected String iconPath = "";
	protected Image iconImage = null;
	protected Rectangle bounds = new Rectangle();
	
	protected Point buttonPosition = new Point(0,0);
		
	// Set the Icon
	public PieMenuButton(String str)
	{
		iconPath = str;
		try
		{
			iconImage = CalicoIconManager.getIconImage(iconPath);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public PieMenuButton(URL url)
	{
		iconImage = Toolkit.getDefaultToolkit().getImage(url);
	}
	public PieMenuButton(Image img)
	{
		iconImage = img;
	}
	
	
	/**
	 * This will 
	 */
	public void onClick()
	{
		// This should be implemented
	}
	
	/**
	 * If you do not override this, then we just call the onclick with nothing
	 * @param event
	 */
	public void onClick(InputEventInfo event)
	{
		// This should be implemented
		onClick();
		PieMenu.isPerformingPieMenuAction =true;
		
		MouseListener mouseListener = new MouseListener()
		{

			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (CViewportCanvas.PERSPECTIVE.isActive())
					CViewportCanvas.getInstance().removeMouseListener(this);
				else if (CCanvas.PERSPECTIVE.isActive())
					CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).removeMouseListener(this);
				
				e.consume();
//				PieMenu.isPerformingPieMenuAction = false;
//				System.out.println("//////////// Removing pie menu event handler");
			}
			
		};
		if (CViewportCanvas.PERSPECTIVE.isActive())
		{
			CViewportCanvas.getInstance().addMouseListener(mouseListener);
		}
		else if (CCanvas.PERSPECTIVE.isActive())
		{
			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).addMouseListener(mouseListener);
//			System.out.println("//////////// Removing pie menu event handler");
		}
	}
	
	public final void setPosition(Point point)
	{
		buttonPosition = point;
		bounds = new Rectangle(point.x, point.y, CalicoOptions.menu.icon_size, CalicoOptions.menu.icon_size);
	}
	
		
	/**
	 * Returns the bounds that contains this menu icon
	 * @return
	 */
	public final Rectangle getBounds()
	{
		return bounds;
	}
	
	/**
	 * This checks to see if the given point is within this menu icon's bounds
	 * @param point
	 * @return true if within bounds
	 */
	public final boolean checkWithinBounds(Point point)
	{
		return bounds.contains(point);
	}
	
	/**
	 * @see #checkWithinBounds(Point)
	 * @param x
	 * @param y
	 * @return
	 */
	public final boolean checkWithinBounds(int x, int y)
	{
		return checkWithinBounds(new Point(x,y));
	}
	
	/**
	 * Returns the {@link edu.umd.cs.piccolo.nodes.PImage} 
	 * @return 
	 */
	public final PImage getPImage()
	{
		
		try
		{
					
			PImage img = new PImage();
			//"http://s3.amazonaws.com/ucicalico2/icons/32/home.png");
			//img.setImage( Toolkit.getDefaultToolkit().getImage(new URL(iconPath)) );
			
			img.setImage( iconImage );//iconImage );
			img.setBounds(buttonPosition.x,buttonPosition.y,CalicoOptions.menu.icon_size,CalicoOptions.menu.icon_size);
			img.repaint();
			return img;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
}
