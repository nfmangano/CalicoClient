package calico.perspectives;

import calico.inputhandlers.InputEventInfo;
import edu.umd.cs.piccolo.PNode;

public abstract class CalicoPerspective
{
	protected abstract boolean showBubbleMenu(PNode bubbleHighlighter, PNode bubbleContainer);

	protected abstract void drawPieMenu(PNode pieCrust);
	
	protected abstract boolean hasPhasicPieMenuActions();
	
	protected abstract boolean processToolEvent(InputEventInfo event);
	
	protected abstract long getEventTarget(InputEventInfo event);
	
	public boolean isActive()
	{
		return Active.INSTANCE.currentPerspective == this;
	}
	
	public void activate()
	{
		Active.INSTANCE.currentPerspective = this;
	}
	
	public static class Active
	{
		public static boolean showBubbleMenu(PNode bubbleHighlighter, PNode bubbleContainer)
		{
			return INSTANCE.currentPerspective.showBubbleMenu(bubbleHighlighter, bubbleContainer);
		}
		
		public static void drawPieMenu(PNode pieCrust)
		{
			INSTANCE.currentPerspective.drawPieMenu(pieCrust);
		}
		
		public static boolean hasPhasicPieMenuActions()
		{
			return INSTANCE.currentPerspective.hasPhasicPieMenuActions();
		}
		
		public static boolean processToolEvent(InputEventInfo event)
		{
			return INSTANCE.currentPerspective.processToolEvent(event);
		}
		
		public static long getEventTarget(InputEventInfo event)
		{
			return INSTANCE.currentPerspective.getEventTarget(event);
		}
		
		private static final Active INSTANCE = new Active();
		
		private CalicoPerspective currentPerspective = null;
	}
}
