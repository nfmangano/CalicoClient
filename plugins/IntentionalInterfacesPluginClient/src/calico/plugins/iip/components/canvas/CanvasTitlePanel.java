package calico.plugins.iip.components.canvas;

import java.awt.Color;
import java.awt.Point;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoDraw;
import calico.components.CCanvas;
import calico.controllers.CCanvasController;
import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.inputhandlers.CalicoAbstractInputHandler;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.inputhandlers.StickyItem;
import calico.networking.netstuff.CalicoPacket;
import calico.perspectives.CalicoPerspective;
import calico.perspectives.CalicoPerspective.PerspectiveChangeListener;
import calico.perspectives.CanvasPerspective;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.IntentionPanelLayout;
import calico.plugins.iip.components.canvas.CanvasTitleDialog.Action;
import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.controllers.CIntentionCellController;
import calico.plugins.iip.controllers.IntentionCanvasController;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * Simple panel containing the canvas title, which is attached to the upper left corner of the Canvas View. Tapping the
 * panel pops up the <code>CanvasTitleDialog</code>, and this panel acts as the controller for that dialog. Title
 * changes are applied via <code>CIntentionCellController</code>. There is only one instance of this panel, and it is
 * moved from canvas to canvas as the user navigates. When the title of the current canvas changes, this panel expects a
 * call to <code>refresh()</code> so it can update the display.
 * 
 * @author Byron Hawkins
 */
public class CanvasTitlePanel implements StickyItem, CalicoEventListener, PerspectiveChangeListener
{
	public static CanvasTitlePanel getInstance()
	{
		return INSTANCE;
	}

	private static CanvasTitlePanel INSTANCE = new CanvasTitlePanel();

	public static final double PANEL_COMPONENT_INSET = 5.0;

	public static final double ROW_HEIGHT = 30.0;
	public static final double ROW_TEXT_INSET = 1.0;

	private final CanvasTitleNodeContainer titleNodeContainer;

	private final long uuid;
	private long canvas_uuid;

	private IntentionPanelLayout layout;

	private boolean initialized = false;
	
	private CanvasTitlePanel()
	{
		uuid = Calico.uuid();
		this.canvas_uuid = 0L;

		CalicoInputManager.addCustomInputHandler(uuid, new InputHandler());

		titleNodeContainer = new CanvasTitleNodeContainer(0l, CanvasTitleNodeType.TITLE);

		titleNodeContainer.setPaint(Color.white);
		CalicoInputManager.registerStickyItem(this);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CLINK_CREATE, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CLINK_MOVE_ANCHOR, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.II_PERSPECTIVE_ACTIVATED, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CIC_TAG, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoEventHandler.getInstance().addListener(IntentionalInterfacesNetworkCommands.CIC_UNTAG, this, CalicoEventHandler.PASSIVE_LISTENER);
		CalicoPerspective.addListener(this);
		
		
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
		if (titleNodeContainer.getBounds().contains(p))
			return true;
		
		for (CanvasTitleNodeContainer ctnc : titles)
		{
			if (ctnc.getBounds().contains(p))
				return true;
		}
		
		return false;
//		return panel.getBounds().contains(p);
	}

	public void moveTo(long canvas_uuid)
	{
		this.canvas_uuid = canvas_uuid;

		if (titleNodeContainer.getParent() != null)
		{
			titleNodeContainer.getParent().removeChild(titleNodeContainer);
		}
		refresh();
		rebuildTitleNodes();
//		CCanvasController.canvasdb.get(canvas_uuid).getCamera().addChild(panel);
		CCanvasController.canvasdb.get(canvas_uuid).getCamera().addChild(titleNodeContainer);
	}

	public void refresh()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable() {
				public void run()
				{
					refresh();
					rebuildTitleNodes();
				}
			});
			return;
		}

//		panel.refresh();
		rebuildTitleNodes();
		updatePanelBounds();
//		CalicoDraw.setVisible(panel, true);
//		panel.setVisible(true);
//		CalicoDraw.repaint(panel);
//		panel.repaint();
	}

	private void updatePanelBounds()
	{
//		double width = panel.calculateWidth();
//		double height = panel.calculateHeight();
//		layout.updateBounds(panel, width, height);
		layout.updateBounds(titleNodeContainer, (int)titleNodeContainer.calculateWidth(), (int)titleNodeContainer.calculateHeight());
		

//		CalicoDraw.repaint(panel);
		CalicoDraw.repaint(titleNodeContainer);
//		panel.repaint();
	}

	public void setLayout(IntentionPanelLayout layout)
	{
		this.layout = layout;
	}

	/**
	 * Represents the title panel in the Piccolo component hierarchy.
	 * 
	 * @author Byron Hawkins
	 */
	private class PanelNode extends PComposite
	{
		private final PText text = new PText();

		public PanelNode()
		{
			text.setConstrainWidthToTextWidth(true);
			text.setConstrainHeightToTextHeight(true);
			text.setFont(text.getFont().deriveFont(20f));

			CalicoDraw.addChildToNode(this, text);
//			addChild(text);
		}

		void tap(Point point)
		{
			CanvasTitleDialog.Action action = CanvasTitleDialog.getInstance().queryUserForLabel(
					CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid));

			if (action == Action.OK)
			{
				CIntentionCellController.getInstance().setCellTitle(CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid).getId(),
						CanvasTitleDialog.getInstance().getText(), false);
			}
		}

		double calculateWidth()
		{
			return text.getBounds().width + (2 * PANEL_COMPONENT_INSET);
		}

		double calculateHeight()
		{
			return text.getBounds().height;
		}

		void refresh()
		{
			if (canvas_uuid == 0L)
			{
				return;
			}
			
			CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(canvas_uuid);
			
			String tag = "";
			if (cell.getIntentionTypeId() != -1)
				tag = " (" + IntentionCanvasController.getInstance().getIntentionType(cell.getIntentionTypeId()).getName() + ")";
			
			String titlePrefix = "";
			if (! CIntentionCellController.getInstance().isRootCanvas(canvas_uuid))
				titlePrefix = cell.getSiblingIndex() + ". ";
			
			String title = titlePrefix + cell.getTitle() + tag;
			
			long parentUUID = CIntentionCellController.getInstance().getCIntentionCellParent(canvas_uuid);
			
			while (parentUUID != 0l)
			{
				cell = CIntentionCellController.getInstance().getCellByCanvasId(parentUUID);
				tag = "";
				titlePrefix = "";
				if (! CIntentionCellController.getInstance().isRootCanvas(parentUUID))
					titlePrefix = cell.getSiblingIndex() + ". ";
				
				if (cell.getIntentionTypeId() != -1)
					tag = " (" + IntentionCanvasController.getInstance().getIntentionType(cell.getIntentionTypeId()).getName() +")";
				title = titlePrefix + cell.getTitle() + tag + " > " + title;
				
				parentUUID = CIntentionCellController.getInstance().getCIntentionCellParent(parentUUID);
			}

			text.setText("-----" + title);
		}

		@Override
		protected void layoutChildren()
		{
			if (!initialized)
			{
				return;
			}

			PBounds bounds = getBounds();

			text.recomputeLayout();
			PBounds textBounds = text.getBounds();
			text.setBounds(bounds.x + PANEL_COMPONENT_INSET, bounds.y + ROW_TEXT_INSET, textBounds.width, textBounds.getHeight());
		}
	}

	/**
	 * Only tap input is recognized, so it is only necessary to track the pressed state.
	 * 
	 * @author Byron Hawkins
	 */
	private enum InputState
	{
		IDLE,
		PRESSED
	}

	/**
	 * Recognizes a press as a tap if it is not held longer than the <code>tapDuration</code> and no drag extends beyond
	 * the <code>dragThreshold</code>. The <code>state</code> is voluntarily read/write locked under
	 * <code>stateLock</code>.
	 * 
	 * @author Byron Hawkins
	 */
	private class InputHandler extends CalicoAbstractInputHandler
	{
		private final Object stateLock = new Object();

		private final long tapDuration = 500L;
		private final double dragThreshold = 10.0;

		private InputState state = InputState.IDLE;
		private long pressTime = 0L;
		private Point pressAnchor;

		@Override
		public void actionReleased(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				if ((state == InputState.PRESSED) && ((System.currentTimeMillis() - pressTime) < tapDuration))
				{
//					panel.tap(event.getPoint());
					Point p = event.getPoint();
					if (titleNodeContainer.getBounds().contains(p))
					{
						titleNodeContainer.tap(p);
					}
					
					for (int i = 0; i < titles.size(); i++)
					{
						CanvasTitleNodeContainer ctnc = titles.get(i);
						if (ctnc.getBounds().contains(p))
							ctnc.tap(p);
					}
				}
				else if ((state == InputState.PRESSED) && ((System.currentTimeMillis() - pressTime) >= tapDuration))
				{
					Point p = event.getPoint();
					if (titleNodeContainer.getBounds().contains(p))
					{
						long targetCanvas = titleNodeContainer.getCanvasAt(p);
						if (targetCanvas != 0l)
							CCanvasController.loadCanvas(targetCanvas);
					}
					
					for (int i = 0; i < titles.size(); i++)
					{
						CanvasTitleNodeContainer ctnc = titles.get(i);
						if (ctnc.getBounds().contains(p))
						{
							long targetCanvas = ctnc.getCanvasAt(p);
							if (targetCanvas != 0l)
								CCanvasController.loadCanvas(targetCanvas);
						}
					}
				}
				state = InputState.IDLE;
			}

			pressTime = 0L;

			CalicoInputManager.unlockHandlerIfMatch(uuid);
		}

		@Override
		public void actionDragged(InputEventInfo event)
		{
			if (pressAnchor.distance(event.getGlobalPoint()) < dragThreshold)
			{
				// not a drag, completely ignore this event
				return;
			}

			synchronized (stateLock)
			{
				if (state == InputState.PRESSED)
				{
					state = InputState.IDLE;
					pressTime = 0L;
				}
			}
		}

		@Override
		public void actionPressed(InputEventInfo event)
		{
			synchronized (stateLock)
			{
				state = InputState.PRESSED;

				pressTime = System.currentTimeMillis();
				pressAnchor = event.getGlobalPoint();
			}
		}
	}
	
	@Override
	public void handleCalicoEvent(int event, CalicoPacket p) {
		
		if (event == IntentionalInterfacesNetworkCommands.CLINK_CREATE
				|| event == IntentionalInterfacesNetworkCommands.CLINK_MOVE_ANCHOR
				|| event == IntentionalInterfacesNetworkCommands.CIC_TAG
				|| event == IntentionalInterfacesNetworkCommands.CIC_UNTAG)
		{
			refresh();
		}
		
	}

	@Override
	public void perspectiveChanged(CalicoPerspective perspective) {
		if (perspective instanceof CanvasPerspective)
		{
			refresh();
//			rebuildTitleNodes();
			CalicoInputManager.registerStickyItem(this);
		}
		else
		{
			CalicoInputManager.unregisterStickyItem(this);
		}
		
		
	}
	
	private ArrayList<CanvasTitleNodeContainer> titles = new ArrayList<CanvasTitleNodeContainer>();
	
	private void rebuildTitleNodes()
	{
		if (canvas_uuid == 0l)
			return;
		
		//remove old nodes from canvas
		titleNodeContainer.removeAllChildren();
		if (titles != null)
		{
			clearDisplayedStack(null);
		}
		
		//iterate and build array of title nodes

		
		ArrayList<PText> titleNodes = new ArrayList<PText>();
		CanvasTitleNode ctNode = new CanvasTitleNode(canvas_uuid, CanvasTitleNodeType.TITLE);
		titleNodes.add(0, ctNode);
		
		long parentUUID = CIntentionCellController.getInstance().getCIntentionCellParent(canvas_uuid);
		while (parentUUID != 0l)
		{
			titleNodes.add(0, getTitleNodeSpacer());
			ctNode = new CanvasTitleNode(parentUUID, CanvasTitleNodeType.TITLE);
			titleNodes.add(0, ctNode);
			parentUUID = CIntentionCellController.getInstance().getCIntentionCellParent(parentUUID);
		}
		
		titleNodes.add(0, getTitleNodeSpacer());
		ctNode = new CanvasTitleNode(CanvasTitleNode.WALL, CanvasTitleNodeType.TITLE);
		titleNodes.add(0, ctNode);
		

		//lay them out from left to right
		//get their width and height
//		titleNodeContainer.setBounds(PANEL_COMPONENT_INSET, ROW_TEXT_INSET, width, maxHeight);
		
		int xPos = (int)titleNodeContainer.getBounds().getX();
		int yPos = (int)titleNodeContainer.getBounds().getY();
		for (PText n : titleNodes)
		{
			n.setX(xPos);
			n.setY(yPos);
			titleNodeContainer.addChild(n);
			xPos += n.getWidth() + CanvasTitleNodeContainer.CTNODE_SPACING;
		}
		
		CalicoDraw.addChildToNode(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer(CCanvas.Layer.TOOLS), 
				titleNodeContainer);
//		titles = new ArrayList<CanvasTitleNodeContainer>();
//		titles.add(titleContainer);
		
	}
	
	/**
	 * Defines if the children of CanvasTitleNode should go downward or rightward.
	 * @author nfmangano
	 *
	 */
	public enum CanvasTitleNodeType {
		TITLE(0), DROPDOWN(1);

		public final int id;

		private CanvasTitleNodeType(int id) {
			this.id = id;
		}
	}
	
	private class CanvasTitleNodeContainer extends PComposite
	{
		private long parentCanvas = 0l;
		CanvasTitleNodeType type;
		public static final int CTNODE_SPACING = 5;
		final static int CTNODE_VERTICAL_SPACING = 0;
		
		public CanvasTitleNodeContainer(long parentCanvas, CanvasTitleNodeType type)
		{
			this.parentCanvas = parentCanvas;
			this.type = type;
		}
		
		public long getParentCanvas()
		{
			return parentCanvas;
		}
		
		public void tap(Point p) {
			//Get which node was tapped
			int childIndex = getChildIndex(p);
			
			//show its children
			if (childIndex != -1)
			{
				boolean childContainerVisible = false;
				
				CanvasTitleNode child = (CanvasTitleNode)getChild(childIndex);
				for (CanvasTitleNodeContainer ctnc : titles)
					if (child.getCanvasId() == ctnc.getParentCanvas())
						childContainerVisible = true;
				
				clearDisplayedStack(this);
				if (!childContainerVisible)
					child.showChildren();
			}
		}
		
		public long getCanvasAt(Point p)
		{
			int childIndex = getChildIndex(p);
			
			if (childIndex != -1 
					&& getChild(childIndex) instanceof CanvasTitleNode)
			{
				return ((CanvasTitleNode)getChild(childIndex)).canvasId;
			}
			
			return 0;
		}

		public int getChildIndex(Point p) {
			int childIndex = -1;
			for (int i = 0; i < getChildrenCount(); i++)
			{
				if (getChild(i).getBounds().contains(p))
					if (getChild(i) instanceof CanvasTitleNode)
						childIndex = i;
			}
			return childIndex;
		}

		public double calculateWidth()
		{
			if (type == CanvasTitleNodeType.TITLE)
			{
				if (getChildrenCount() == 0)
					return 0;
				
				double width = getChild(0).getWidth();
				for (int i = 1; i < getChildrenCount(); i++)
				{
					width += CTNODE_SPACING + getChild(i).getWidth();	
				}
				
				return width;
				
			}
			else if (type == CanvasTitleNodeType.DROPDOWN)
			{
				if (getChildrenCount() == 0)
					return 0;
				
				double width = getChild(0).getWidth();
				for (int i = 1; i < getChildrenCount(); i++)
				{
					if (getChild(i).getWidth() > width)
						width = getChild(i).getWidth();	
				}
				
				return width;
			}
			
			return 0;
		}
		
		public double calculateHeight()
		{
			if (type == CanvasTitleNodeType.TITLE)
			{
				if (getChildrenCount() == 0)
					return 0;
				
				double height = getChild(0).getHeight();
				for (int i = 1; i < getChildrenCount(); i++)
				{
					if (getChild(i).getHeight() > height)
						height = getChild(i).getHeight();	
				}
				
				return height;
			}
			else if (type == CanvasTitleNodeType.DROPDOWN)
			{
				if (getChildrenCount() == 0)
					return 0;
				
				double height = getChild(0).getHeight();
				for (int i = 1; i < getChildrenCount(); i++)
				{
					height += CTNODE_VERTICAL_SPACING + getChild(i).getHeight();	
				}
				
				return height;
			}
			
			return 0;
		}
	}
	
	private class CanvasTitleNode extends PText
	{
		final static long WALL = -1;

		
		private long canvasId;
		CanvasTitleNodeType type;
		
		public CanvasTitleNode(long canvasId, CanvasTitleNodeType t)
		{
			this.canvasId = canvasId;
			this.type = t;
			
			refresh();
		}
		
		public long getCanvasId()
		{
			return canvasId;
		}
		
		public void refresh()
		{
			CIntentionCell cell = CIntentionCellController.getInstance().getCellByCanvasId(this.canvasId);
			if (cell == null && canvasId != CanvasTitleNode.WALL)
			{
				System.out.println("Warning: cell is null in calico.plugins.iip.components.canvas.CanvasTitlePanel.CanvasTitleNode.refresh(), canvasId is " + canvasId);
				return;
			}
			
			String tag = "";
			if (this.canvasId != CanvasTitleNode.WALL
					&& cell.getIntentionTypeId() != -1)
				tag = " (" + IntentionCanvasController.getInstance().getIntentionType(cell.getIntentionTypeId()).getName() + ")";
			
			String titlePrefix = "";
			if (! CIntentionCellController.getInstance().isRootCanvas(this.canvasId)
					&& this.canvasId != CanvasTitleNode.WALL)
				titlePrefix = cell.getSiblingIndex() + ". ";
			else if (this.canvasId != CanvasTitleNode.WALL)
			{
				int clusterIndex = cell.getClusterIndex();
				if (clusterIndex != -1)
					titlePrefix = "C" + clusterIndex + ". ";
				else
					titlePrefix = "C#. ";
			}
			
			String numChildren = "";
			if (this.canvasId != CanvasTitleNode.WALL
					&& type == CanvasTitleNodeType.DROPDOWN)
			{	
				int num = CIntentionCellController.getInstance().getCIntentionCellChildren(this.canvasId).length;
				if (num > 0)
					numChildren = " (" + num + ")";	
			}
				
			
			String title = "";
			if (this.canvasId == CanvasTitleNode.WALL)
				title = "Wall";
			else
				title = titlePrefix + cell.getTitle() + tag + numChildren;
			
			this.setText(title);
			this.setConstrainWidthToTextWidth(true);
			this.setConstrainHeightToTextHeight(true);
			this.setFont(this.getFont().deriveFont(20f));
//			Map<TextAttribute, Object> fontAttributes = new HashMap<TextAttribute, Object>();
//			fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
//			this.setFont(this.getFont().deriveFont(fontAttributes));
			this.recomputeLayout();
		}
		
		public void showChildren()
		{
			//initialize container
			CanvasTitleNodeContainer container = new CanvasTitleNodeContainer(this.canvasId, CanvasTitleNodeType.DROPDOWN);
			
			//create children immediately below this node
			layoutChildren(container);
			
			//add to layer
			CCanvasController.canvasdb.get(canvas_uuid).getCamera().addChild(container);
			titles.add(container);
			CalicoDraw.repaint(container);
		}

		public void layoutChildren(CanvasTitleNodeContainer container) {
			if (type == CanvasTitleNodeType.TITLE)
			{
				long[] children;
				if (this.canvasId == CanvasTitleNode.WALL)
					children = IntentionGraph.getInstance().getRootsOfAllClusters();
				else
					children = CIntentionCellController.getInstance().getCIntentionCellChildren(this.canvasId);
				
				CanvasTitleNode[] titleNodes = new CanvasTitleNode[children.length];
				int xPos = (int)getX();
				int yPosOriginal = (int)getY() + (int)getHeight() + CanvasTitleNodeContainer.CTNODE_VERTICAL_SPACING; 
				int yPos = yPosOriginal;
				for (int i = 0; i < titleNodes.length; i++)
				{	
					titleNodes[i] = new CanvasTitleNode(children[i], CanvasTitleNodeType.DROPDOWN);
					titleNodes[i].setX(xPos);
					titleNodes[i].setY(yPos);
					container.addChild(titleNodes[i]);
					
					yPos += titleNodes[i].getHeight() + CanvasTitleNodeContainer.CTNODE_VERTICAL_SPACING;
				}
				container.setBounds(getX(), yPosOriginal, container.calculateWidth(), container.calculateHeight());
			}
			else if (type == CanvasTitleNodeType.DROPDOWN)
			{
				long[] children = CIntentionCellController.getInstance().getCIntentionCellChildren(this.canvasId);
				
				int width = (int)getWidth();
				if (getParent() instanceof CanvasTitleNodeContainer)
					width = (int)(((CanvasTitleNodeContainer)getParent()).calculateWidth());
				
				CanvasTitleNode[] titleNodes = new CanvasTitleNode[children.length];
				int xPosOriginal = (int)getX() + width + CanvasTitleNodeContainer.CTNODE_SPACING;
				int yPosOriginal = (int)getY();
				int xPos = xPosOriginal;
				int yPos = yPosOriginal;
				
				PText spacer = getTitleNodeSpacer();
				spacer.setX(xPos);
				spacer.setY(yPos);
				container.addChild(spacer);
				xPos += spacer.getWidth();
				
				for (int i = 0; i < titleNodes.length; i++)
				{	
					titleNodes[i] = new CanvasTitleNode(children[i], CanvasTitleNodeType.DROPDOWN);
					titleNodes[i].setX(xPos);
					titleNodes[i].setY(yPos);
					container.addChild(titleNodes[i]);
					
					yPos += titleNodes[i].getHeight() + CanvasTitleNodeContainer.CTNODE_VERTICAL_SPACING;
				}
				container.setBounds(xPosOriginal, yPosOriginal, container.calculateWidth() + spacer.getWidth(), container.calculateHeight());
			}
		}
	}
	
	private void clearDisplayedStack(CanvasTitleNodeContainer upToThisContainer)
	{
		for (int i = titles.size()-1; i >= 0; i--)
		{
			if (upToThisContainer == null 
					|| titles.get(i).getParentCanvas() != upToThisContainer.getParentCanvas())
			{
				CalicoDraw.setVisible(titles.get(i), false);
				titles.get(i).getParent().removeChild(titles.get(i));
//				CalicoDraw.removeChildFromNode(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getLayer(CCanvas.Layer.TOOLS),
//						titles.get(i));
				titles.remove(i);
			}
			else
				break;
		}
	}
	
	private PText getTitleNodeSpacer()
	{
		PText spacer = new PText();
		spacer.setText(" > ");
		spacer.setConstrainWidthToTextWidth(true);
		spacer.setConstrainHeightToTextHeight(true);
		spacer.setFont(spacer.getFont().deriveFont(20f));
		spacer.recomputeLayout();
		return spacer;
		
	}
}
