package calico.perspectives;

import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.events.CalicoEventHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import edu.umd.cs.piccolo.PNode;

public abstract class CalicoPerspective
{
	public interface PerspectiveChangeListener
	{
		void perspectiveChanged(CalicoPerspective perspective);
	}
	
	private static final List<PerspectiveChangeListener> listeners = new ArrayList<PerspectiveChangeListener>();

	public static void addListener(PerspectiveChangeListener listener)
	{
		listeners.add(listener);
	}
	
	public static void removeListener(PerspectiveChangeListener listener)
	{
		listeners.remove(listener);
	}
	
	protected CalicoPerspective()
	{
		Registry.register(this);
	}

	protected abstract void displayPerspective(long contextCanvasId);
	
	protected abstract boolean showBubbleMenu(PNode bubbleHighlighter, PNode bubbleContainer);

	protected abstract void drawPieMenu(PNode pieCrust);

	protected abstract boolean hasPhasicPieMenuActions();

	protected abstract boolean processToolEvent(InputEventInfo event);

	protected abstract long getEventTarget(InputEventInfo event);

	protected abstract void addMouseListener(MouseListener listener);

	protected abstract void removeMouseListener(MouseListener listener);

	public abstract boolean isNavigationPerspective();
	
	public abstract void tickerUpdate();

	public boolean isActive()
	{
		return Active.INSTANCE.currentPerspective == this;
	}

	public void activate()
	{
		Active.INSTANCE.currentPerspective = this;

		for (PerspectiveChangeListener listener : listeners)
		{
			listener.perspectiveChanged(this);
		}
	}

	public static class Registry
	{
		private static final List<CalicoPerspective> perspectives = new ArrayList<CalicoPerspective>();
		private static CalicoPerspective navigationPerspective = null;

		private static void register(CalicoPerspective perspective)
		{
			perspectives.add(perspective);

			if (perspective.isNavigationPerspective())
			{
				navigationPerspective = perspective;
			}
		}

		public static void activateNavigationPerspective()
		{
			activateNavigationPerspective(CCanvasController.getCurrentUUID());
		}
		
		public static void activateNavigationPerspective(long contextCanvasId)
		{
			while (navigationPerspective == null)
			{
				try {
					System.out.println("Warning: navigation perspective is null, waiting 100ms to try again");
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (navigationPerspective != null)
			{
				navigationPerspective.displayPerspective(contextCanvasId);
			}
		}
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

		public static void addMouseListener(MouseListener listener)
		{
			INSTANCE.currentPerspective.addMouseListener(listener);
		}

		public static void removeMouseListener(MouseListener listener)
		{
			INSTANCE.currentPerspective.removeMouseListener(listener);
			
		}
		
		public static CalicoPerspective getCurrentPerspective()
		{
			return INSTANCE.currentPerspective;
		}
		
		public static void displayPerspective(long contextCanvasId)
		{
			INSTANCE.currentPerspective.displayPerspective(contextCanvasId);
		}

		private static final Active INSTANCE = new Active();

		private CalicoPerspective currentPerspective = null;
	}
}
