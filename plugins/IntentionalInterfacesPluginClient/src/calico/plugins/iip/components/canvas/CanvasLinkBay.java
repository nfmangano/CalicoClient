package calico.plugins.iip.components.canvas;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import calico.Calico;
import calico.controllers.CCanvasController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.controllers.IntentionCanvasController;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CanvasLinkBay implements StickyItem
{
	public interface Layout
	{
		void updateBounds(Rectangle2D bounds, double width, double height);
	}
	
	public static final double BAY_INSET_X = 100.0;
	public static final double BAY_INSET_Y = 50.0;
	public static final double TOKEN_MARGIN = 3.0;

	final Bay bay = new Bay();

	private final long uuid;
	private long canvas_uuid;
	private final CCanvasLink.LinkDirection direction;

	private boolean visible;
	private final Rectangle2D bounds = new Rectangle2D.Double();
	private Layout layout;
	private final List<CCanvasLinkToken> tokens = new ArrayList<CCanvasLinkToken>();

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
		return bounds.contains(p);
	}

	public boolean isVisible()
	{
		return bay.getVisible();
	}

	public void setVisible(boolean b)
	{
		visible = b;
		
		if (b)
		{
			refreshLayout();
		}

		bay.setVisible(b);

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
		layout.updateBounds(bounds, width, height);
		bay.setBounds(bounds);
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
			double x = ((int) bounds.getX()) + TOKEN_MARGIN;
			double y = ((int) bounds.getY()) + TOKEN_MARGIN;

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
		@Override
		public void actionReleased(InputEventInfo event)
		{
			synchronized (tokens)
			{
				IntentionCanvasController.getInstance().populateTokens(canvas_uuid, direction, tokens);
				for (CCanvasLinkToken token : tokens)
				{
					if (token.getGlobalBounds().contains(event.getPoint()))
					{
						System.out.println("Clicked on a " + token.getLink().getType() + " to canvas #" + token.getLink().getCanvasId());
						break;
					}
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
