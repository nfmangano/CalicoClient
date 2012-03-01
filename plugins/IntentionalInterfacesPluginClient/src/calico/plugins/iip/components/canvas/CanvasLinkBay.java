package calico.plugins.iip.components.canvas;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import calico.Calico;
import calico.components.piemenu.PieMenu;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.piemenu.DeleteLinkButton;
import calico.plugins.iip.components.piemenu.GoToCanvasButton;
import calico.plugins.iip.components.piemenu.SetLinkLabelButton;
import calico.plugins.iip.controllers.IntentionCanvasController;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CanvasLinkBay implements StickyItem
{
	public interface Layout
	{
		void updateBounds(PNode node, double width, double height);
	}

	public static final double BAY_INSET_X = 100.0;
	public static final double BAY_INSET_Y = 50.0;
	public static final double TOKEN_MARGIN = 3.0;

	final Bay bay = new Bay();

	private final long uuid;
	private long canvas_uuid;
	private final CCanvasLink.LinkDirection direction;

	private boolean visible;
	private Layout layout;
	private final List<CCanvasLinkToken> tokens = new ArrayList<CCanvasLinkToken>();

	private final DeleteLinkButton deleteLinkButton = new DeleteLinkButton();
	private final SetLinkLabelButton setLinkLabelButton = new SetLinkLabelButton();
	private final GoToCanvasButton goToCanvasButton = new GoToCanvasButton();

	public CanvasLinkBay(long canvas_uuid, CCanvasLink.LinkDirection direction, Layout layout)
	{
		uuid = Calico.uuid();
		this.direction = direction;
		this.canvas_uuid = canvas_uuid;

		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());

		this.layout = layout;

		bay.setPaint(Color.LIGHT_GRAY);
		bay.setVisible(visible = false);
	}

	@Override
	public long getUUID()
	{
		return uuid;
	}

	@Override
	public boolean containsPoint(Point p)
	{
		return bay.getBounds().contains(p);
	}

	public boolean isVisible()
	{
		return bay.getVisible();
	}

	public void setVisible(boolean b)
	{
		if (visible == b)
		{
			return;
		}

		visible = b;

		if (b)
		{
			refreshLayout();
		}
		else
		{
			bay.setVisible(false);
		}

		if (b)
		{
			CalicoInputManager.registerStickyItem(this);
			bay.repaint();
		}
		else
		{
			CalicoInputManager.unregisterStickyItem(this);
		}
	}

	public void moveTo(long canvas_uuid)
	{
		this.canvas_uuid = canvas_uuid;

		if (bay.getParent() != null)
		{
			bay.getParent().removeChild(bay);
		}
		refreshLayout();
		CCanvasController.canvasdb.get(canvas_uuid).getCamera().addChild(bay);
	}

	public void refreshLayout()
	{
		if (!visible)
		{
			return;
		}

		int tokenCount = IntentionCanvasController.getInstance().getTokenCount(canvas_uuid, direction);
		if (tokenCount == 0)
		{
			bay.setBounds(0, 0, 0, 0);
			bay.setVisible(false);
			return;
		}

		bay.setVisible(true);

		double width = (tokenCount * (CCanvasLinkToken.TOKEN_WIDTH + TOKEN_MARGIN)) + TOKEN_MARGIN;
		double height = CCanvasLinkToken.TOKEN_HEIGHT + (TOKEN_MARGIN * 2);
		layout.updateBounds(bay, width, height);
		bay.repaint();

		bay.updateTokens();
	}

	private class Bay extends PComposite
	{
		void updateTokens()
		{
			removeAllChildren();
			synchronized (tokens)
			{
				IntentionCanvasController.getInstance().populateTokens(canvas_uuid, direction, tokens);
				for (CCanvasLinkToken token : tokens)
				{
					addChild(token);
				}
			}
		}

		@Override
		protected void layoutChildren()
		{
			double x = ((int) bay.getBounds().getX()) + TOKEN_MARGIN;
			double y = ((int) bay.getBounds().getY()) + TOKEN_MARGIN;

			synchronized (tokens)
			{
				IntentionCanvasController.getInstance().populateTokens(canvas_uuid, direction, tokens);
				for (CCanvasLinkToken token : tokens)
				{
					token.setBounds(x, y, CCanvasLinkToken.TOKEN_WIDTH, CCanvasLinkToken.TOKEN_HEIGHT);
					x += (CCanvasLinkToken.TOKEN_WIDTH + TOKEN_MARGIN);
				}
			}
		}
	}

	private class InputHandler extends CalicoAbstractInputHandler
	{
		private final Object stateLock = new Object();

		private boolean pieMenuPending = false;
		private CCanvasLinkToken clickedToken = null;

		private final PieMenuTimer pieTimer = new PieMenuTimer();

		@Override
		public void actionReleased(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				pieMenuPending = false;
			}

			clickedToken = null;

			CalicoInputManager.unlockHandlerIfMatch(uuid);
		}

		@Override
		public void actionDragged(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				pieMenuPending = false;
			}

			clickedToken = null;
		}

		@Override
		public void actionPressed(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				pieMenuPending = false;
			}

			clickedToken = null;
			synchronized (tokens)
			{
				IntentionCanvasController.getInstance().populateTokens(canvas_uuid, direction, tokens);
				for (CCanvasLinkToken token : tokens)
				{
					if (token.getGlobalBounds().contains(event.getPoint()))
					{
						System.out.println("Clicked on a " + token.getLinkAnchor().getArrowEndpointType() + " to canvas #"
								+ token.getLinkAnchor().getOpposite().getCanvasId());
						clickedToken = token;
						break;
					}
				}
			}

			if (clickedToken != null)
			{
				pieTimer.start(event.getGlobalPoint());
			}
		}

		private class PieMenuTimer extends Timer
		{
			private Point point;

			void start(Point point)
			{
				this.point = point;

				synchronized (stateLock)
				{
					pieMenuPending = true;
					schedule(new Task(), 200L);
				}
			}

			private class Task extends TimerTask
			{
				@Override
				public void run()
				{
					synchronized (stateLock)
					{
						if (pieMenuPending)
						{
							pieMenuPending = false;
							deleteLinkButton.setContext(clickedToken.getLinkAnchor().getLink());
							setLinkLabelButton.setContext(clickedToken.getLinkAnchor().getLink());
							goToCanvasButton.setContext(clickedToken.getLinkAnchor().getOpposite().getCanvasId());
							PieMenu.displayPieMenu(point, deleteLinkButton, setLinkLabelButton, goToCanvasButton);
						}
					}
				}
			}
		}
	}
}
