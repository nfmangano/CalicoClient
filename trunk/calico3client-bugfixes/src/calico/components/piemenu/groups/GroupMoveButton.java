package calico.components.piemenu.groups;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import calico.CalicoDataStore;
import calico.components.CGroup;
import calico.components.CViewportCanvas;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CViewportController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.*;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import edu.umd.cs.piccolo.nodes.PImage;

public class GroupMoveButton extends PieMenuButton
{
	
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_CREATE | PieMenuButton.SHOWON_SCRAP_MENU;	
	private long guuid = 0L;
	
	public GroupMoveButton(long uuid)
	{
		super("group.move");
		guuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
//		ev.stop();
//
//		CGroupController.setCopyUUID(guuid);
		
		long canvasUUID = CGroupController.groupdb.get(guuid).getCanvasUID();
		
//		PImage ghost = new PImage();
//		
//		if (CalicoDataStore.isInViewPort)
//		{
//			ghost.setImage(CViewportController.getScaledGroupImage(guuid));
//			ghost.setBounds(CViewportController.getScaledGroupBounds(guuid));
//		}
//		else
//		{
//			ghost.setImage(CGroupController.groupdb.get(guuid).getFamilyPicture());
//			ghost.setBounds(CGroupController.groupdb.get(guuid).getBounds().getBounds2D());
//		}

		
		
		
//		if CViewportCanvas.getInstance().getCanvasIdOfPoint(p)
//		if (CalicoDataStore.isInViewPort)
		
//		CCanvasController.canvasdb.get(canvasUUID).getLayer().addChild(ghost);
//		CCanvasController.canvasdb.get(canvasUUID).getCamera().addChild(ghost);
		
		TranslateMouseListener resizeDragListener = new TranslateMouseListener(canvasUUID, guuid);
		if (CalicoDataStore.isInViewPort)
		{
			CViewportCanvas.getInstance().addMouseListener(resizeDragListener);
			CViewportCanvas.getInstance().addMouseMotionListener(resizeDragListener);
		}
		else
		{
			CCanvasController.canvasdb.get(canvasUUID).addMouseListener(resizeDragListener);
			CCanvasController.canvasdb.get(canvasUUID).addMouseMotionListener(resizeDragListener);
		}
		
		//pass click event on to this listener since it will miss it
		resizeDragListener.mousePressed(ev.getPoint());
		
		ev.stop();
		BubbleMenu.isPerformingBubbleMenuAction = true;
		
		System.out.println("CLICKED GROUP MOVE BUTTON");
		//CGroupController.drop(group_uuid);
	}
		
	private class TranslateMouseListener implements MouseMotionListener, MouseListener
	{
		Point prevPoint, mouseDownPoint;
		long cuuid, guuid;
		
		public TranslateMouseListener(long canvasUUID, long groupUUID)  {
			prevPoint = new Point();
			cuuid = canvasUUID;
			guuid = groupUUID;
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			double viewportScale = 1/CalicoDataStore.gridObject.getViewportScale();
			Point scaledPoint = new Point((int)(e.getPoint().x * viewportScale), (int)(e.getPoint().y * viewportScale));
			
			if (mouseDownPoint == null)
			{
				prevPoint.x = scaledPoint.x;
				prevPoint.y = scaledPoint.y;
				mouseDownPoint = scaledPoint;
				CGroupController.move_start(guuid);
			}
			
			if (BubbleMenu.highlightedGroup != 0l)
			{
				CGroupController.groupdb.get(BubbleMenu.highlightedGroup).highlight_off();
				
			}
			CGroupController.move(guuid, (int)(scaledPoint.x - prevPoint.x), scaledPoint.y - prevPoint.y);
			
			BubbleMenu.moveIconPositions(CGroupController.groupdb.get(guuid).getBounds());
			
			long smallest = 0;
			if ((smallest = CGroupController.groupdb.get(guuid).calculateParent(e.getPoint().x, e.getPoint().y)) != 0l)
			{
				CGroupController.groupdb.get(smallest).highlight_on();
			}
			CGroupController.groupdb.get(guuid).highlight_on();
			prevPoint.x = scaledPoint.x;
			prevPoint.y = scaledPoint.y;
			e.consume();
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
			//BubbleMenu.setSelectedButton(GroupMoveButton.class.getName());
			Point scaledPoint = p;
			
			prevPoint.x = 0;
			prevPoint.y = 0;
			mouseDownPoint = null;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			//BubbleMenu.setSelectedButton(null);
			double viewportScale = 1/CalicoDataStore.gridObject.getViewportScale();
			Point scaledPoint = new Point((int)(e.getPoint().x * viewportScale), (int)(e.getPoint().y * viewportScale));
			
			//if (BubbleMenu.highlightedGroup != 0l)
				//CGroupController.groupdb.get(BubbleMenu.highlightedGroup).highlight_off();
			
			if (CalicoDataStore.isInViewPort)
			{
				CViewportCanvas.getInstance().removeMouseListener(this);
				CViewportCanvas.getInstance().removeMouseMotionListener(this);
			}
			else
			{
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
			}
			
			//This threw a null pointer exception for some reason...
			if (mouseDownPoint != null)
				CGroupController.move_end(this.guuid, e.getX(), e.getY()); //scaledPoint.x - mouseDownPoint.x, scaledPoint.y - mouseDownPoint.y);
//			Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_MOVE_END,
//					this.guuid,
//				scaledPoint.x - mouseDownPoint.x, 
//				scaledPoint.y - mouseDownPoint.y
//			));
			//CGroupController.groupdb.get(guuid).highlight_on();
			//System.out.println(guuid + " on");
			e.consume();
//			PieMenu.isPerformingPieMenuAction = false;
			
			if(!CGroupController.groupdb.get(guuid).isPermanent())
			{
				//CGroupController.drop(guuid);
			}
		}
	}
}
