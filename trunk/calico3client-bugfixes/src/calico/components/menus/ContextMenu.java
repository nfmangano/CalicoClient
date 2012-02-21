package calico.components.menus;

import java.awt.geom.Point2D;

public enum ContextMenu
{
	PIE_MENU,
	BUBBLE_MENU;
	
	/**
	 * The two context menus are currently the pie menu and the bubble menu. This interface allows any entity to listen for
	 * activity in one or both of them.
	 * 
	 * @author Byron Hawkins
	 */
	public interface Listener
	{
		void menuDisplayed(ContextMenu menu, Point2D position);
		
		void menuCleared(ContextMenu menu);
	}
}
