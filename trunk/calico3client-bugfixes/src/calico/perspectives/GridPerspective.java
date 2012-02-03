package calico.perspectives;

import calico.components.grid.CGrid;
import calico.inputhandlers.InputEventInfo;
import edu.umd.cs.piccolo.PNode;

public class GridPerspective extends CalicoPerspective
{
	private static final GridPerspective INSTANCE = new GridPerspective();

	public static GridPerspective getInstance()
	{
		return INSTANCE;
	}

	protected void drawPieMenu(PNode pieCrust)
	{
		CGrid.getInstance().getCamera().addChild(pieCrust);
		CGrid.getInstance().getCamera().repaintFrom(pieCrust.getBounds(), pieCrust);
	}
	
	protected boolean hasPhasicPieMenuActions()
	{
		return false;
	}

	protected boolean processToolEvent(InputEventInfo event)
	{
		return false;
	}
	
	@Override
	protected long getEventTarget(InputEventInfo event)
	{
		return 0L; // i.e., the grid
	}
	
	@Override
	protected boolean showBubbleMenu(PNode bubbleHighlighter, PNode bubbleContainer)
	{
		return false;
	}
}
