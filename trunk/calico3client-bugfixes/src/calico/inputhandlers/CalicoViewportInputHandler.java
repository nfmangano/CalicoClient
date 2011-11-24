package calico.inputhandlers;

import java.awt.Point;
import java.awt.Polygon;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.CViewportCanvas;
import calico.controllers.CViewportController;

/**
 * 
 * @author Nicolas Lopez
 * This Handler works a bit different since it must 
 * Transform inputs from viewport size to actual canvas size
 * 
 */
public class CalicoViewportInputHandler {

	//private static boolean draggingGroup = false;
	
	private static Point pressPoint;
	private static boolean pressedForDragging;
	
	/**
	 * Transforms the event and returns true or processes it if its an event that should be handleded here
	 * @param ev
	 * @return returns true if the event should be processed or false otherwise (if we are going to process it here
	 * for example because we are copying a group from one canvas to another)
	 */
	public static boolean transformInput(InputEventInfo ev){
		if(CViewportCanvas.getInstance().isPointOnMenuBar(ev.getPoint())){
			//do nothing if the click is in the menu bar
			return true;
		}
		else if(!pressedForDragging && CViewportCanvas.getInstance().isClickOnFocusedCanvas(ev.getPoint())){//&&!draggingGroup){
			
			//if the click is in the focused canvas
			//scale the point and continue executing
			ev.setPoint(CViewportCanvas.getInstance().scalePointToFocusedCanvas(ev.getPoint()));
			//Calico.logger.debug("click is in the focused one, scale and continue");
			return true;
		}
		else if(pressedForDragging|| CViewportCanvas.getInstance().isClickOnBoundaries(ev.getPoint())){
			//Calico.logger.debug("CLICK is on Borders MUST DRAG");			
			switch(ev.getAction())
			{
				case InputEventInfo.ACTION_DRAGGED:actionDragged(ev);break;
				case InputEventInfo.ACTION_PRESSED:actionPressed(ev);break;
				case InputEventInfo.ACTION_RELEASED:actionReleased(ev);break;			
				default:
					// Lazy ass
					break;
			}
			return false;
		}
		else if((ev.getAction()==InputEventInfo.ACTION_DRAGGED||ev.getAction()==InputEventInfo.ACTION_RELEASED)){//&&!draggingGroup){
			
			//if we are dragging from one canvas to another I'll just keep the focused canvas until he actually releases
			//and I will set the point so that it is within the bounds of the canvas
			ev.setPoint(CViewportCanvas.getInstance().scalePointToFocusedCanvas(ev.getPoint()));
			Point p = ev.getPoint();
			if(p.x>CalicoDataStore.ScreenWidth){
				p.x=CalicoDataStore.ScreenWidth;
			}
			if(p.x<4){
				p.x=4;
			}
			if(p.y>CalicoDataStore.ScreenHeight-CViewportCanvas.HEIGHT_REDUCE_SIZE){
				p.y=CalicoDataStore.ScreenHeight-CViewportCanvas.HEIGHT_REDUCE_SIZE;
			}
			if(p.y<0){
				p.y=0;
			}
			ev.setPoint(p);
			return true;
		}
		else {//if(ev.getButton()==InputEventInfo.BUTTON_LEFT){			
			//change the focused canvas
			CViewportController.changeFocusedCanvas(ev.getPoint());
			//scale the point and continue executing			
			ev.setPoint(CViewportCanvas.getInstance().scalePointToFocusedCanvas(ev.getPoint()));
			return true;
		}/*//deprecated: now if we want to copy a group from one canvas to another we use the same functionality as if we were not in the viewport  
		    else{			
			//we enter here if: 
			// the click is outside the focused canvas
			// the click is a right click
			// and if we are dragging a group
			//
			if(ev.getAction()==InputEventInfo.ACTION_PRESSED)
			{				
				long pressedGroup = CViewportCanvas.getInstance().getSmallestGroupPressedInCell(ev);
				if (pressedGroup!= 0L){
					draggingGroup = true;					
					CViewportCanvas.getInstance().drawPressedGroup(pressedGroup, ev.getX(), ev.getY());
				}else{
					//if there was no group pressed jump focus to that canvas so the rest of the events afterwards will be ok
					CViewportController.changeFocusedCanvas(ev.getPoint());
					//scale the point and continue executing			
					ev.setPoint(CViewportCanvas.getInstance().scalePointToFocusedCanvas(ev.getPoint()));
					return true;
				}
			}
			if(ev.getAction()==InputEventInfo.ACTION_DRAGGED){
				
				CViewportCanvas.getInstance().movePressedGroup(ev.getX(), ev.getY());
			}
			if(ev.getAction()==InputEventInfo.ACTION_RELEASED){
				
				draggingGroup = false;
				//now copy the group to the new canvas where the user dropped it
				CViewportController.copyDraggedGroupToCanvas(ev.getPoint());
				CViewportCanvas.getInstance().removePressedGroup();
				
			}			
			return false;
		}*/
	}

	
	public static void actionDragged(InputEventInfo ev) {
		//Calico.logger.debug("Running actionDragged");
		if(pressedForDragging){
			Point p=ev.getPoint();
			double deltaX=p.getX()-pressPoint.getX();
			double deltaY=p.getY()-pressPoint.getY();
			CViewportController.dragViewport(deltaX, deltaY);
			pressPoint=p;	
		}
		
	}


	public static void actionPressed(InputEventInfo ev) {
		
		pressPoint=ev.getPoint();
		pressedForDragging=true;
	}

	
	public static void actionReleased(InputEventInfo ev) {
		
		pressPoint=null;
		pressedForDragging=false;
		
	}
	
}
