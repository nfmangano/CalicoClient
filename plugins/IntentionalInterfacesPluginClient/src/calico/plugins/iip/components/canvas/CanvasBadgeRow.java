package calico.plugins.iip.components.canvas;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import calico.Calico;
import calico.components.CCanvas;
import calico.components.CGroup;
import calico.components.piemenu.PieMenu;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.piemenu.DeleteLinkButton;
import calico.plugins.iip.components.piemenu.GoToCanvasButton;
import calico.plugins.iip.components.piemenu.SetLinkLabelButton;
import calico.plugins.iip.controllers.IntentionCanvasController;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CanvasBadgeRow implements StickyItem
{
	private static final DeleteLinkButton deleteLinkButton = new DeleteLinkButton();
	private static final SetLinkLabelButton setLinkLabelButton = new SetLinkLabelButton();
	private static final GoToCanvasButton goToCanvasButton = new GoToCanvasButton();

	private final long uuid;
	private final long groupId;
	private final Row row;
	private final List<CCanvasLinkBadge> badges;

	public CanvasBadgeRow(long groupId)
	{
		uuid = Calico.uuid();
		this.groupId = groupId;
		row = new Row();
		badges = new ArrayList<CCanvasLinkBadge>();

		CGroup group = CGroupController.groupdb.get(groupId);
		CCanvasController.canvasdb.get(group.getCanvasUID()).getLayer(CCanvas.Layer.TOOLS).addChild(row);

		CalicoInputManager.registerStickyItem(this);
		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());

		row.refreshDisplay();
	}

	public void addBadge(CCanvasLinkBadge badge)
	{
		badges.add(badge);
		row.addChild(badge.getImage());
		row.refreshDisplay();
	}

	public void updateBadgeCoordinates()
	{
		for (CCanvasLinkBadge badge : badges)
		{
			row.removeChild(badge.getImage());
			badge.updateImage();
			row.addChild(badge.getImage());
		}
	}

	public void removeBadge(CCanvasLinkBadge badge)
	{
		badges.remove(badge);

		if (badges.isEmpty())
		{
			uninstallFromCanvas();
		}
		else
		{
			row.removeChild(badge.getImage());
			row.refreshDisplay();
		}
	}

	public void setVisible(boolean b)
	{
		row.setVisible(b);
		row.repaint();
	}

	private void uninstallFromCanvas()
	{
		CGroup group = CGroupController.groupdb.get(groupId);
		CCanvasController.canvasdb.get(group.getCanvasUID()).getLayer(CCanvas.Layer.TOOLS).removeChild(row);
		CalicoInputManager.unregisterStickyItem(this);
		CalicoInputManager.removeCustomInputHandler(uuid);
		IntentionCanvasController.getInstance().removeBadgeRow(groupId);
	}

	public void refreshDisplay()
	{
		row.refreshDisplay();
	}

	@Override
	public long getUUID()
	{
		return uuid;
	}

	@Override
	public boolean containsPoint(Point p)
	{
		return row.getVisible() && row.getBounds().contains(p);
	}

	private class Row extends PComposite
	{
		void refreshDisplay()
		{
			CGroup group = CGroupController.groupdb.get(groupId);
			if (group == null)
			{
				System.out.println("Warning: unable to update the position of a badge row because group #" + groupId + " cannot be found.");
				return;
			}

			double width = badges.size() * CCanvasLinkBadge.BADGE_WIDTH;
			double height = CCanvasLinkBadge.BADGE_HEIGHT;
			double x = group.getBounds().getCenterX() - (width / 2.0);
			double y = group.getBounds().getCenterY() - (height / 2.0);

			setBounds(x, y, width, height);
			repaint();
		}

		@Override
		protected void layoutChildren()
		{
			PBounds bounds = getBounds();
			double x = bounds.getX();
			for (CCanvasLinkBadge badge : badges)
			{
				badge.getImage().setBounds(x, bounds.getY(), CCanvasLinkBadge.BADGE_WIDTH, CCanvasLinkBadge.BADGE_HEIGHT);
				x += CCanvasLinkBadge.BADGE_WIDTH;
			}
		}
	}

	private class InputHandler extends CalicoAbstractInputHandler
	{
		private final Object stateLock = new Object();

		private CCanvasLinkBadge badge = null;
		private boolean pieMenuPending = false;

		private final PieMenuTimer pieTimer = new PieMenuTimer();

		@Override
		public void actionReleased(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				pieMenuPending = false;
			}

			CalicoInputManager.unlockHandlerIfMatch(uuid);
		}

		@Override
		public void actionDragged(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				pieMenuPending = false;
			}
		}

		@Override
		public void actionPressed(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				pieMenuPending = false;
			}

			badge = null;
			for (CCanvasLinkBadge badge : badges)
			{
				if (badge.getImage().getBounds().contains(event.getGlobalPoint()))
				{
					this.badge = badge;
					break;
				}
			}
			pieTimer.start(event.getGlobalPoint());
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

							CCanvasLinkAnchor anchor = badge.getLinkAnchor();
							deleteLinkButton.setContext(anchor.getLink());
							setLinkLabelButton.setContext(anchor.getLink());
							goToCanvasButton.setContext(anchor.getOpposite().getCanvasId());
							PieMenu.displayPieMenu(point, deleteLinkButton, setLinkLabelButton, goToCanvasButton);
						}
					}
				}
			}
		}
	}
}
