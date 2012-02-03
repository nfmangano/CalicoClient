package calico.components.bubblemenu;

import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PComposite;

public class BubbleMenuContainer extends PComposite {
	public BubbleMenuContainer()
	{
		int buttons = BubbleMenu.getButtonCount();
		for(int i=0;i<buttons;i++)
		{
			addChild( BubbleMenu.getButton(i).getPImage() );
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
