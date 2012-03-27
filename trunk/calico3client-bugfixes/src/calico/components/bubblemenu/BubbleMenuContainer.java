package calico.components.bubblemenu;

import java.awt.geom.Rectangle2D;

import calico.CalicoDraw;
import calico.controllers.CCanvasController;

import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PComposite;

public class BubbleMenuContainer extends PComposite {
	public BubbleMenuContainer()
	{
		int buttons = BubbleMenu.getButtonCount();
		for(int i=0;i<buttons;i++)
		{
			//Do not use CalicoDraw here. The container is not yet added to a visible canvas.
			//The children need to be available on the active thread.
			addChild( BubbleMenu.getButton(i).getPImage() );
			//CalicoDraw.addChildToNode(this, BubbleMenu.getButton(i).getPImage());
		}
	}
	
	@Override
	public boolean setBounds(Rectangle2D rect)
	{
		//return super.setBounds(getComputedBounds());
		return super.setBounds(rect);
	}
	
	
	/**
	 * This paints the pie slices
	 */
	protected void paint(PPaintContext paintContext)
	{
		
	}
}
