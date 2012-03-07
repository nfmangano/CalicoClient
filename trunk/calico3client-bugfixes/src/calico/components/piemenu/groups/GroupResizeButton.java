package calico.components.piemenu.groups;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import calico.Calico;
import calico.CalicoDraw;
import calico.components.CGroup;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.decorators.CListDecorator;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.inputhandlers.InputEventInfo;
import edu.umd.cs.piccolo.nodes.PImage;

public class GroupResizeButton extends PieMenuButton
{
	
	long uuid;
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_CREATE | PieMenuButton.SHOWON_SCRAP_MENU;
	private boolean isActive = false;
	
	public GroupResizeButton(long u)
	{
		super("group.resize");
		uuid = u;
	}
	
	public void onPressed(InputEventInfo ev)
	{	
		if (!CGroupController.exists(uuid) || isActive)
		{
			return;
		}
		
		isActive = true;
		
		long canvasUUID = CGroupController.groupdb.get(uuid).getCanvasUID();
		PImage ghost = new PImage();
		ghost.setImage(CGroupController.groupdb.get(uuid).getFamilyPicture());
		//ghost.setBounds(CGroupController.groupdb.get(uuid).getBounds().getBounds2D());
		CalicoDraw.setNodeBounds(ghost, CGroupController.groupdb.get(uuid).getBounds().getBounds2D());
//		ghost.scale(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer().getScale());
		
		//CCanvasController.canvasdb.get(canvasUUID).getLayer().addChild(ghost);
		CalicoDraw.addChildToNode(CCanvasController.canvasdb.get(canvasUUID).getLayer(), ghost);
		
		RotateMouseListener rotateDragListener = new RotateMouseListener(ghost, canvasUUID, uuid);
		CCanvasController.canvasdb.get(canvasUUID).addMouseListener(rotateDragListener);
		CCanvasController.canvasdb.get(canvasUUID).addMouseMotionListener(rotateDragListener);

		
		//pass click event on to this listener since it will miss it
		rotateDragListener.mousePressed(ev.getPoint());
		
		ev.stop();
		BubbleMenu.isPerformingBubbleMenuAction = true;
		
		
		Calico.logger.debug("CLICKED GROUP ROTATE BUTTON");
	}
	
	private class RotateMouseListener implements MouseMotionListener, MouseListener
	{
		PImage ghost;
		Point2D.Double prevPoint, mouseDownPoint, mouseUpPoint;
		Point2D.Double centerPoint;
		long cuuid, guuid;
		boolean isListItem;
		
		public RotateMouseListener(PImage g, long canvasUUID, long groupUUID)  {
			ghost = g;
			Point2D cp = CGroupController.groupdb.get(groupUUID).getMidPoint();
			centerPoint = new Point2D.Double(cp.getX(), cp.getY());
			prevPoint = new Point2D.Double();
			cuuid = canvasUUID;
			guuid = groupUUID;
			
			isListItem = false;
			CGroup cGroup = CGroupController.groupdb.get(guuid);
			while(cGroup.getParentUUID() != 0l)
			{
				cGroup = CGroupController.groupdb.get(cGroup.getParentUUID());
				if (cGroup instanceof CListDecorator)
				{
					isListItem = true;
					break;
				}
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			try
			{
				
				Point scaledPoint = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getUnscaledPoint(e.getPoint());
				
				Point2D.Double p = new Point2D.Double(scaledPoint.getX(), scaledPoint.getY());
				/*double angle = getAngle(prevPoint, p, centerPoint);
				ghost.rotateAboutPoint(angle, centerPoint);*/
				
				double oldScale = getScaleMP(prevPoint);
				double newScale = getScaleMP(p);
				double scale = newScale/oldScale;
				ghost.scaleAboutPoint(scale, centerPoint);
	
				//ghost.repaintFrom(ghost.getBounds(), ghost);
				CalicoDraw.repaintNode(ghost);
	
				BubbleMenu.moveIconPositions(ghost.getFullBounds());
				
				
				prevPoint.x = scaledPoint.getX();
				prevPoint.y = scaledPoint.getY();
				e.consume();
			}
			catch(NullPointerException ne)
			{
				System.out.println("Group disappeared while in use: removing Resize listeners");
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
				//CCanvasController.canvasdb.get(cuuid).getLayer().removeChild(ghost);
				CalicoDraw.removeChildFromNode(CCanvasController.canvasdb.get(cuuid).getLayer(), ghost);
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
				//BubbleMenu.setSelectedButton(GroupRotateButton.class.getName());
				Point scaledPoint = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getUnscaledPoint(p);
				
				prevPoint.x = scaledPoint.getX();
				prevPoint.y = scaledPoint.getY();
				mouseDownPoint = new Point2D.Double(scaledPoint.getX(), scaledPoint.getY()); 
				
				if (!isListItem)
				{
					CGroupController.move_start(guuid);
				}
			}
			catch(NullPointerException ne)
			{
				System.out.println("Group disappeared while in use: removing Resize listeners");
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			try
			{
	
				Point scaledPoint = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getUnscaledPoint(e.getPoint());
				
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
				
				mouseUpPoint = new Point2D.Double(scaledPoint.getX(), scaledPoint.getY());
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
				//CCanvasController.canvasdb.get(cuuid).getLayer().removeChild(ghost);
				CalicoDraw.removeChildFromNode(CCanvasController.canvasdb.get(cuuid).getLayer(), ghost);
				
				/*double angle = getAngle(mouseDownPoint, mouseUpPoint, centerPoint);
				CGroupController.rotate(guuid, angle);*/
				
				//Turn off highlighter before resize to make sure it does not leave artifacts. 
				CGroupController.groupdb.get(guuid).highlight_off();
				CGroupController.groupdb.get(guuid).highlight_repaint();
				
				double scale = getScaleMP(mouseUpPoint);
				CGroupController.scale(guuid, scale, scale);
				if (!isListItem)
				{
					CGroupController.move_end(this.guuid, e.getX(), e.getY()); 
				}
				
				//Turn highlighter back on for resized version
				CGroupController.groupdb.get(guuid).highlight_on();
				
				e.consume();
	//			PieMenu.isPerformingPieMenuAction = false;
				
				if(!CGroupController.groupdb.get(guuid).isPermanent())
				{
					//CGroupController.drop(guuid);
				}
				
				isActive = false;
			}
			catch(NullPointerException ne)
			{
				System.out.println("Group disappeared while in use: removing Resize listeners");
				CCanvasController.canvasdb.get(cuuid).removeMouseListener(this);
				CCanvasController.canvasdb.get(cuuid).removeMouseMotionListener(this);
				//CCanvasController.canvasdb.get(cuuid).getLayer().removeChild(ghost);
				CalicoDraw.removeChildFromNode(CCanvasController.canvasdb.get(cuuid).getLayer(), ghost);
			}
		}
		
		//gets angle between two points with respect to the third point
		/*double getAngle(Point2D point1, Point2D point2, Point2D midPoint)
		{
			Point2D adjustedPoint1 = new Point2D.Double(point1.getX() - midPoint.getX(), point1.getY() - midPoint.getY());
			double point1Angle = getAngle(adjustedPoint1, new Point(0,0));
			
			Point2D adjustedPoint2 = new Point2D.Double(point2.getX() - midPoint.getX(), point2.getY() - midPoint.getY());
			double point2Angle = getAngle(adjustedPoint2, new Point(0,0));
			
			double angle = point1Angle - point2Angle;
			
			return angle;			
		}*/
		
		//taken from: http://bytes.com/topic/c/answers/452165-finding-angle-between-two-points#post1728631
		/*double getAngle(Point2D point1, Point2D point2 )
		{
			double theta;
			if ( point2.getX() - point1.getX() == 0 )
				if ( point2.getY() > point1.getY() )
					theta = 0;
				else
					theta = Math.PI;
			else
			{
				theta = Math.atan( (point2.getY() - point1.getY()) / (point2.getX() - point1.getX()) );
				if ( point2.getX() > point1.getX() )
					theta = Math.PI / 2.0f - theta;
				else
					theta = Math.PI * 1.5f - theta;
			};
			return theta;
		}*/
		
		private double getScaleMP(Point2D.Double p)
		{
			double originalDistance = Math.sqrt(Math.pow(mouseDownPoint.getY() - centerPoint.getY(), 2) 
											+ Math.pow(mouseDownPoint.getX() - centerPoint.getX(), 2));
			double newDistance = Math.sqrt(Math.pow(p.getY() - centerPoint.getY(), 2) 
					+ Math.pow(p.getX() - centerPoint.getX(), 2));
			
			return newDistance / originalDistance;
		}

	}
	
	//Leave this empty
	public void setHaloEnabled(boolean enable)
	{
		
	}
}
