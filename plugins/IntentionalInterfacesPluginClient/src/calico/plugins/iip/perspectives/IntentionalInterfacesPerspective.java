package calico.plugins.iip.perspectives;

import java.awt.event.MouseListener;

import calico.inputhandlers.InputEventInfo;
import calico.perspectives.CalicoPerspective;
import calico.plugins.iip.components.graph.IntentionGraph;
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
		IntentionGraph.getInstance().getCamera().addChild(pieCrust);
		IntentionGraph.getInstance().repaint();
	}
	
	@Override
	protected long getEventTarget(InputEventInfo event)
	{
		// look for arrows, CICs
		return 0;
	}
	
	@Override
	protected boolean hasPhasicPieMenuActions()
	{
		return true;
	}
	
	@Override
	protected boolean processToolEvent(InputEventInfo event)
	{
		// if the event is on the menubar, process it as such and return true to consume
		return false;
	}
	
	@Override
	protected boolean showBubbleMenu(PNode bubbleHighlighter, PNode bubbleContainer)
	{
		IntentionGraph.getInstance().getCamera().addChild(bubbleHighlighter);
		IntentionGraph.getInstance().getCamera().addChild(bubbleContainer);
		return true;
	}
}
