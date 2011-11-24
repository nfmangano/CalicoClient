package calico.components.piemenu.groups;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import calico.CalicoDataStore;
import calico.components.CGroup;
import calico.components.CViewportCanvas;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CViewportController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.utils.Geometry;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;

public class GroupCopyDragButton extends PieMenuButton
{
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_CREATE | PieMenuButton.SHOWON_SCRAP_MENU;
	private long guuid = 0L;
	
	public GroupCopyDragButton(long uuid)
	{
		super("grid.canvas.copy");

		guuid = uuid;
	}
	
	public void onClick(InputEventInfo ev)
	{
//		ev.stop();
//
//		CGroupController.setCopyUUID(guuid);
		
		long canvasUUID = CGroupController.groupdb.get(guuid).getCanvasUID();
		
		PImage ghost = new PImage();
		
		if (CalicoDataStore.isInViewPort)
		{
			ghost.setImage(CViewportController.getScaledGroupImage(guuid));
			ghost.setBounds(CViewportController.getScaledGroupBounds(guuid));
//			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).repaint(ghost.getBounds());
		}
		else
		{
			ghost.setImage(CGroupController.groupdb.get(guuid).getFamilyPicture());
			ghost.setBounds(CGroupController.groupdb.get(guuid).getBounds().getBounds2D());
			ghost.scale(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer().getScale());
//			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).repaint(ghost.getBounds());
		}
		
//		if CViewportCanvas.getInstance().getCanvasIdOfPoint(p)
//		if (CalicoDataStore.isInViewPort)
		
//		CCanvasController.canvasdb.get(canvasUUID).getLayer().addChild(ghost);
		CCanvasController.canvasdb.get(canvasUUID).getCamera().addChild(ghost);
		
		TranslateMouseListener resizeDragListener = new TranslateMouseListener(ghost, canvasUUID, guuid);
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
//		ev.getMouseEvent().consume();
		PieMenu.isPerformingPieMenuAction = true;
		
		System.out.println("CLICKED GROUP COPY_DRAG BUTTON");
		//CGroupController.drop(group_uuid);
	}
	
	private void paste(long guuid, Point mouseUp, Point2D mouseDownPoint, long originUUID)
	{
		Point2D gMidPoint = CGroupController.groupdb.get(guuid).getMidPoint();
		if (CGroupController.exists(guuid))
		{
		
			CGroup group = CGroupController.groupdb.get(guuid);			
			
			Point2D center = gMidPoint;
			long canvasUUID;

			
			
			if (CalicoDataStore.isInViewPort)
			{	
				canvasUUID = CViewportCanvas.getInstance().getCanvasIdOfPoint(mouseUp);
				if (canvasUUID == 0l)
					return;
				Point mouseDownScaled=new Point((int)mouseDownPoint.getX(), (int)mouseDownPoint.getY());
				mouseDownScaled=CViewportCanvas.getInstance().scalePointToCanvas(mouseDownScaled, originUUID);
				
				Point dest = CViewportCanvas.getInstance().scalePointToCanvas(mouseUp, canvasUUID);
				
				Point2D.Double shiftDelta = new Point2D.Double(
						gMidPoint.getX() - mouseDownScaled.getX() + dest.getX(),
						gMidPoint.getY() - mouseDownScaled.getY() + dest.getY());
				
				int shift_x = (int)(shiftDelta.getX() - center.getX());
				int shift_y = (int)(shiftDelta.getY() - center.getY());
				
				
				Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_COPY_TO_CANVAS, 
						guuid,
						canvasUUID,
						0L,
						shift_x,
						shift_y,
						(int)mouseUp.getX(),
						(int)mouseUp.getY()
					));
				System.out.println("package sent");
			}
			else{
				
				Point2D.Double shiftDelta = new Point2D.Double(
						gMidPoint.getX() - mouseDownPoint.getX() + mouseUp.getX(),
						gMidPoint.getY() - mouseDownPoint.getY() + mouseUp.getY());
				
				int shift_x = (int)(shiftDelta.getX() - center.getX());
				int shift_y = (int)(shiftDelta.getY() - center.getY());
				canvasUUID = CCanvasController.getCurrentUUID();
				Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_COPY_TO_CANVAS, 
						guuid,
						canvasUUID,
						0L,
						shift_x,
						shift_y,
						(int)mouseUp.getX(),
						(int)mouseUp.getY()
					));
			}
			
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
			Point scaledPoint = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getUnscaledPoint(p);
			
			prevPoint.x = scaledPoint.getX();
			prevPoint.y = scaledPoint.getY();
			mouseDownPoint = new Point2D.Double(scaledPoint.getX(), scaledPoint.getY()); 
			
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			Point scaledPoint = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getUnscaledPoint(e.getPoint());
			
			mouseUpPoint = new Point2D.Double(scaledPoint.getX(), scaledPoint.getY());
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
			CCanvasController.canvasdb.get(cuuid).getCamera().removeChild(ghost);
			
			
			
			paste(guuid,  scaledPoint, mouseDownPoint, cuuid);
			
			e.consume();
//			PieMenu.isPerformingPieMenuAction = false;
			
			if(!CGroupController.groupdb.get(guuid).isPermanent())
			{
				CGroupController.drop(guuid);
			}
		}
	}
	
}
