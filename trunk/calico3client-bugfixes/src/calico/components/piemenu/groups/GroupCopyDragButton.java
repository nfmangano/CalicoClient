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
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import edu.umd.cs.piccolo.nodes.PImage;

public class GroupCopyDragButton extends PieMenuButton
{
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_CREATE | PieMenuButton.SHOWON_SCRAP_MENU;
	private long guuid = 0L;
	
	public GroupCopyDragButton(long uuid)
	{
		super("group.copy");

		guuid = uuid;
	}
	
	public void onPressed(InputEventInfo ev)
	{
//		ev.stop();
//
//		CGroupController.setCopyUUID(guuid);
		
		long canvasUUID = CGroupController.groupdb.get(guuid).getCanvasUID();
		
		PImage ghost = new PImage();
		
		ghost.setImage(CGroupController.groupdb.get(guuid).getFamilyPicture());
		ghost.setBounds(CGroupController.groupdb.get(guuid).getBounds().getBounds2D());
		ghost.scale(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer().getScale());
//			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).repaint(ghost.getBounds());
		
//		if CViewportCanvas.getInstance().getCanvasIdOfPoint(p)
//		if (CalicoDataStore.isInViewPort)
		
//		CCanvasController.canvasdb.get(canvasUUID).getLayer().addChild(ghost);
		CCanvasController.canvasdb.get(canvasUUID).getCamera().addChild(ghost);
		
		TranslateMouseListener resizeDragListener = new TranslateMouseListener(ghost, canvasUUID, guuid);
		CCanvasController.canvasdb.get(canvasUUID).addMouseListener(resizeDragListener);
		CCanvasController.canvasdb.get(canvasUUID).addMouseMotionListener(resizeDragListener);
		
		//pass click event on to this listener since it will miss it
		resizeDragListener.mousePressed(ev.getPoint());
		
		ev.stop();
//		ev.getMouseEvent().consume();
		BubbleMenu.isPerformingBubbleMenuAction = true;
		
		System.out.println("CLICKED GROUP COPY_DRAG BUTTON");
		//CGroupController.drop(group_uuid);
	}
	
	private void paste(long guuid, Point mouseUp, Point2D mouseDownPoint, long originUUID, long new_guuid)
	{
		Point2D gMidPoint = CGroupController.groupdb.get(guuid).getMidPoint();
		if (CGroupController.exists(guuid))
		{
		
			CGroup group = CGroupController.groupdb.get(guuid);			

			Point2D center = gMidPoint;
			long canvasUUID;

			
			
			Point2D.Double shiftDelta = new Point2D.Double(
					gMidPoint.getX() - mouseDownPoint.getX() + mouseUp.getX(),
					gMidPoint.getY() - mouseDownPoint.getY() + mouseUp.getY());
			
			int shift_x = (int)(shiftDelta.getX() - center.getX());
			int shift_y = (int)(shiftDelta.getY() - center.getY());
			canvasUUID = CCanvasController.getCurrentUUID();
			Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_COPY_TO_CANVAS, 
					guuid,
					canvasUUID,
					new_guuid,
					shift_x,
					shift_y,
					(int)mouseUp.getX(),
					(int)mouseUp.getY()
				));
		}
	}
		
	private class TranslateMouseListener implements MouseMotionListener, MouseListener
	{
		PImage ghost;
		Point2D.Double prevPoint, mouseDownPoint, mouseUpPoint;
		Point2D.Double centerPoint;
		Point2D.Double scrapOriginPoint;
		long cuuid, guuid;
		
		public TranslateMouseListener(PImage g, long canvasUUID, long groupUUID)  {
			ghost = g;
			Point2D cp = CGroupController.groupdb.get(groupUUID).getMidPoint();
			centerPoint = new Point2D.Double(cp.getX(), cp.getY());
			prevPoint = new Point2D.Double();
			cuuid = canvasUUID;
			guuid = groupUUID;
			Point op = CGroupController.groupdb.get(guuid).getPathReference().getBounds().getLocation();
			scrapOriginPoint = new Point2D.Double(op.getX(), op.getY());
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			Point scaledPoint = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getUnscaledPoint(e.getPoint());
			double deltaX = scaledPoint.getX() - prevPoint.getX();
			double deltaY = scaledPoint.getY() - prevPoint.getY();
			
			ghost.translate(deltaX, deltaY);
			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getCamera().validateFullPaint();
			
			BubbleMenu.moveIconPositions(ghost.getFullBounds());
			
			prevPoint.x = scaledPoint.getX();
			prevPoint.y = scaledPoint.getY();
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
			//BubbleMenu.setSelectedButton(GroupCopyDragButton.class.getName());
			Point scaledPoint = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getUnscaledPoint(p);
			
			prevPoint.x = scaledPoint.getX();
			prevPoint.y = scaledPoint.getY();
			mouseDownPoint = new Point2D.Double(scaledPoint.getX(), scaledPoint.getY()); 
			
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			//BubbleMenu.clearMenu();
			Point scaledPoint = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getUnscaledPoint(e.getPoint());
			
			mouseUpPoint = new Point2D.Double(scaledPoint.getX(), scaledPoint.getY());
			CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
			CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
			CCanvasController.canvasdb.get(cuuid).getCamera().removeChild(ghost);
			
			
			long new_guuid = Calico.uuid();
			//System.out.println(guuid + " : " + new_guuid);
			paste(guuid,  scaledPoint, mouseDownPoint, cuuid, new_guuid);
			
			e.consume();
//			PieMenu.isPerformingPieMenuAction = false;
			
			if(!CGroupController.groupdb.get(guuid).isPermanent())
			{
				CGroupController.drop(guuid);
				Point newPoint = BubbleMenu.lastOpenedPosition;
				
				newPoint.x += mouseUpPoint.x - mouseDownPoint.x;
				newPoint.y += mouseUpPoint.y - mouseDownPoint.y;
	
				//BubbleMenu.clearMenu();

				//temporary solution
				try {
					while(!CGroupController.exists(new_guuid))
					{
					Thread.sleep(100);
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				CGroupController.setCurrentUUID(new_guuid);
				CGroupController.setLastCreatedGroupUUID(new_guuid);
				CGroupController.show_group_bubblemenu(new_guuid, newPoint, PieMenuButton.SHOWON_SCRAP_CREATE, false);

			}
			else
			{
			
				Point newPoint = BubbleMenu.lastOpenedPosition;
	
				newPoint.x += mouseUpPoint.x - mouseDownPoint.x;
				newPoint.y += mouseUpPoint.y - mouseDownPoint.y;
	
				//BubbleMenu.clearMenu();
				
				//temporary solution
				try {
					while(!CGroupController.exists(new_guuid))
					{
					Thread.sleep(10);
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				CGroupController.show_group_bubblemenu(new_guuid, newPoint, false);
			}
		}
	}
	
}
