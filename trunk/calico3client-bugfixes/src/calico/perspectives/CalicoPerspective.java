package calico.perspectives;

import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import edu.umd.cs.piccolo.PNode;

public abstract class CalicoPerspective
{
	protected CalicoPerspective()
	{
		Registry.register(this);
	}

	protected abstract boolean showBubbleMenu(PNode bubbleHighlighter, PNode bubbleContainer);

	protected abstract void drawPieMenu(PNode pieCrust);

	protected abstract boolean hasPhasicPieMenuActions();

	protected abstract boolean processToolEvent(InputEventInfo event);

	protected abstract long getEventTarget(InputEventInfo event);

	protected abstract void addMouseListener(MouseListener listener);

	protected abstract void removeMouseListener(MouseListener listener);

	protected abstract boolean isNavigationPerspective();

	public boolean isActive()
	{
		return Active.INSTANCE.currentPerspective == this;
	}

	public void activate()
	{
		Active.INSTANCE.currentPerspective = this;

		CalicoInputManager.perspectiveChanged();
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
			CCanvasController.loadCanvas(CCanvasController.getCanvasByIndex(1).uuid);

			/*
			 * if (navigationPerspective != null) { navigationPerspective.activate(); }
			 */
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

		private static final Active INSTANCE = new Active();

		private CalicoPerspective currentPerspective = null;
	}
}
