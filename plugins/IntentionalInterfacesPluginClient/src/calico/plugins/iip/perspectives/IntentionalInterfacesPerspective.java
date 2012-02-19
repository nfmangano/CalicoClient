package calico.plugins.iip.perspectives;

import java.awt.event.MouseListener;

import calico.inputhandlers.InputEventInfo;
import calico.perspectives.CalicoPerspective;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.inputhandlers.CIntentionCellInputHandler;
import edu.umd.cs.piccolo.PNode;

public class IntentionalInterfacesPerspective extends CalicoPerspective
{
	private static final IntentionalInterfacesPerspective INSTANCE = new IntentionalInterfacesPerspective();

	public static IntentionalInterfacesPerspective getInstance()
	{
		return INSTANCE;
	}

	@Override
	protected void addMouseListener(MouseListener listener)
	{
		IntentionGraph.getInstance().addMouseListener(listener);
	}

	@Override
	protected void removeMouseListener(MouseListener listener)
	{
		IntentionGraph.getInstance().removeMouseListener(listener);
	}

	@Override
	protected void drawPieMenu(PNode pieCrust)
	{
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS).addChild(pieCrust);
		IntentionGraph.getInstance().repaint();
	}

	@Override
	protected long getEventTarget(InputEventInfo event)
	{
		// this will be entirely wrong if the pie menu was activated on a cell and then it was cleared. The input
		// handler has no idea that the pie menu was cleared, and it will continue to think that cell is active until
		// the next event occurs (for which it will report the old active cell as being still active now).

		long cic_uuid = CIntentionCellInputHandler.getInstance().getActiveCell();
		if (cic_uuid >= 0L)
		{
			return cic_uuid;
		}

		cic_uuid = CIntentionCellController.getInstance().getCellAt(event.getGlobalPoint());
		if (cic_uuid >= 0L)
		{
			CIntentionCellInputHandler.getInstance().setCurrentCellId(cic_uuid);
			return cic_uuid;
		}

		// look for arrows, CICs, else:
		return IntentionGraph.getInstance().getId();
	}

	@Override
	protected boolean hasPhasicPieMenuActions()
	{
		return true;
	}

	@Override
	protected boolean processToolEvent(InputEventInfo event)
	{
		return IntentionGraph.getInstance().processToolEvent(event);
	}

	@Override
	protected boolean showBubbleMenu(PNode bubbleHighlighter, PNode bubbleContainer)
	{
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS).addChild(bubbleHighlighter);
		IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.TOOLS).addChild(bubbleContainer);
		return true;
	}
}
