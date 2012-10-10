package calico.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
import calico.Calico;
import calico.CalicoDraw;
import calico.CalicoOptions;
import calico.components.bubblemenu.BubbleMenu;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.CalicoInputManager;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PPaintContext;

public class CList extends CGroup {

	public Long2ReferenceArrayMap<Boolean> groupCheckValues = new Long2ReferenceArrayMap<Boolean>();
	int iconWidth = 32, iconHeight = 32, iconWidthBuffer = 4;
	int iconXSpace = this.iconWidth + this.iconWidthBuffer*2;
	int widthBuffer = 5;
	
	private final boolean debugList = false;
	
	Image checkIcon, uncheckedIcon;	
	
	public CList(long uuid, long cuid, long puid) {
		super(uuid, cuid, puid);
		
		//override load signature of scrap
		networkLoadCommand = NetworkCommand.CLIST_LOAD;
	}
	
	@Override
	protected void paint(final PPaintContext paintContext) {
		final Graphics2D g2 = paintContext.getGraphics();
		
		setPaint(Color.white);
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
			       0.3f));
		
		super.paint(paintContext);
		
		if (BubbleMenu.highlightedParentGroup == this.uuid)
		{
			if (CGroupController.exists(CalicoInputManager.group) && CalicoInputManager.group != this.uuid)
			{
				if (this.containsPoint(CalicoInputManager.mostRecentPoint.x, CalicoInputManager.mostRecentPoint.y))
				{
					g2.setColor(Color.blue);
					g2.draw(getNearestLine());
					this.repaintFrom(this.getBounds(), this);
					//CalicoDraw.repaintNode(this);
				}
			}
		}
		
		long[] childGroups = this.getChildGroups();
		
		Image checkImage;
		if (childGroups != null)
		{
			Rectangle[] iconBounds = getCheckIconBounds();
			for (int i = 0; i < childGroups.length; i++)
			{
				if (groupCheckValues.containsKey(childGroups[i]))
				{
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
						       1.0f));
					checkImage = (groupCheckValues.get(childGroups[i]).booleanValue())
						? checkIcon
						: uncheckedIcon;
					g2.drawImage(checkImage, iconBounds[i].x, iconBounds[i].y, iconBounds[i].width, iconBounds[i].height, null);
					
				}
			}
		}
		else
			System.out.println();
	}
	
	@Override
	public void setChildGroups(long[] gplist) {
		super.setChildGroups(gplist);
		initializeCheckValues();
//		resetListElementPositions();
	}
	
	@Override
	public void move(int x, int y)
	{
		super.move(x, y);
		ArrayList<PImage> groupIcons = new ArrayList<PImage>();
		
		Rectangle bounds;
		for (PImage icon : groupIcons)
		{
			if (icon == null || icon.getBounds() == null)
				continue;
			bounds = icon.getBounds().getBounds();
			bounds.translate(x, y);
			//icon.setBounds(bounds);
			CalicoDraw.setNodeBounds(icon, bounds);
		}
	}
	
	@Override
	public void addChildGroup(long grpUUID, int x, int y) {
		if (CGroupController.exists(grpUUID) && !CGroupController.groupdb.get(grpUUID).isPermanent())
			return;
		
		super.addChildGroup(grpUUID, x, y);
		if (!groupCheckValues.containsKey(grpUUID))
			groupCheckValues.put(grpUUID, new Boolean(false));
		
		resetListElementPositions(grpUUID, x, y);
		
		recomputeBounds();
		recomputeValues();
		
	}
	
	@Override
	public void deleteChildGroup(long grpUUID) {
		super.deleteChildGroup(grpUUID);
		
		resetListElementPositions();
		
		recomputeBounds();
		recomputeValues();

		if (this.debugList)
			Calico.logger.debug("Group removed from list: " + grpUUID);
	}
	
	@Override
	public void recomputeBounds()
	{
		resetListElementPositions();
		
		recomputeBoundsAroundElements();
	}

	public void recomputeBoundsAroundElements() {
		Rectangle bounds = getBoundsOfContents();
		
		Rectangle newBounds = new Rectangle(bounds.x - widthBuffer - iconXSpace, bounds.y,
				bounds.width + widthBuffer*2 + iconXSpace, bounds.height);
		
		CGroupController.no_notify_make_rectangle(this.uuid, newBounds.x, newBounds.y, newBounds.width, newBounds.height);
		
		
		//this.invalidatePaint();
		CalicoDraw.invalidatePaint(this);
		//this.repaint();
		CalicoDraw.repaint(this);
		super.recomputeBounds();
	}
	
	@Override
	public void recomputeValues()
	{
		
		long[] childGroups = getChildGroups();

		for (int i = 0; i < childGroups.length; i++)
		{
			if (CGroupController.groupdb.get(childGroups[i]) instanceof CList)
			{
				CList innerList = ((CList)CGroupController.groupdb.get(childGroups[i]));
				if (innerList.getChildGroups().length > 0)
				{
					boolean containsUnchecked = false;
					long[] innerListGroups = innerList.getChildGroups();
					for (int j = 0; j < innerListGroups.length; j++)
						if (innerList.groupCheckValues.containsKey(innerListGroups[j])
								&& innerList.groupCheckValues.get(innerListGroups[j]).booleanValue() == false)
							containsUnchecked = true;
					
					
					if (containsUnchecked)
//						CGroupDecoratorController.groupCheckValues.put(childGroups[i], new Boolean(false));
						setCheck(childGroups[i], false);
					else
//						CGroupDecoratorController.groupCheckValues.put(childGroups[i], new Boolean(true));
						setCheck(childGroups[i], true);
				}
			}
		}
		super.recomputeValues();
	}
	
	public void setCheck(long guuid, boolean value)
	{
		groupCheckValues.put(guuid, new Boolean(value));

		CalicoDraw.invalidatePaint(this);

		CalicoDraw.repaint(this);
	}
	
	public boolean isChecked(long guuid)
	{
		if (!groupCheckValues.containsKey(guuid)) { return false; }
		else
			return groupCheckValues.get(guuid).booleanValue();
	}
	
	@Override
	public boolean containsShape(Shape shape)
	{
		return this.containsPoint((int)shape.getBounds2D().getCenterX(), (int)shape.getBounds2D().getCenterY());
	}
	
	
	public void resetListElementPositions()
	{
		resetListElementPositions(false);
	}
	
	public void resetListElementPositions(boolean setLocationToFirstElement)
	{
		resetListElementPositions(setLocationToFirstElement, 0l, 0, 0);
	}
	
	public void resetListElementPositions(long guuid, int g_x, int g_y)
	{
		resetListElementPositions(false, guuid, g_x, g_y);
	}
	
	public void resetListElementPositions(boolean setLocationToFirstElement, long guuid, int g_x, int g_y) {
		
		int moveToX, moveToY, deltaX, deltaY, elementSpacing = 5;
		
		
		int yOffset = /*elementSpacing + */0;
		int widestWidth = 0;
		int x, y;
		
		if (setLocationToFirstElement && getChildGroups().length > 0)
		{
			long firstchild = getChildGroups()[0];
			x = CGroupController.groupdb.get(firstchild).getPathReference().getBounds().x - widthBuffer - iconXSpace; //  bounds.x; // + widthBuffer / 2;
			y = CGroupController.groupdb.get(firstchild).getPathReference().getBounds().y - yOffset; //bounds.y; // + elementSpacing / 2;
		}
		else 
		{
			if (getPathReference() == null)
				return;
			
			x = this.getPathReference().getBounds().x + CalicoOptions.group.padding;
			y = this.getPathReference().getBounds().y + CalicoOptions.group.padding;
		}
		
		Rectangle bounds; // = getDecoratedGroup().getPathReference().getBounds();
		
		long[] listElements = getChildGroups();
		
		listElements = insertGroupByYPosition(listElements, guuid, g_x, g_y);
		
		if (listElements.length < 1 || CGroupController.groupdb.get(listElements[0]) == null || CGroupController.groupdb.get(listElements[0]).getPathReference() == null)
			return;
		
		if (debugList)
		{
			Rectangle lb = getBounds().getBounds();
			System.out.printf("List (%d)/\n\t bounds: (%d,%d,%d,%d)", this.uuid, lb.x, lb.y, lb.width, lb.height);
			System.out.println("");
		}
		for (int i = 0; i < listElements.length; i++)
		{
			if (!CGroupController.exists(listElements[i]))
				continue;
			
			//destination
			moveToX = x + widthBuffer + iconXSpace;
			moveToY = y;
			
			//figure out offset
			bounds = CGroupController.groupdb.get(listElements[i]).getPathReference().getBounds();
			deltaX = moveToX - bounds.x;
			deltaY = moveToY - bounds.y;
			
			if (debugList)
			{
				System.out.printf("\t%d: %d, %d, %d, %d", i, moveToX, moveToY + yOffset, bounds.width, bounds.height);
				System.out.println("");
			}
			
			CGroupController.no_notify_move(listElements[i], deltaX, deltaY + yOffset);
			yOffset += bounds.height + elementSpacing;
			
			//check for the widest width
			if (bounds.width > widestWidth)
				widestWidth = bounds.width;
		}
		if (listElements.length == 0)
		{
			widestWidth = 100;
			yOffset = 50;
		}

	}
	
	/**
	 * This method assumes that the given array is already ordered by their Y position
	 * 
	 * @param listElements
	 * @param guuid
	 * @param gX
	 * @param gY
	 * @return
	 */
	private long[] insertGroupByYPosition(long[] listElements, long guuid,
			int gX, int gY) {
		
		if (!CGroupController.exists(guuid))
			return listElements;
		
		long[] retList = listElements.clone(); 
		
		for (int i = 0; i < retList.length; i++)
			if (retList[i] == guuid)
				retList = ArrayUtils.removeElement(retList, guuid);
				

		for (int i = 0; i < retList.length; i++)
		{
			if (CGroupController.groupdb.get(retList[i]).getMidPoint().getY() > gY)
			{
				retList = ArrayUtils.add(retList, i, guuid);
				break;
			}
		}
		
		if (retList.length > 0)
		{
			if (CGroupController.groupdb.get(retList[retList.length-1]).getMidPoint().getY() < gY)
				retList = ArrayUtils.add(retList, guuid);
		}
		
		return retList;
	}
	
	public Rectangle[] getCheckIconBounds()
	{
		int moveToY, elementSpacing = 5, widthBuffer = 5;
		int iconXSpace = this.iconWidth + this.iconWidthBuffer*2;
		
		int yOffset = elementSpacing + 0;
		int x, y;
		
		long[] listElements = getChildGroups();
		
		if (listElements == null || listElements.length == 0)
			return new Rectangle[] { };
		
		long firstchild = listElements[0];
		if (!CGroupController.exists(firstchild))
			return new Rectangle[] { };
		
		x = CGroupController.groupdb.get(firstchild).getPathReference().getBounds().x - widthBuffer - iconXSpace; //  bounds.x; // + widthBuffer / 2;
		y = CGroupController.groupdb.get(firstchild).getPathReference().getBounds().y - yOffset; //bounds.y; // + elementSpacing / 2;
		
		Rectangle bounds;
		
		
		Rectangle[] checkMarkBounds = new Rectangle[listElements.length];
		
		if (listElements.length < 1 || CGroupController.groupdb.get(listElements[0]) == null || CGroupController.groupdb.get(listElements[0]).getPathReference() == null)
			return null;
		
		for (int i = 0; i < listElements.length; i++)
		{
			if (!CGroupController.exists(listElements[i]))
				continue;
			
			//destination
			moveToY = y;
			
			//figure out offset
			bounds = CGroupController.groupdb.get(listElements[i]).getPathReference().getBounds();

			checkMarkBounds[i] = new Rectangle(x + iconWidthBuffer, moveToY + yOffset + bounds.height/2 - iconHeight/2, this.iconWidth, this.iconHeight);
			yOffset += bounds.height + elementSpacing;
		}
		
		return checkMarkBounds;
	}	
	
	public Line2D getNearestLine() {
//		Point2D midPoint = CGroupController.groupdb.get(CalicoInputManager.group).getMidPoint();
		Point2D referencePoint = new Point2D.Double(CalicoInputManager.mostRecentPoint.getX(), CalicoInputManager.mostRecentPoint.getY());
		long[] listElements = getChildGroups();
		
		int[] yPos = new int[listElements.length+1];
		int maxWidth = 0;
		Rectangle bds;
		
		for (int i = 0; i < listElements.length; i++)
		{
			if (CGroupController.groupdb.get(listElements[i]) == null)
				continue;
			bds = CGroupController.groupdb.get(listElements[i]).getPathReference().getBounds();
			yPos[i] = bds.y - 5/2;
			if (bds.width > maxWidth)
				maxWidth = bds.width;
		}
		if (listElements.length > 0)
		{
			bds = CGroupController.groupdb.get(listElements[listElements.length-1]).getPathReference().getBounds();
			yPos[yPos.length-1] = bds.y + bds.height + 5/2;
		}
		
		double smallestDistance = Integer.MAX_VALUE;
		int smallestIndex = 0;
		double distance = 0;
		for (int i = 0; i < yPos.length; i++)
		{
			if (smallestDistance > (distance = referencePoint.distance(new Point2D.Double(referencePoint.getX(), yPos[i]))))
			{
				smallestDistance = distance;
				smallestIndex = i;
			}
		}
		
		double x1 = getPathReference().getBounds2D().getX() + 10;
		double y = yPos[smallestIndex] - 1;
		int iconXSpace = this.iconWidth + this.iconWidthBuffer*2;
		
		return new Line2D.Double(x1, y, x1 + maxWidth - 4 + iconXSpace, y);
		
	}	
	
	private void initializeCheckValues()
	{
		checkIcon = CalicoIconManager.getIconImage("lists.checked");
		uncheckedIcon =  CalicoIconManager.getIconImage("lists.unchecked");
		
		long[] childGroups = getChildGroups().clone();
		for (int i = 0; i < childGroups.length; i++)
		{
			if (!groupCheckValues.containsKey(childGroups[i]))
			{
				groupCheckValues.put(childGroups[i], new Boolean(false));
			}
		}
	}
	
	/**
	 * Serialize this activity node in a packet
	 */
	@Override
	public CalicoPacket[] getUpdatePackets(long uuid, long cuid, long puid,
			int dx, int dy, boolean captureChildren) {
		
		//Creates the packet for saving this CGroup
		CalicoPacket packet = super.getUpdatePackets(uuid, cuid, puid, dx, dy,
				captureChildren)[0];

		long[] keySet = groupCheckValues.keySet().toLongArray();
		packet.putInt(keySet.length);
		for (int i = 0; i < keySet.length; i++)
		{
			packet.putLong(keySet[i]);
			packet.putBoolean(groupCheckValues.get(keySet[i]));
		}

		return new CalicoPacket[] { packet };
	}
	
	public long getGroupCheckMarkAtPoint(Point p)
	{
		Rectangle[] checkMarks = getCheckIconBounds();
		long[] children = getChildGroups();
		if (checkMarks == null || children == null)
			return 0l;
		
		for (int i = 0; i < checkMarks.length; i++)
			if (checkMarks[i].contains(p))
				return children[i];
		
//		LongSet keySet = CGroupDecoratorController.groupImages.keySet();
//		for (Long key : keySet)
//		{
//			if (CGroupDecoratorController.groupImages.get(key.longValue()).getBounds().contains(p))
//				return key;
//		}
		return 0l;
	}
	
	private long[] orderByYAxis(long[] listItems)
	{
		int[] yValues = new int[listItems.length];
		
		//copy Y values to array to sort
		for (int i=0;i<listItems.length;i++)
		{
			if (CGroupController.exists(listItems[i]))
				yValues[i] = (int)CGroupController.groupdb.get(listItems[i]).getMidPoint().getY();
		}
		
		//sort the y values
		java.util.Arrays.sort(yValues);
		
		//match the y values back to their Groups and return the sorted array
		long[] sortedElementList = new long[listItems.length];
		for (int i=0;i<listItems.length;i++)
		{
			for (int j=0;j<listItems.length;j++)
			{
				if (CGroupController.exists(listItems[j]))
				{
					if ((int)CGroupController.groupdb.get(listItems[j]).getMidPoint().getY() == yValues[i])
					{
						sortedElementList[i] = listItems[j];
						//This line needed in case multiple groups have the same y value
						listItems[j] = -1;
						break;
					}
				}
			}
		}
		
		return sortedElementList;
	}
	
	@Override
	public boolean canParentChild(long child, int x,  int y)
	{
		if (child == 0l || child == this.uuid)
			return false;
		
		long potentialParent_new_parent = 0l;
		long child_parent = 0l;
		
		potentialParent_new_parent = getParentUUID();
		
		if (CStrokeController.strokes.containsKey(child))
		{
			return false;
		}
		else if (CGroupController.groupdb.containsKey(child))
		{
			if (!CGroupController.groupdb.get(child).isPermanent())
				return false;
			
			//must contain center of mass
			Point2D center = CGroupController.groupdb.get(child).getMidPoint();
			if (!this.containsPoint(x, y))
				return false;
			
			child_parent = CGroupController.groupdb.get(child).getParentUUID();
		}
		
		if (CGroupController.group_is_ancestor_of(child, this.uuid))
			return false;
		
		if (child_parent == 0l)
			return true;
		
		return potentialParent_new_parent == child_parent;
	}
	
	@Override
	public long[] getChildGroups()
	{
		return orderByYAxis(super.getChildGroups());
	}
	
}
