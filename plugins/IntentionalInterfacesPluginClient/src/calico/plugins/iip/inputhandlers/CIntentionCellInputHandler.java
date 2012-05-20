package calico.plugins.iip.inputhandlers;

import java.awt.Point;
import java.awt.geom.Point2D;

import calico.components.bubblemenu.BubbleMenu;
import calico.components.menus.ContextMenu;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.components.piemenu.iip.CreateCanvasCopyButton;
import calico.plugins.iip.components.piemenu.iip.CreateLinkButton;
import calico.plugins.iip.components.piemenu.iip.CreateNewCanvasLinkButton;
import calico.plugins.iip.components.piemenu.iip.DeleteCanvasButton;
import calico.plugins.iip.components.piemenu.iip.EnterCanvasButton;
import calico.plugins.iip.controllers.CIntentionCellController;
import edu.umd.cs.piccolo.util.PBounds;

public class CIntentionCellInputHandler extends CalicoAbstractInputHandler implements ContextMenu.Listener
{
	public static CIntentionCellInputHandler getInstance()
	{
		return INSTANCE;
	}

	private static final CIntentionCellInputHandler INSTANCE = new CIntentionCellInputHandler();

	private static final double DRAG_THRESHOLD = 20.0;
	private static final int BUBBLE_MENU_TYPE_ID = BubbleMenu.registerType(new BubbleMenuComponentType());

	private enum State
	{
		IDLE,
		ACTIVATED,
		DRAG,
		MENU;
	}

	private long currentCellId;

	private State state = State.IDLE;
	private final Object stateLock = new Object();

	private Point mouseDragAnchor;
	private Point2D cellDragAnchor;

	private final EnterCanvasButton enterCanvasButton = new EnterCanvasButton();
	private final DeleteCanvasButton deleteCanvasButton = new DeleteCanvasButton();
	private final CreateLinkButton linkButton = new CreateLinkButton();
	private final CreateNewCanvasLinkButton newCanvasButton = new CreateNewCanvasLinkButton();
	private final CreateCanvasCopyButton copyCanvasButton = new CreateCanvasCopyButton();

	private CIntentionCellInputHandler()
	{
		BubbleMenu.addListener(this);
	}

	public void setCurrentCellId(long currentCellId)
	{
		this.currentCellId = currentCellId;

		CIntentionCellController.getInstance().getCellById(currentCellId).setHighlighted(true);
	}

	public long getActiveCell()
	{
		if (state == State.IDLE)
		{
			return -1L;
		}

		return currentCellId;
	}

	private void moveCurrentCell(Point destination, boolean local)
	{
		double xMouseDelta = (destination.x - mouseDragAnchor.x) / IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();
		double yMouseDelta = (destination.y - mouseDragAnchor.y) / IntentionGraph.getInstance().getLayer(IntentionGraph.Layer.CONTENT).getScale();

		if (local)
		{
			CIntentionCellController.getInstance().moveCellLocal(currentCellId, cellDragAnchor.getX() + xMouseDelta, cellDragAnchor.getY() + yMouseDelta);
		}
		else
		{
			CIntentionCellController.getInstance().moveCell(currentCellId, cellDragAnchor.getX() + xMouseDelta, cellDragAnchor.getY() + yMouseDelta);
		}
	}

	@Override
	public void actionDragged(InputEventInfo event)
	{
		synchronized (stateLock)
		{
			switch (state)
			{
				case ACTIVATED:
					if (event.getGlobalPoint().distance(mouseDragAnchor) >= DRAG_THRESHOLD)
					{
						state = State.DRAG;
					}
					else
					{
						break;
					}
				case DRAG:
					moveCurrentCell(event.getGlobalPoint(), true);
			}
		}
	}

	@Override
	public void actionPressed(InputEventInfo event)
	{
		if (event.isLeftButtonPressed())
		{
			synchronized (stateLock)
			{
				state = State.ACTIVATED;
			}

			mouseDragAnchor = event.getGlobalPoint();
			cellDragAnchor = CIntentionCellController.getInstance().getCellById(currentCellId).getLocation();
		}
	}

	@Override
	public void actionReleased(InputEventInfo event)
	{
		CIntentionCellController.getInstance().getCellById(currentCellId).setHighlighted(false);

		synchronized (stateLock)
		{
			switch (state)
			{
				case DRAG:
					moveCurrentCell(event.getGlobalPoint(), false);
					break;
				case ACTIVATED:
					if (event.getGlobalPoint().distance(mouseDragAnchor) < DRAG_THRESHOLD)
					{
						state = State.MENU;

						if (CCanvasController.canvasdb.size() > 1)
						{
							BubbleMenu.displayBubbleMenu(currentCellId, true, BUBBLE_MENU_TYPE_ID, deleteCanvasButton, enterCanvasButton, linkButton,
									newCanvasButton, copyCanvasButton);
						}
						else
						{
							BubbleMenu.displayBubbleMenu(currentCellId, true, BUBBLE_MENU_TYPE_ID, enterCanvasButton, linkButton, newCanvasButton,
									copyCanvasButton);
						}
					}
					break;
			}

			if (state != State.MENU)
			{
				state = State.IDLE;
			}
		}
	}

	@Override
	public void menuCleared(ContextMenu menu)
	{
		if ((state == State.MENU) && (menu == ContextMenu.BUBBLE_MENU))
		{
			state = State.IDLE;
			CIntentionCellController.getInstance().getCellById(currentCellId).setHighlighted(false);
		}
	}

	@Override
	public void menuDisplayed(ContextMenu menu)
	{
	}

	private static class BubbleMenuComponentType implements BubbleMenu.ComponentType
	{
		@Override
		public PBounds getBounds(long uuid)
		{
			PBounds bounds = CIntentionCellController.getInstance().getCellById(uuid).getGlobalBounds();
//			bounds.setOrigin(bounds.getX() + IntentionGraph.getInstance().getTranslation().getX(), bounds.getY()
//					+ IntentionGraph.getInstance().getTranslation().getY());
			return bounds;
		}

		@Override
		public void highlight(boolean b, long uuid)
		{
			CIntentionCellController.getInstance().getCellById(uuid).setHighlighted(b);
		}

		@Override
		public int getButtonPosition(String buttonClassname)
		{
			if (buttonClassname.equals(DeleteCanvasButton.class.getName()))
			{
				return 1;
			}
			if (buttonClassname.equals(EnterCanvasButton.class.getName()))
			{
				return 2;
			}
			if (buttonClassname.equals(CreateLinkButton.class.getName()))
			{
				return 3;
			}
			if (buttonClassname.equals(CreateNewCanvasLinkButton.class.getName()))
			{
				return 4;
			}
			if (buttonClassname.equals(CreateCanvasCopyButton.class.getName()))
			{
				return 5;
			}

			return 0;
		}
	}
}
