package calico.components.composable;

import java.util.ArrayList;

import calico.networking.netstuff.CalicoPacket;

public interface Composable {
	
	public static final int TYPE_CONNECTOR = 0;
	public static final int TYPE_SCRAP = 1;

	/**
	 * Get all composable elements for this component
	 * @return
	 */
	public CalicoPacket[] getComposableElements();
	
	/**
	 * Use this method to first removeAll and then add elements that should be present when the component is created
	 */
	public void resetToDefaultElements();
	
	/**
	 * Removes ALL elements, even ones that were added by default
	 * IE. This will remove things such as highlighting a selected connector
	 * Use reset to bring those back if needed
	 */
	public void removeAllElements();
	
	/**
	 * Get the type (connector/scrap). Necessary since there is no ComposableController
	 * @return
	 */
	public int getComposableType();
	
	public void redraw();
}
