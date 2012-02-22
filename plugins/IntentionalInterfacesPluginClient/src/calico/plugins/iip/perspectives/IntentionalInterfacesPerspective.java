package calico.plugins.iip.perspectives;

import java.awt.event.MouseListener;

import calico.inputhandlers.InputEventInfo;
import calico.perspectives.CalicoPerspective;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.inputhandlers.CCanvasLinkInputHandler;
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
		long target_uuid = CIntentionCellInputHandler.getInstance().getActiveCell();
		if (target_uuid >= 0L)
		{
			return target_uuid;
		}
		
		target_uuid = CCanvasLinkInputHandler.getInstance().getActiveLink();
		if (target_uuid >= 0L)
		{
			return target_uuid;
		}

		target_uuid = CCanvasLinkController.getInstance().getLinkAt(event.getGlobalPoint());
		if (target_uuid >= 0L)
		{
			CCanvasLinkInputHandler.getInstance().setCurrentLinkId(target_uuid);
			return target_uuid;
		}
		
		target_uuid = CIntentionCellController.getInstance().getCellAt(event.getGlobalPoint());
		if (target_uuid >= 0L)
		{
			CIntentionCellInputHandler.getInstance().setCurrentCellId(target_uuid);
			return target_uuid;
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
