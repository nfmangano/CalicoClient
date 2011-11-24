package calico.inputhandlers;

import java.awt.Point;

public interface PressAndHoldAction {
	
	
	
	//Each mouseDown, mouseDrag, and mouseRelease set should have an identifier so we can make sure these belong to the same action
	//Note: After mouseUp has completed, this method should return zero.
	public long getLastAction();
	
	public Point getMouseDown();
	
	public Point getMouseUp();
	
	public Point getLastPoint();
	
	public double getDraggedDistance();
	
	//If press and hold is successful, we need to abort the action
	public void pressAndHoldCompleted();
	
	public void pressAndHoldAbortedEarly();
	
	public void openMenu(long potScrap, long group, Point point);

}
