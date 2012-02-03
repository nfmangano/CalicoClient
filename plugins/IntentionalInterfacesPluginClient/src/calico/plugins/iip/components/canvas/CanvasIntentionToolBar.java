package calico.plugins.iip.components.canvas;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.menus.CanvasMenuButton;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.menus.buttons.NewAlternativeButton;
import calico.plugins.iip.components.menus.buttons.NewIdeaButton;
import calico.plugins.iip.components.menus.buttons.NewPerspectiveButton;
import calico.plugins.iip.components.menus.buttons.ToggleLinkBaysButton;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CanvasIntentionToolBar implements StickyItem
{
	private static final int ITEM_WIDTH = 30;
	private static final int ITEM_HEIGHT = 30;
	private static final int ITEM_BORDER = 5;
	private static final int CANVAS_INSET_X = 100;
	private static final int CANVAS_INSET_Y = 50;

	private enum Button
	{
		TOGGLE_LINK_BAYS(new ToggleLinkBaysButton()),
		NEW_IDEA(new NewIdeaButton()),
		NEW_ALTERNATIVE(new NewAlternativeButton()),
		NEW_PERSPECTIVE(new NewPerspectiveButton());

		private CanvasIntentionToolBarItem item;

		private Button(CanvasIntentionToolBarItem item)
		{
			this.item = item;
		}
	}

	final ToolBar toolbar = new ToolBar();

	private final long uuid;
	private final long canvas_uuid;

	private int x;
	private int y;

	private final List<CanvasMenuButton> buttons = new ArrayList<CanvasMenuButton>();

	public CanvasIntentionToolBar(long canvas_uuid)
	{
		uuid = Calico.uuid();
		this.canvas_uuid = canvas_uuid;

		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());
		CalicoInputManager.registerStickyItem(this);

		toolbar.setPaint(Color.GRAY);

		for (Button button : Button.values())
		{
			button.item.reset();
			addButton(button.item);
		}
		
		toolbar.setVisible(false);
	}

	@Override
	public long getUUID()
	{
		return uuid;
	}
	
	public long getCanvasId()
	{
		return canvas_uuid;
	}

	@Override
	public boolean containsPoint(Point p)
	{
		return toolbar.getGlobalBounds().contains(p);
	}

	public void addButton(CanvasIntentionToolBarItem button)
	{
		buttons.add(button);
		toolbar.addChild(button);
		button.setCanvasId(canvas_uuid);

		int width = (buttons.size() * (ITEM_WIDTH + ITEM_BORDER)) + ITEM_BORDER;
		int height = ITEM_HEIGHT + (2 * ITEM_BORDER);

		x = CalicoDataStore.ScreenWidth - (CANVAS_INSET_X + width);
		y = CANVAS_INSET_Y;

		toolbar.setBounds(x, y, width, height);
	}

	class ToolBar extends PComposite
	{
		@Override
		protected void layoutChildren()
		{
			int xButton = x + ITEM_BORDER;
			int yButton = y + ITEM_BORDER;
			for (CanvasMenuButton button : buttons)
			{
				button.setBounds(xButton, yButton, ITEM_WIDTH, ITEM_HEIGHT);
				xButton += (ITEM_WIDTH + ITEM_BORDER);
			}
		}
	}

	private class InputHandler extends CalicoAbstractInputHandler
	{
		@Override
		public void actionReleased(InputEventInfo event)
		{
			for (CanvasMenuButton button : buttons)
			{
				if (button.getGlobalBounds().contains(event.getPoint()))
				{
					button.actionMouseClicked();
					break;
				}
			}
			
			CalicoInputManager.unlockHandlerIfMatch(uuid);
		}
		
		@Override
		public void actionDragged(InputEventInfo ev)
		{
		}
		
		@Override
		public void actionPressed(InputEventInfo ev)
		{
		}
	}
}
