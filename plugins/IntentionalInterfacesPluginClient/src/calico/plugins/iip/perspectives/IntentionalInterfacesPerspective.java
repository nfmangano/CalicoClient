package calico.plugins.iip.perspectives;

import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

import calico.CalicoDataStore;
import calico.controllers.CCanvasController;
import calico.controllers.CHistoryController;
import calico.inputhandlers.InputEventInfo;
import calico.perspectives.CalicoPerspective;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionGraphController;
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

	private boolean notYetDisplayed = true;

	public void displayPerspective(final long contextCanvasId)
	{
		boolean initializing = notYetDisplayed;

//		CHistoryController.getInstance().push(new HistoryFrame(contextCanvasId));

		CalicoDataStore.calicoObj.getContentPane().removeAll();
		CalicoDataStore.calicoObj.getContentPane().add(IntentionGraph.getInstance().getComponent());
		CalicoDataStore.calicoObj.pack();
		CalicoDataStore.calicoObj.setVisible(true);
		CalicoDataStore.calicoObj.repaint();
		activate();

		if (!initializing)
		{
			SwingUtilities.invokeLater(new Runnable() {
				public void run()
				{
					CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(contextCanvasId);
					if (cell == null)
					{
						IntentionGraph.getInstance().fitContents();
					}
					else
					{
						long cellId = cell.getId();
						IntentionGraph.getInstance().zoomToCell(cellId);
					}
				}
			});
		}
	}

	@Override
	public void activate()
	{
		if (notYetDisplayed)
		{
			notYetDisplayed = false;
			IntentionGraphController.getInstance().initializeDisplay();
			CIntentionCellController.getInstance().initializeDisplay();
		}

		super.activate();
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
			CCanvasLinkInputHandler.getInstance().setCurrentLinkId(target_uuid, event.getGlobalPoint());
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

	@Override
	protected boolean isNavigationPerspective()
	{
		return true;
	}

	private class HistoryFrame extends CHistoryController.Frame
	{
		private final long contextCanvasId;

		public HistoryFrame(long contextCanvasId)
		{
			this.contextCanvasId = contextCanvasId;
		}

		protected void restore()
		{
			displayPerspective(contextCanvasId);
		}
	}
}
