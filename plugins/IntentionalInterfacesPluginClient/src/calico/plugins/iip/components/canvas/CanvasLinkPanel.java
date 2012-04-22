package calico.plugins.iip.components.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import calico.Calico;
import calico.components.CCanvas;
import calico.components.piemenu.PieMenu;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.IntentionPanelLayout;
import calico.plugins.iip.components.piemenu.DeleteLinkButton;
import calico.plugins.iip.components.piemenu.PieMenuTimerTask;
import calico.plugins.iip.components.piemenu.SetLinkLabelButton;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.IntentionCanvasController;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CanvasLinkPanel implements StickyItem
{
	public static CanvasLinkPanel getInstance()
	{
		return INSTANCE;
	}

	private static CanvasLinkPanel INSTANCE = new CanvasLinkPanel();

	public static final double BAY_INSET_X = 100.0;
	public static final double BAY_INSET_Y = 50.0;
	public static final double TOKEN_MARGIN = 3.0;

	final Bay bay = new Bay();

	private final long uuid;
	private long canvas_uuid;
	// remove this
	private final CCanvasLink.LinkDirection direction = CCanvasLink.LinkDirection.INCOMING;

	private boolean visible;
	private IntentionPanelLayout layout;
	private final List<CCanvasLinkToken> tokens = new ArrayList<CCanvasLinkToken>();

	private final DeleteLinkButton deleteLinkButton = new DeleteLinkButton();
	private final SetLinkLabelButton setLinkLabelButton = new SetLinkLabelButton();

	private boolean initialized = false;

	private CanvasLinkPanel()
	{
		uuid = Calico.uuid();
		canvas_uuid = 0L;

		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());

		bay.setPaint(Color.LIGHT_GRAY);
		bay.setVisible(visible = false);

		initialized = true;
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

	public void addLink(CCanvasLink link)
	{

	}

	public void removeLink(CCanvasLink link)
	{

	}

	public void moveLinkAnchor(CCanvasLinkAnchor anchor, long previousCanvasId)
	{
		if (anchor.getCanvasId() == previousCanvasId)
		{
			return;
		}

		/**
		 * update ***
		 * 
		 * <pre>
		if (anchor.hasGroup())
		{
			badgeRowsByGroupId.get(anchor.getGroupId()).updateCanvasCoordinates();
		}
		else
		{
			tokensByAnchorId.get(anchor.getOpposite().getId()).updateCanvasCoordinates();
		}

		// the "design inside" source anchor can't be moved, so assume anchor.getLink().getLinkType() != DESIGN_INSIDE
		removeToken(anchor.getId(), previousCanvasId, anchor.getLink().getAnchorA() == anchor);
		addToken(anchor);
		 */
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

	public void setLayout(IntentionPanelLayout layout)
	{
		this.layout = layout;
	}

	private class Bay extends PComposite
	{
		private final Color CLICK_HIGHLIGHT = new Color(0xFFFF30);
		private final Color CONTEXT_HIGHLIGHT = Color.red;

		private PPath clickHighlight = createHighlight(CLICK_HIGHLIGHT);
		private PPath contextHighlight = createHighlight(CONTEXT_HIGHLIGHT);

		private PPath createHighlight(Color c)
		{
			PPath highlight = new PPath(new Rectangle2D.Double(0, 0, CCanvasLinkToken.TOKEN_WIDTH, CCanvasLinkToken.TOKEN_HEIGHT));
			highlight.setStrokePaint(c);
			highlight.setStroke(new BasicStroke(1f));
			highlight.setVisible(false);
			return highlight;
		}

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

			addChild(contextHighlight);
			addChild(clickHighlight);
		}

		void highlightClickedToken(int index)
		{
			showHighlight(clickHighlight, index);
			repaint();
		}

		private void showHighlight(PPath highlight, int index)
		{
			PBounds bounds = getBounds();
			int x = (int) (bounds.getX() + (CCanvasLinkToken.TOKEN_WIDTH * index) + (TOKEN_MARGIN * (index + 1)));
			int y = (int) (bounds.getY() + TOKEN_MARGIN);
			highlight.setBounds(x, y, CCanvasLinkToken.TOKEN_WIDTH, CCanvasLinkToken.TOKEN_HEIGHT);
			highlight.setVisible(true);
		}

		@Override
		protected void layoutChildren()
		{
			if (!initialized)
			{
				return;
			}

			double x = ((int) bay.getBounds().getX()) + TOKEN_MARGIN;
			double y = ((int) bay.getBounds().getY()) + TOKEN_MARGIN;

			long traversedCanvasId = CCanvasLinkController.getInstance().getTraversedLinkSourceCanvas();
			contextHighlight.setVisible(false);
			synchronized (tokens)
			{
				IntentionCanvasController.getInstance().populateTokens(canvas_uuid, direction, tokens);
				for (int i = 0; i < tokens.size(); i++)
				{
					CCanvasLinkToken token = tokens.get(i);

					token.setBounds(x, y, CCanvasLinkToken.TOKEN_WIDTH, CCanvasLinkToken.TOKEN_HEIGHT);
					x += (CCanvasLinkToken.TOKEN_WIDTH + TOKEN_MARGIN);

					if (token.getLinkAnchor().getOpposite().getCanvasId() == traversedCanvasId)
					{
						showHighlight(contextHighlight, i);
					}
				}
			}
		}
	}

	private enum InputState
	{
		IDLE,
		PRESSED,
		PIE
	}

	private class InputHandler extends CalicoAbstractInputHandler
	{
		private final Object stateLock = new Object();

		private InputState state = InputState.IDLE;
		private CCanvasLinkToken clickedToken = null;

		private final PieMenuTimer pieTimer = new PieMenuTimer();

		@Override
		public void actionReleased(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				if ((state == InputState.PRESSED) && (clickedToken != null))
				{
					CCanvasLinkController.getInstance().traverseLinkToCanvas(clickedToken.getLinkAnchor());
				}
				state = InputState.IDLE;
			}

			bay.clickHighlight.setVisible(false);
			clickedToken = null;

			CalicoInputManager.unlockHandlerIfMatch(uuid);
		}

		@Override
		public void actionDragged(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				state = InputState.IDLE;
			}

			clickedToken = null;
		}

		@Override
		public void actionPressed(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				state = InputState.PRESSED;
			}

			clickedToken = null;
			synchronized (tokens)
			{
				IntentionCanvasController.getInstance().populateTokens(canvas_uuid, direction, tokens);
				for (int i = 0; i < tokens.size(); i++)
				{
					CCanvasLinkToken token = tokens.get(i);

					if (token.getGlobalBounds().contains(event.getPoint()))
					{
						System.out.println("Clicked on a " + token.getLinkAnchor().getArrowEndpointType() + " to canvas #"
								+ token.getLinkAnchor().getOpposite().getCanvasId());
						clickedToken = token;
						bay.highlightClickedToken(i);
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
					schedule(new Task(), 200L);
				}
			}

			private class Task extends PieMenuTimerTask
			{
				@Override
				public void run()
				{
					synchronized (stateLock)
					{
						if (state == InputState.PRESSED)
						{
							startAnimation(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer(CCanvas.Layer.TOOLS), point);
						}
					}
				}

				@Override
				protected void animationCompleted()
				{
					if (state == InputState.PRESSED)
					{
						bay.clickHighlight.setVisible(false);
						state = InputState.PIE;

						deleteLinkButton.setContext(clickedToken.getLinkAnchor().getLink());
						setLinkLabelButton.setContext(clickedToken.getLinkAnchor().getLink());
						PieMenu.displayPieMenu(point, deleteLinkButton, setLinkLabelButton);
					}
				}
			}
		}
	}
}
