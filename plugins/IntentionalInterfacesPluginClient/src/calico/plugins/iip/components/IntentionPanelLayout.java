package calico.plugins.iip.components;

import edu.umd.cs.piccolo.PNode;

/**
 * Layout abstraction for integrating panels from this plugin into the Canvas View.
 * 
 * @author Byron Hawkins
 */
public interface IntentionPanelLayout
{
	/**
	 * Update the bounds of the associated panel according to <code>width</code> and <code>height</code>, along with any
	 * other dimensional values relevant to the rules for layout of the associated panel. This may include adjusting for
	 * screen size, toolbar positions, etc.
	 * 
	 * @param node
	 *            the Piccolo representation of the panel for which bounds should be adjusted
	 * @param width
	 *            width of <code>node</code>
	 * @param height
	 *            height of <code>node</code>
	 */
	void updateBounds(PNode node, double width, double height);
}
