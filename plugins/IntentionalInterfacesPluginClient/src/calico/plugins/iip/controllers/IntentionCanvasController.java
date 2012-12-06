package calico.plugins.iip.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.components.CCanvas;
import calico.components.bubblemenu.BubbleMenuContainer;
import calico.components.menus.CanvasMenuBar;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.networking.Networking;
import calico.networking.PacketHandler;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.perspectives.CalicoPerspective;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CCanvasLinkAnchor;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.CIntentionType;
import calico.plugins.iip.components.IntentionPanelLayout;
import calico.plugins.iip.components.canvas.CanvasInputProximity;
import calico.plugins.iip.components.canvas.CanvasTagPanel;
import calico.plugins.iip.components.canvas.CanvasTitlePanel;
import calico.plugins.iip.components.canvas.CopyCanvasButton;
import calico.plugins.iip.components.canvas.NewCanvasButton;
import calico.plugins.iip.components.canvas.ShowIntentionGraphButton;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * Coordinates the visual components of this plugin which are installed in the Canvas View.
 * 
 * @author Byron Hawkins
 */
public class IntentionCanvasController implements CalicoPerspective.PerspectiveChangeListener
{
	public static IntentionCanvasController getInstance()
	{
		return INSTANCE;
	}

	public static void initialize()
	{
		INSTANCE = new IntentionCanvasController();

		INSTANCE.initializeComponents();

		CalicoPerspective.addListener(INSTANCE);
	}

	private static IntentionCanvasController INSTANCE;

	// kind of a hack here, would be better to ask the menubar what dimensions it is using
	private static final double MENUBAR_WIDTH = (CalicoOptions.menu.menubar.defaultIconDimension + (CalicoOptions.menu.menubar.iconBuffer * 2));

	/**
	 * Map of the canvas tags which are currently available for user selection, indexed by id.
	 */
	private final Long2ReferenceArrayMap<CIntentionType> activeIntentionTypes = new Long2ReferenceArrayMap<CIntentionType>();

	/**
	 * State field referring to the canvas currently displayed in the Canvas View.
	 */
	private long currentCanvasId = 0L;

	/**
	 * State flag indicating whether the tag panel is currently visible.
	 */
	private boolean tagPanelVisible = false;
	/**
	 * State flag indicating whether the linkpanel is currently visible.
	 */
	private boolean linkPanelVisible = false;

	/**
	 * Layout delegate for the title panel, responsible for keeping the title panel in its designated position when
	 * related dimensions and positions change.
	 */
	private final TitlePanelBounds titlePanelBounds = new TitlePanelBounds();
	/**
	 * Layout delegate for the tag panel, responsible for keeping the tag panel in its designated position when related
	 * dimensions and positions change.
	 */
	private final TagPanelBounds tagPanelBounds = new TagPanelBounds();

	/**
	 * State container for values related to the creation of a new canvas, such as whether the mouse was on the left or
	 * right half of the screen when the new canvas was requested.
	 */
	private CanvasCreationContext canvasCreationContext = null;

	private void initializeComponents()
	{
		CanvasTitlePanel.getInstance().setLayout(titlePanelBounds);
		CanvasTagPanel.getInstance().setLayout(tagPanelBounds);
	}

	@Override
	public void perspectiveChanged(CalicoPerspective perspective)
	{
		if (!(perspective instanceof CanvasPerspective))
		{
			canvasCreationContext = null;
		}
	}

	/**
	 * Copy the contents of <code>sourceCanvasId</code> into <code>targetCanvasId</code>, sending the request directly
	 * to the server.
	 */
	public void copyCanvas(long sourceCanvasId, long targetCanvasId)
	{
		Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_COPY, sourceCanvasId, targetCanvasId));
	}

	/**
	 * Add a new tag to this plugin's internal model
	 */
	public void localAddIntentionType(CIntentionType type)
	{
		activeIntentionTypes.put(type.getId(), type);

		CanvasTagPanel.getInstance().updateIntentionTypes();
	}

	/**
	 * Rename a tag in this plugin's internal model
	 */
	public void localRenameIntentionType(long typeId, String name)
	{
		activeIntentionTypes.get(typeId).setName(name);

		CanvasTagPanel.getInstance().updateIntentionTypes();
	}

	/**
	 * Change the color of a tag in this plugin's internal model
	 */
	public void localSetIntentionTypeColor(long typeId, int colorIndex)
	{
		activeIntentionTypes.get(typeId).setColorIndex(colorIndex);

		if (CanvasPerspective.getInstance().isActive())
		{
			CanvasTagPanel.getInstance().refresh();
		}
	}

	/**
	 * Remove a tag from this plugin's internal model
	 */
	public void localRemoveIntentionType(long typeId)
	{
		activeIntentionTypes.remove(typeId);

		CanvasTagPanel.getInstance().updateIntentionTypes();
	}

	/**
	 * Create a new intention type, sending the command directly to the server without doing anything else.
	 */
	public void addIntentionType(String name)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIT_CREATE);
		packet.putLong(Calico.uuid());
		packet.putString(name);
		packet.putInt(-1); // request a color to be chosen on the server

		packet.rewind();
		Networking.send(packet);
	}

	/**
	 * Rename an intention type, sending the command directly to the server without doing anything else.
	 */
	public void renameIntentionType(long typeId, String name)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIT_RENAME);
		packet.putLong(typeId);
		packet.putString(name);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	/**
	 * Change the color of an intention type, sending the command directly to the server without doing anything else.
	 */
	public void setIntentionTypeColorIndex(long typeId, int colorIndex)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIT_SET_COLOR);
		packet.putLong(typeId);
		packet.putInt(colorIndex);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	/**
	 * Remove an intention type, sending the command directly to the server without doing anything else.
	 */
	public void removeIntentionType(long typeId)
	{
		CalicoPacket packet = new CalicoPacket();
		packet.putInt(IntentionalInterfacesNetworkCommands.CIT_DELETE);
		packet.putLong(typeId);

		packet.rewind();
		PacketHandler.receive(packet);
		Networking.send(packet);
	}

	public void showTagPanel(boolean b)
	{
		if (b != tagPanelVisible)
		{
			toggleTagPanelVisibility();
		}
	}

	public void toggleTagPanelVisibility()
	{
		if (!tagPanelVisible)
		{
			if (CIntentionCellController.getInstance().getCellByCanvasId(currentCanvasId) == null)
			{
				return; // not showing the tag panel if there is no CIC for the current canvas
			}
		}

		tagPanelVisible = !tagPanelVisible;
		CanvasTagPanel.getInstance().setVisible(tagPanelVisible);
	}

	public Collection<CIntentionType> getActiveIntentionTypes()
	{
		return activeIntentionTypes.values();
	}

	public CIntentionType getIntentionType(long typeId)
	{
		return activeIntentionTypes.get(typeId);
	}

	public Color getIntentionTypeColor(long typeId)
	{
		if (typeId < 0L)
		{
			return Color.white;
		}
		else
		{
			return getIntentionType(typeId).getColor();
		}
	}

	/**
	 * Notify this controller that canvas <code>newCanvsaId</code> was created by this client.
	 * 
	 * @param newCanvasId
	 *            identifies the newly created canvas
	 * @param originatingCanvasId
	 *            identifies the canvas that was last displayed in the Canvas View when the new canvas was requested (if
	 *            any).
	 * @param proximity
	 *            specifies which side of the screen the mouse was on at the new canvas request time.
	 */
	public void canvasCreatedLocally(long newCanvasId, long originatingCanvasId, CanvasInputProximity proximity)
	{
		canvasCreationContext = new CanvasCreationContext(newCanvasId, originatingCanvasId, proximity);
	}

	/**
	 * Notify this controller that the Canvas View is now showing <code>canvasId</code>. Updates all visual components
	 * accordingly.
	 */
	public void canvasChanged(long canvasId)
	{
		currentCanvasId = canvasId;
		CanvasTitlePanel.getInstance().moveTo(canvasId);
		CanvasTagPanel.getInstance().moveTo(canvasId);

		CCanvasLinkController.getInstance().showingCanvas(canvasId);

		CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvasId);
		if (cell == null)
		{
			showTagPanel(false);
		}
		else
		{
			showTagPanel(!cell.hasIntentionType());
		}

		if ((canvasCreationContext != null) && (canvasId != canvasCreationContext.newCanvasId))
		{
			canvasCreationContext = null;
		}
	}

	/**
	 * Notify this controller that the most recently created canvas has been tagged, such that if it is also linked,
	 * this controller can discern whether to collapse the chain and make the new canvsa sibling of the canvas it was
	 * created from.
	 */
	public void collapseLikeIntentionTypes()
	{
		if (canvasCreationContext != null)
		{
			long linkOriginCanvasId = canvasCreationContext.originatingCanvasId;
			long parentCanvasId = 0L;
			for (long anchorId : CCanvasLinkController.getInstance().getAnchorIdsByCanvasId(canvasCreationContext.originatingCanvasId))
			{
				CCanvasLinkAnchor anchor = CCanvasLinkController.getInstance().getAnchor(anchorId);
				if (anchor.getLink().getAnchorB() == anchor)
				{
					parentCanvasId = anchor.getOpposite().getCanvasId();
					break;
				}
			}

			if (parentCanvasId > 0L)
			{
				CIntentionCell newCell = CIntentionCellController.getInstance().getCellByCanvasId(canvasCreationContext.newCanvasId);
				CIntentionCell originatingCell = CIntentionCellController.getInstance().getCellByCanvasId(canvasCreationContext.originatingCanvasId);
				if ((newCell != null) && (originatingCell != null) && (originatingCell.getIntentionTypeId() == newCell.getIntentionTypeId()))
				{
					linkOriginCanvasId = parentCanvasId;
				}
			}
			// collapse like tags
			CCanvasLinkController.getInstance().createLink(linkOriginCanvasId, canvasCreationContext.newCanvasId);
		}
	}

	/**
	 * Discern whether teh scrap <code>groupId</code> exists in the currently displayed canvas (if any).
	 */
	private boolean isCurrentlyDisplayed(long groupId)
	{
		if (!CanvasPerspective.getInstance().isActive())
		{
			return false;
		}
		return (CGroupController.groupdb.get(groupId).getCanvasUID() == CCanvasController.getCurrentUUID());
	}

	private class TitlePanelBounds implements IntentionPanelLayout
	{
		private final int X_MARGIN = 20;
		private final int Y_MARGIN = 20;

		@Override
		public void updateBounds(PNode node, double width, double height)
		{
			double x = X_MARGIN + MENUBAR_WIDTH;
			double y = Y_MARGIN;
			node.setBounds(x, y, width, height);
		}
	}

	private class TagPanelBounds implements IntentionPanelLayout
	{
		private final int X_MARGIN = 20;
		private final int Y_MARGIN = 20;

		private final ArrayList<?> peers = new ArrayList<Object>();

		@Override
		public synchronized void updateBounds(PNode node, double width, double height)
		{
			if ((canvasCreationContext != null) && (canvasCreationContext.proximity == CanvasInputProximity.RIGHT))
			{
				positionRight(node, width, height);
			}
			else
			{
				positionLeft(node, width, height);
			}
		}

		private void positionRight(PNode node, double width, double height)
		{
			double x = CalicoDataStore.ScreenWidth
					- (X_MARGIN + (CalicoOptions.menu.menubar.defaultIconDimension + (CalicoOptions.menu.menubar.iconBuffer * 2)) + width);
			double y = Y_MARGIN;

			peers.clear();
			PBounds bounds = new PBounds(x, y, width, height);

			CCanvas canvas = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID());
			if (canvas != null)
			{
				canvas.getCamera().findIntersectingNodes(bounds, peers);
				peers.remove(node);
				for (int i = (peers.size() - 1); i >= 0; i--)
				{
					PNode peer = (PNode) peers.get(i);
					if ((peer instanceof PCamera) || peer.isDescendentOf(node))
					{
						peers.remove(i);
					}
				}

				for (Object peer : peers)
				{
					PBounds peerArea = ((PNode) peer).getBounds();
					if (peerArea.intersects(bounds))
					{
						double unobstructedPosition = peerArea.getX() - (X_MARGIN + width);
						if (unobstructedPosition < x)
						{
							x = unobstructedPosition;
							bounds.setOrigin(x, y);
						}
					}
				}
			}

			node.setBounds(bounds);
		}

		private void positionLeft(PNode node, double width, double height)
		{
			double x = X_MARGIN + CalicoOptions.menu.menubar.defaultIconDimension + (CalicoOptions.menu.menubar.iconBuffer * 2);
			double y = Y_MARGIN;

			peers.clear();
			PBounds bounds = new PBounds(x, y, width, height);

			CCanvas canvas = CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID());
			if (canvas != null)
			{
				canvas.getCamera().findIntersectingNodes(bounds, peers);
				peers.remove(node);
				for (int i = (peers.size() - 1); i >= 0; i--)
				{
					PNode peer = (PNode) peers.get(i);
					if ((peer instanceof PCamera) || (peer instanceof BubbleMenuContainer) || !peer.getVisible())
					{
						peers.remove(i);
					}
					else
					{
						while (peer != null)
						{
							if ((peer instanceof BubbleMenuContainer) || (peer == node) || !peer.getVisible())
							{
								peers.remove(i);
								break;
							}
							peer = peer.getParent();
						}
					}
				}

				for (Object peer : peers)
				{
					PBounds peerArea = ((PNode) peer).getBounds();
					if (peerArea.intersects(bounds))
					{
						double unobstructedPosition = peerArea.getY() + peerArea.getHeight() + Y_MARGIN;
						if (unobstructedPosition > y)
						{
							y = unobstructedPosition;
							bounds.setOrigin(x, y);
						}
					}
				}
			}

			node.setBounds(bounds);
		}
	}

	private class CanvasCreationContext
	{
		final long newCanvasId;
		final long originatingCanvasId;
		final CanvasInputProximity proximity;

		CanvasCreationContext(long newCanvasId, long originatingCanvasId, CanvasInputProximity proximity)
		{
			this.newCanvasId = newCanvasId;
			this.originatingCanvasId = originatingCanvasId;
			this.proximity = proximity;
		}
	}
}
