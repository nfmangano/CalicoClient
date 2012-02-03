package calico.plugins.iip.components.canvas;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import calico.Calico;
import calico.components.CCanvas;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.CCanvasLinkBadge;
import calico.plugins.iip.controllers.CanvasPerspectiveController;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CanvasLinkBay implements StickyItem
{
	public interface Layout
	{
		void updateBounds(Rectangle2D bounds, double width, double height);
	}
	
	public static final double BAY_INSET_X = 100.0;
	public static final double BAY_INSET_Y = 50.0;
	public static final double BADGE_MARGIN = 3.0;

	final Bay bay = new Bay();

	private final long uuid;
	private final long canvas_uuid;
	private final CCanvasLinkBadge.Type type;

	private final Rectangle2D bounds = new Rectangle2D.Double();
	private Layout layout;
	private final List<CCanvasLinkBadge> badges = new ArrayList<CCanvasLinkBadge>();

	public CanvasLinkBay(long canvas_uuid, CCanvasLinkBadge.Type type, Layout layout)
	{
		uuid = Calico.uuid();
		this.type = type;
		this.canvas_uuid = canvas_uuid;

		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());
		CalicoInputManager.registerStickyItem(this);

		this.layout = layout;

		bay.setPaint(Color.LIGHT_GRAY);
		bay.setVisible(false);
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
		if (b)
		{
			refreshLayout();
		}
		bay.setVisible(b);
		if (b)
		{
			bay.repaint();
		}
	}

	public void install(CCanvas canvas)
	{
		canvas.getCamera().addChild(bay);
	}

	public void refreshLayout()
	{
		int badgeCount = CanvasPerspectiveController.getInstance().getBadgeCount(canvas_uuid, type);
		if (badgeCount == 0)
		{
			bay.setBounds(0, 0, 0, 0);
			bay.setVisible(false);
			return;
		}

		double width = (badgeCount * (CCanvasLinkBadge.BADGE_WIDTH + BADGE_MARGIN)) + BADGE_MARGIN;
		double height = CCanvasLinkBadge.BADGE_HEIGHT + (BADGE_MARGIN * 2);
		layout.updateBounds(bounds, width, height);
		bay.setBounds(bounds);
		
		bay.updateBadges();
	}
	
	private class Bay extends PComposite
	{
		void updateBadges()
		{
			removeAllChildren();
			synchronized (badges)
			{
				CanvasPerspectiveController.getInstance().populateBadges(canvas_uuid, type, badges);
				for (CCanvasLinkBadge badge : badges)
				{
					addChild(badge);
				}
			}
		}
		
		@Override
		protected void layoutChildren()
		{
			double x = ((int) bounds.getX()) + BADGE_MARGIN;
			double y = ((int) bounds.getY()) + BADGE_MARGIN;

			synchronized (badges)
			{
				CanvasPerspectiveController.getInstance().populateBadges(canvas_uuid, type, badges);
				for (CCanvasLinkBadge badge : badges)
				{
					badge.setBounds(x, y, CCanvasLinkBadge.BADGE_WIDTH, CCanvasLinkBadge.BADGE_HEIGHT);
				}
				x += (CCanvasLinkBadge.BADGE_WIDTH + BADGE_MARGIN);
			}
		}
	}

	private class InputHandler extends CalicoAbstractInputHandler
	{
		@Override
		public void actionReleased(InputEventInfo event)
		{
			synchronized (badges)
			{
				CanvasPerspectiveController.getInstance().populateBadges(canvas_uuid, type, badges);
				for (CCanvasLinkBadge badge : badges)
				{
					if (badge.getGlobalBounds().contains(event.getPoint()))
					{
						System.out.println("Clicked on a " + badge.getLink().getType() + " to canvas #" + badge.getLink().getCanvasId());
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
