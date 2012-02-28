package calico.plugins.iip.components.canvas;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;

import calico.Calico;
import calico.components.CGroup;
import calico.components.piemenu.PieMenu;
import calico.controllers.CGroupController;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.plugins.iip.components.CCanvasLink;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.piemenu.DeleteLinkButton;
import calico.plugins.iip.components.piemenu.GoToCanvasButton;
import calico.plugins.iip.components.piemenu.SetLinkLabelButton;
import calico.plugins.iip.util.IntentionalInterfacesGraphics;
import edu.umd.cs.piccolo.nodes.PImage;

public class CCanvasLinkBadge implements StickyItem
{
	public static final double BADGE_WIDTH = 30.0;
	public static final double BADGE_HEIGHT = 30.0;

	private static final DeleteLinkButton deleteLinkButton = new DeleteLinkButton();
	private static final SetLinkLabelButton setLinkLabelButton = new SetLinkLabelButton();
	private static final GoToCanvasButton goToCanvasButton = new GoToCanvasButton();

	private final long uuid;

	private final CCanvasLinkAnchor anchor;
	private final CCanvasLink.LinkDirection direction;

	private PImage image;

	public CCanvasLinkBadge(CCanvasLinkAnchor anchor)
	{
		uuid = Calico.uuid();

		image = new PImage(IntentionalInterfacesGraphics.superimposeCellAddress(anchor.getLink().getLinkType().image, anchor.getOpposite().getCanvasId()));

		this.anchor = anchor;
		if (anchor.getCanvasId() == anchor.getLink().getAnchorA().getCanvasId())
		{
			direction = CCanvasLink.LinkDirection.OUTGOING;
		}
		else
		{
			direction = CCanvasLink.LinkDirection.INCOMING;
		}

		CalicoInputManager.registerStickyItem(this);
		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());
	}

	public long getId()
	{
		return uuid;
	}

	public CCanvasLinkAnchor getLinkAnchor()
	{
		return anchor;
	}

	public CCanvasLink.LinkDirection getDirection()
	{
		return direction;
	}

	public PImage getImage()
	{
		return image;
	}
	
	public void setVisible(boolean b)
	{
		image.setVisible(b);
		image.repaint();
	}

	public void updatePosition()
	{
		CGroup group = CGroupController.groupdb.get(anchor.getGroupId());
		if (group == null)
		{
			System.out.println("Warning: unable to update the position of a link badge because group #" + anchor.getGroupId() + " cannot be found.");
			return;
		}

		image.setBounds(group.getBounds().getCenterX() - (BADGE_WIDTH / 2), group.getBounds().getCenterY() - (BADGE_HEIGHT / 2), BADGE_WIDTH, BADGE_HEIGHT);
		image.repaint();
	}

	public void cleanup()
	{
		CalicoInputManager.removeCustomInputHandler(uuid);
	}

	@Override
	public long getUUID()
	{
		return uuid;
	}

	@Override
	public boolean containsPoint(Point p)
	{
		return image.getBounds().contains(p);
	}

	private class InputHandler extends CalicoAbstractInputHandler
	{
		private final Object stateLock = new Object();

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
