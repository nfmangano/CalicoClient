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
import calico.plugins.iip.components.piemenu.PieMenuTimerTask;
import calico.plugins.iip.components.piemenu.SetLinkLabelButton;
import calico.plugins.iip.controllers.CCanvasLinkController;
import calico.plugins.iip.controllers.IntentionCanvasController;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CanvasBadgeRow implements StickyItem
{
	private static final DeleteLinkButton deleteLinkButton = new DeleteLinkButton();
	private static final SetLinkLabelButton setLinkLabelButton = new SetLinkLabelButton();

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
		row.refreshHighlights();
	}

	public void updateCanvasCoordinates()
	{
		for (CCanvasLinkBadge badge : badges)
		{
			row.removeChild(badge.getImage());
			badge.updateImage();
			row.addChild(badge.getImage());
		}

		row.refreshHighlights();
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

	public void updateContextHighlight()
	{
		row.updateContextHighlight();
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
		private final Color CLICK_HIGHLIGHT = new Color(0xFFFF30);
		private final Color CONTEXT_HIGHLIGHT = Color.red;

		private PPath clickHighlight = createHighlight(CLICK_HIGHLIGHT);
		private PPath contextHighlight = createHighlight(CONTEXT_HIGHLIGHT);

		private PPath createHighlight(Color c)
		{
			PPath highlight = new PPath(new Rectangle2D.Double(0, 0, CCanvasLinkBadge.BADGE_WIDTH, CCanvasLinkBadge.BADGE_HEIGHT));
			highlight.setStrokePaint(c);
			highlight.setStroke(new BasicStroke(1f));
			highlight.setVisible(false);
			return highlight;
		}

		void refreshHighlights()
		{
			removeChild(clickHighlight);
			removeChild(contextHighlight);
			addChild(contextHighlight);
			addChild(clickHighlight);
		}

		void updateContextHighlight()
		{
			long traversedCanvasId = CCanvasLinkController.getInstance().getTraversedLinkSourceCanvas();
			contextHighlight.setVisible(false);
			for (int i = 0; i < badges.size(); i++)
			{
				CCanvasLinkBadge badge = badges.get(i);
				if (badge.getLinkAnchor().getOpposite().getCanvasId() == traversedCanvasId)
				{
					showHighlight(contextHighlight, i);
				}
			}
		}

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
			PBounds groupBounds = group.getBounds();
			double y = groupBounds.getY() + groupBounds.getHeight();
			setBounds(groupBounds.getX(), y, width, height);
			repaint();
		}

		void highlightClickedBadge(int index)
		{
			showHighlight(clickHighlight, index);
			repaint();
		}

		private void showHighlight(PPath highlight, int index)
		{
			PBounds bounds = getBounds();
			int x = (int) (bounds.getX() + (CCanvasLinkToken.TOKEN_WIDTH * index));
			int y = (int) bounds.getY();
			highlight.setBounds(x, y, CCanvasLinkBadge.BADGE_WIDTH, CCanvasLinkBadge.BADGE_HEIGHT);
			highlight.setVisible(true);
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

	private enum InputState
	{
		IDLE,
		PRESSED,
		PIE
	}

	private class InputHandler extends CalicoAbstractInputHandler
	{
		private final Object stateLock = new Object();

		private CCanvasLinkBadge badge = null;
		private InputState state = InputState.IDLE;

		private final PieMenuTimer pieTimer = new PieMenuTimer();

		@Override
		public void actionReleased(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				if ((state == InputState.PRESSED) && (badge != null))
				{
					CCanvasLinkController.getInstance().traverseLinkToCanvas(badge.getLinkAnchor());
				}
				state = InputState.IDLE;
			}

			row.clickHighlight.setVisible(false);

			CalicoInputManager.unlockHandlerIfMatch(uuid);
		}

		@Override
		public void actionDragged(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				state = InputState.IDLE;
			}
		}

		@Override
		public void actionPressed(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				state = InputState.PRESSED;
			}

			badge = null;
			for (int i = 0; i < badges.size(); i++)
			{
				CCanvasLinkBadge badge = badges.get(i);
				if (badge.getImage().getBounds().contains(event.getGlobalPoint()))
				{
					this.badge = badge;
					row.highlightClickedBadge(i);
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
						row.clickHighlight.setVisible(false);
						state = InputState.PIE;

						CCanvasLinkAnchor anchor = badge.getLinkAnchor();
						deleteLinkButton.setContext(anchor.getLink());
						setLinkLabelButton.setContext(anchor.getLink());
						PieMenu.displayPieMenu(point, deleteLinkButton, setLinkLabelButton);
					}
				}
			}
		}
	}
}
