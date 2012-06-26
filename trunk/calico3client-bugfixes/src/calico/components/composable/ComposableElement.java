package calico.components.composable;

import calico.networking.netstuff.CalicoPacket;
import edu.umd.cs.piccolo.PNode;

public abstract class ComposableElement {
	
	public static final int TYPE_ARROWHEAD = 0;
	public static final int TYPE_CARDINALITY = 1;
	public static final int TYPE_COLOR = 2;
	public static final int TYPE_HIGHLIGHT = 3;
	public static final int TYPE_LABEL = 4;
	public static final int TYPE_LINESTYLE = 5;
	
	/**
	 * The element UUID
	 */
	protected long uuid;
	
	/**
	 * The component UUID
	 */
	protected long cuuid;
	
	
	public ComposableElement(long uuid, long cuuid)
	{
		this.uuid = uuid;
		this.cuuid = cuuid;
	}
	
	public long getElementUUID()
	{
		return this.uuid;
	}
	
	public long getComponentUUID()
	{
		return this.cuuid;
	}
	
	public Composable getComposable()
	{
		return null;
	}
	
	/**
	 * Override this function to add behavior and elements that are only applied once
	 * IE. setting a color variable
	 */
	public void applyElement()
	{
		
	}
	
	/**
	 * Override this function to to change behavior and elements only when the element is being removed
	 * IE. setting a color back to the original color
	 */
	public void removeElement()
	{
		
	}
	
	/**
	 * If a PNode needs to be returned so it can be added to a component on each redraw/repaint, this should return true.
	 * @return
	 */
	public boolean isDrawable()
	{
		return false;
	}
	
	/**
	 * If isDrawable is true, this should return the PNode to add. All calculations for locations, rotations, etc should be done 
	 * within this method.
	 * @return
	 */
	public PNode getNode()
	{
		return null;
	}
	
	public CalicoPacket getPacket(long uuid, long cuuid)
	{
		return null;
	}
	
	/**
	 * Return the packet that stores the element
	 * @return
	 */
	public CalicoPacket getPacket()
	{
		return null;
	}
	
}
