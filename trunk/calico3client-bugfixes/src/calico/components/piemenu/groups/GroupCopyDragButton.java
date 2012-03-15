package calico.components.piemenu.groups;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import calico.Calico;
import calico.components.CCanvas;
import calico.components.CGroup;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.bubblemenu.BubbleMenuButton;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import edu.umd.cs.piccolo.nodes.PImage;

public class GroupCopyDragButton extends PieMenuButton
{
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_CREATE | PieMenuButton.SHOWON_SCRAP_MENU;
	private long new_guuid = 0L;
	private boolean isActive = false;
	Point prevPoint, mouseDownPoint;
	long cuuid, oguuid; 
	
	public GroupCopyDragButton(long uuid)
	{
		super("group.copy");
		draggable = true;
		guuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
		if (!CGroupController.exists(guuid) || isActive)
		{
			return;
		}
		
		isActive = true;

		long old_guuid = guuid;
		//This updates the current guuid
		BubbleMenu.updateGroupUUID(CGroupController.copy_to_canvas(old_guuid));

		CGroupController.groupdb.get(old_guuid).highlight_off();
		CGroupController.groupdb.get(old_guuid).highlight_repaint();
		if (!CGroupController.groupdb.get(old_guuid).isPermanent())
		{
			CGroupController.drop(old_guuid);
			CGroupController.setCurrentUUID(guuid);
			CGroupController.setLastCreatedGroupUUID(guuid);
		}
		
		
		
			
		CGroupController.groupdb.get(guuid).highlight_on();
		CGroupController.groupdb.get(guuid).highlight_repaint();
		
		
		long canvasUUID = CGroupController.groupdb.get(guuid).getCanvasUID();
		
		/*TranslateMouseListener resizeDragListener = new TranslateMouseListener(canvasUUID, guuid, new_guuid);
		CCanvasController.canvasdb.get(canvasUUID).addMouseListener(resizeDragListener);
		CCanvasController.canvasdb.get(canvasUUID).addMouseMotionListener(resizeDragListener);
		
		//pass click event on to this listener since it will miss it
		resizeDragListener.mousePressed(ev.getPoint());*/
		
		prevPoint = new Point(0, 0);
		mouseDownPoint = null;
		cuuid = canvasUUID;

		
		ev.stop();
		BubbleMenu.isPerformingBubbleMenuAction = true;
		
		System.out.println("CLICKED GROUP COPY BUTTON");
	}
	
	public void onDragged(InputEventInfo ev)
	{
		if (mouseDownPoint == null)
		{
			prevPoint.x = ev.getPoint().x;
			prevPoint.y = ev.getPoint().y;
			mouseDownPoint = ev.getPoint();
			CGroupController.move_start(guuid);
		}
		
		CGroupController.move(guuid, (int)(ev.getPoint().x - prevPoint.x), ev.getPoint().y - prevPoint.y);
		
		long smallestParent = CGroupController.groupdb.get(guuid).calculateParent(ev.getPoint().x, ev.getPoint().y);
		if (smallestParent != BubbleMenu.highlightedParentGroup)
		{
			if (BubbleMenu.highlightedParentGroup != 0l)
			{
				CGroupController.groupdb.get(BubbleMenu.highlightedParentGroup).highlight_off();
				CGroupController.groupdb.get(BubbleMenu.highlightedParentGroup).highlight_repaint();
			}
			if (smallestParent != 0l)
			{
				CGroupController.groupdb.get(smallestParent).highlight_on();
				CGroupController.groupdb.get(smallestParent).highlight_repaint();
			}
			BubbleMenu.highlightedParentGroup = smallestParent;
		}
		
		/*if ((smallestParent = CGroupController.groupdb.get(guuid).calculateParent(e.getPoint().x, e.getPoint().y)) != 0l)
		{
			CGroupController.groupdb.get(smallestParent).highlight_on();
		}*/
		
		prevPoint.x = ev.getPoint().x;
		prevPoint.y = ev.getPoint().y;
		ev.stop();
	}
	
	public void onReleased(InputEventInfo ev)
	{
		if (BubbleMenu.highlightedParentGroup != 0l)
		{
			CGroupController.groupdb.get(BubbleMenu.highlightedParentGroup).highlight_off();
			CGroupController.groupdb.get(BubbleMenu.highlightedParentGroup).highlight_repaint();
			BubbleMenu.highlightedParentGroup = 0l;
		}
		
		//This threw a null pointer exception for some reason...
		if (mouseDownPoint != null)
			CGroupController.move_end(this.guuid, ev.getX(), ev.getY()); 
		
		//Update the menu location in case it was dropped into a list
		//BubbleMenu.moveIconPositions(CGroupController.groupdb.get(guuid).getBounds());
		
		ev.stop();
		isActive = false;
	}
		
	private class TranslateMouseListener implements MouseMotionListener, MouseListener
	{
		Point prevPoint, mouseDownPoint;
		long cuuid, guuid, oguuid; 
		
		public TranslateMouseListener(long canvasUUID, long originalUUID, long groupUUID)  {
			prevPoint = new Point();
			cuuid = canvasUUID;
			oguuid = originalUUID;
			guuid = groupUUID;
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			try
			{
				if (mouseDownPoint == null)
				{
					prevPoint.x = e.getPoint().x;
					prevPoint.y = e.getPoint().y;
					mouseDownPoint = e.getPoint();
					CGroupController.move_start(guuid);
				}
				
				if (BubbleMenu.activeGroup != 0l)
				{
					CGroupController.groupdb.get(BubbleMenu.activeGroup).highlight_off();
					CGroupController.groupdb.get(BubbleMenu.activeGroup).highlight_repaint();
				}
				CGroupController.move(guuid, (int)(e.getPoint().x - prevPoint.x), e.getPoint().y - prevPoint.y);
				
				//BubbleMenu.moveIconPositions(CGroupController.groupdb.get(guuid).getBounds());
				
				long smallestParent = CGroupController.groupdb.get(guuid).calculateParent(e.getPoint().x, e.getPoint().y);
				if (smallestParent != BubbleMenu.highlightedParentGroup)
				{
					if (BubbleMenu.highlightedParentGroup != 0l)
					{
						CGroupController.groupdb.get(BubbleMenu.highlightedParentGroup).highlight_off();
						CGroupController.groupdb.get(BubbleMenu.highlightedParentGroup).highlight_repaint();
					}
					if (smallestParent != 0l)
					{
						CGroupController.groupdb.get(smallestParent).highlight_on();
						CGroupController.groupdb.get(smallestParent).highlight_repaint();
					}
					BubbleMenu.highlightedParentGroup = smallestParent;
				}
				
				/*if ((smallestParent = CGroupController.groupdb.get(guuid).calculateParent(e.getPoint().x, e.getPoint().y)) != 0l)
				{
					CGroupController.groupdb.get(smallestParent).highlight_on();
				}*/
				CGroupController.groupdb.get(guuid).highlight_on();
				CGroupController.groupdb.get(guuid).highlight_repaint();
				prevPoint.x = e.getPoint().x;
				prevPoint.y = e.getPoint().y;
				e.consume();
			}
			catch(NullPointerException ne)
			{
				System.out.println("Group disappeared while in use: removing Copy/Drag listeners");
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
			}
		}
		

		@Override
		public void mouseMoved(MouseEvent e) { e.consume(); }		
		@Override
		public void mouseClicked(MouseEvent e) { e.consume(); }	
		@Override
		public void mouseEntered(MouseEvent e) { e.consume(); }	
		@Override
		public void mouseExited(MouseEvent e) { e.consume(); }
		@Override
		public void mousePressed(MouseEvent e) { e.consume(); }
		
		public void mousePressed(Point p) {
			try
			{
				prevPoint.x = 0;
				prevPoint.y = 0;
				mouseDownPoint = null;
			}
			catch(NullPointerException ne)
			{
				System.out.println("Group disappeared while in use: removing Copy/Drag listeners");
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			try
			{
				
				if(!CGroupController.groupdb.get(guuid).isPermanent())
				{
								
					CGroupController.setCurrentUUID(guuid);
					CGroupController.setLastCreatedGroupUUID(guuid);
					CGroupController.show_group_bubblemenu(guuid, PieMenuButton.SHOWON_SCRAP_CREATE, false);
				}
				else
				{
					CGroupController.show_group_bubblemenu(guuid, false);
				}
	
				if (BubbleMenu.highlightedParentGroup != 0l)
				{
					CGroupController.groupdb.get(BubbleMenu.highlightedParentGroup).highlight_off();
					CGroupController.groupdb.get(BubbleMenu.highlightedParentGroup).highlight_repaint();
					BubbleMenu.highlightedParentGroup = 0l;
				}
				
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
				
				//This threw a null pointer exception for some reason...
				if (mouseDownPoint != null)
					CGroupController.move_end(this.guuid, e.getX(), e.getY()); 
				
				//Update the menu location in case it was dropped into a list
				//BubbleMenu.moveIconPositions(CGroupController.groupdb.get(guuid).getBounds());
				
				e.consume();
				isActive = false;
			}
			catch(NullPointerException ne)
			{
				ne.printStackTrace();
				System.out.println("Group disappeared while in use: removing Copy/Drag listeners");
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
			}
		}
	}
}
