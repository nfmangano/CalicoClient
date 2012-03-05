package calico.components.menus;

public enum ContextMenu
{
	PIE_MENU,
	BUBBLE_MENU;

	/**
	 * The two context menus are currently the pie menu and the bubble menu. This interface allows any entity to listen
	 * for activity in one or both of them.
	 * 
	 * @author Byron Hawkins
	 */
	public interface Listener
	{
		void menuDisplayed(ContextMenu menu);

		void menuCleared(ContextMenu menu);
	}
}
