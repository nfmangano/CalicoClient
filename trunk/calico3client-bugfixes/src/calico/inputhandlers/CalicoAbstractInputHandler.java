package calico.inputhandlers;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Arc2D.Double;

import org.shodor.util11.PolygonUtils;

import calico.*;
import calico.CalicoOptions.core;
import calico.CalicoOptions.pen;
import calico.components.CStroke;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.components.piemenu.canvas.ArrowButton;
import calico.components.piemenu.canvas.TextCreate;
import calico.components.piemenu.groups.GroupCopyDragButton;
import calico.components.piemenu.groups.GroupDeleteButton;
import calico.components.piemenu.groups.GroupMoveButton;
import calico.components.piemenu.groups.GroupRotateButton;
import calico.components.piemenu.groups.GroupSetPermanentButton;
import calico.components.piemenu.groups.GroupShrinkToContentsButton;
import calico.components.piemenu.groups.ListCreateButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.*;
import calico.utils.Ticker;
import calico.utils.TickerTask;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;



/**
 * This is the default class for all the input handlers 
 * @author mdempsey
 *
 */
public abstract class CalicoAbstractInputHandler
{

	public static class MenuTimer extends TickerTask
	{
		long stroke;
		long checkInterval;
		double maxHoldDistance;
		int holdTime;
		Point previousPoint;
		long guuid;
		PressAndHoldAction handler;
		private PLayer canvasToPaintTo;
		boolean terminateMenuTimer = false;
		
		public MenuTimer(
				PressAndHoldAction h,
				long uuid, long interval, double maxDistance,
				int holdTime, Point startingPoint, long guuid, PLayer pLayer) {
			handler = h;
			stroke = uuid;
			checkInterval = interval;
			maxHoldDistance = maxDistance;
			this.holdTime = holdTime;
			this.previousPoint = startingPoint;
			this.guuid = guuid;
			canvasToPaintTo = pLayer;
		}
	
		public boolean runtask()
		{
			//This means that either mouse up has occurred or a new stroke is being drawn, so we immediately quit!
			if (handler.getLastAction() != stroke)
			{
				handler.pressAndHoldAbortedEarly();
				return false;
			}
			
			if (handler.getLastPoint().distance(previousPoint) < maxHoldDistance
					&& handler.getLastAction() == stroke && handler.getMouseDown().distance(handler.getLastPoint()) < 30
					&& handler.getDraggedDistance() < 30
					/*&& Geometry.getPolygonLength(CStrokeController.strokes.get(CStrokeController.getCurrentUUID()).getPolygon()) < 30*/)
			{
				final double maxDistance = maxHoldDistance;
				final Point p = new Point(handler.getLastPoint());
				final double radius = CalicoOptions.pen.press_and_hold_menu_radius;
				final long suuid = stroke;
				Arc2D.Double arcShape = new Arc2D.Double(p.x - radius, p.y - radius, radius * 2, radius * 2, -90d, 0, Arc2D.OPEN);
				final PPath arc = new PPath(arcShape);
				arc.setStroke(new BasicStroke(3.0f));
				arc.setStrokePaint(CalicoOptions.pen.press_and_hold_menu_animation_color);
				arc.setTransparency(0.7f);
				
				canvasToPaintTo.addChild(arc);
				PActivity flash = new PActivity(CalicoOptions.pen.press_and_hold_menu_animation_duration, CalicoOptions.pen.press_and_hold_menu_animation_tick_rate, System.currentTimeMillis()) {
					long step = 0;
		      
				    protected void activityStep(long time) {
				    		if (terminateMenuTimer)
				    			this.terminate();
				            super.activityStep(time);
				            if (handler.getLastAction() != suuid || handler.getLastPoint().distance(p) > maxDistance)
				            {
				            	this.terminate();
				            }
				            else
				            {
				            	
				            	double arcLength = (step*2) * 360d/20 + 90;
				            	step++;
				            	if (arcLength > 360 - 2 * 360d/20 + 90) { terminate(); return; }//arcLength = 360 - 2 * 360d/20 + 90;
				            	Arc2D.Double arcShape = new Arc2D.Double(p.x - radius, p.y - radius, radius * 2, radius * 2, arcLength, 10, Arc2D.OPEN);
								final PPath arcChild = new PPath(arcShape);
								arcChild.setStroke(new BasicStroke(3.0f));
								arcChild.setStrokePaint(Color.RED);
								arcChild.setTransparency(0.7f);
								arc.addChild(arcChild);
					            arc.repaint();
					            System.out.println("Stepping through PActivity: " + arcLength + ", " + time);
				            }
				            
				    }
				    
				    protected void activityFinished() {
				    	canvasToPaintTo.removeChild(arc);
				    	if (step > 9)
				    	{
				    		try
				    		{
				    			handler.pressAndHoldCompleted();
				    		}
				    		catch (Exception e)
				    		{
				    			e.printStackTrace();
				    		}
							handler.openMenu(0l, guuid, handler.getLastPoint());
				    	}
				    	else
				    	{
				    		handler.pressAndHoldAbortedEarly();
				    	}
				    }
				};
				// Must schedule the activity with the root for it to run.
				arc.getRoot().addActivity(flash);
	
				return false;
			}
			
			PLayer layer = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer();
			MenuTimer menuTimer = new MenuTimer(handler, stroke, checkInterval, CalicoOptions.core.max_hold_distance, CalicoOptions.core.hold_time, handler.getLastPoint(), guuid, layer);
			Ticker.scheduleIn(CalicoOptions.core.hold_time, menuTimer); 
	
			return false;
		}
		
		public void terminate()
		{
			terminateMenuTimer = true;
		}
	}

	protected PImage modeIcon = null;
	protected boolean isModeIconShowing = false;
	protected long canvas_uid = 0L;
	protected Point modeIconLocation = null;
	
	protected void setupModeIcon(String iconName)
	{
		// setup the icon
		try
		{
			this.modeIcon = new PImage();
			this.modeIcon.setImage( CalicoIconManager.getIconImage(iconName));
		}
		catch(Exception e)
		{
			// Dunno
		}
		
	}
	
	protected void showModeIcon(Point showLocation)
	{
		if(this.isModeIconShowing)
		{
			return;
		}
		
		try
		{
			this.modeIconLocation = new Point(showLocation.x, showLocation.y); 
			this.modeIcon.setBounds(showLocation.getX()-16, showLocation.getY()-16, 16, 16);
			this.modeIcon.setPaintInvalid(true);
			CCanvasController.canvasdb.get(this.canvas_uid).getLayer().addChild(this.modeIcon);
//			CCanvasController.canvasdb.get(this.canvas_uid).getLayer().repaint();
			this.isModeIconShowing = true;
		}
		catch(Exception e)
		{
			
		}
	}
	
	// this just forces it to close
	protected void hideModeIcon()
	{
		if(!this.isModeIconShowing)
		{
			return;
		}	
		this.modeIcon.removeFromParent();
		this.isModeIconShowing = false;
	}
	
	// TODO: check to see if we are out of the bounds
	protected void hideModeIcon(Point mouseCoords)
	{
		if(this.modeIconLocation != null
				&& mouseCoords.distance(this.modeIconLocation)<CalicoOptions.menu.icon_tooltip_dist_threshold)
		{
			return;
		}
		this.hideModeIcon();//new Point(-1*this.modeIconLocation.x, -1*this.modeIconLocation.y));
	}
	
	
	/**
	 * This determines the action taken upon receiving a PRESSED action
	 * @param ev
	 */
	public abstract void actionPressed(InputEventInfo ev);

	/**
	 * This determines the action taken upon receiving a RELEASED action
	 * @param ev
	 */
	public abstract void actionReleased(InputEventInfo ev);

	/**
	 * This determines the action taken upon receiving a DRAGGED action
	 * @param ev
	 */
	public abstract void actionDragged(InputEventInfo ev);

	/**
	 * This determines the action taken upon receiving a CLICKED action
	 * @deprecated We shouldnt use this really, all events should rely on RELEASED instead
	 * @see #actionReleased(InputEventInfo)
	 * @param ev
	 */
	public void actionClicked(InputEventInfo ev)
	{
		actionReleased(ev);
	}
	
	/**
	 * Used for scrolling events
	 * @param ev
	 */
	public void actionScroll(InputEventInfo ev)
	{
		// Ignore
	}
	
	
	public double getDragDistance(Polygon poly)
	{
		if(poly.npoints<=1)
		{
			return 0.0;
		}
		Point p1 = new Point(poly.xpoints[0], poly.ypoints[0]);
		Point p2 = new Point(poly.xpoints[poly.npoints-1], poly.ypoints[poly.npoints-1]);
		
		return PolygonUtils.getLength(p1, p2);
	}

	public static void clickMenu(long potScrap, long group, Point point) {
			long potentialScrap = potScrap;
			
			boolean deleteStroke = false;
			if (potentialScrap == 0l)
//				deleteStroke = true;
//			else
				potentialScrap = CStrokeController.getPotentialScrap(point);
			
			if (group != 0l  //the group must exist
				&& !CGroupController.group_contains_stroke(group, potentialScrap))	//and the group must not contain a potential scrap
			{
				CGroupController.show_group_bubblemenu(group, point);
			}
			else if (potentialScrap > 0l
					|| (potentialScrap) > 0l)
			{
				CStroke stroke = CStrokeController.strokes.get(potentialScrap);
//				if (!CGroupController.checkIfLastTempGroupExists())
//				{
					long previewScrap = stroke.createTemporaryScrapPreview(deleteStroke);
					CGroupController.show_group_bubblemenu(previewScrap, point, PieMenuButton.SHOWON_SCRAP_CREATE, true);
//				}

			}
			else
			{	
				//CCanvasController.show_canvas_piemenu(point);
//				PieMenu.displayPieMenu(point, new TextCreate(), new ArrowButton());
			}
			
		}

}
