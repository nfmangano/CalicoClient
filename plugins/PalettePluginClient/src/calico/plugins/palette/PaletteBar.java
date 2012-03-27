package calico.plugins.palette;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import calico.CalicoDataStore;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.StickyItem;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.plugins.palette.menuitems.*;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolox.nodes.PComposite;

public class PaletteBar extends PComposite 
	implements CalicoEventListener, StickyItem {
	
	private int numItemsShown;
	
	int xLoc = 50, yLoc = 50;
	int itemBuffer = 5;
	int defaultHeight = 30;
	long uuid;
	private boolean menuBarIconsVisible = false;
	
//	Palette palette;
	
	
	public PaletteBar(int numItemsShown, Palette p)
	{
		this.numItemsShown = numItemsShown;
//		this.palette = p;
		this.uuid = calico.Calico.uuid();
		
		setupPaletteItems(numItemsShown);
//		CalicoEventHandler.getInstance().addListenerForType("PALETTE", this, CalicoEventHandler.PASSIVE_LISTENER);
		for (Integer event : PalettePlugin.getNetworkCommands(PaletteNetworkCommands.class))
			CalicoEventHandler.getInstance().addListener(event.intValue(), this, CalicoEventHandler.PASSIVE_LISTENER);
		
		CalicoInputManager.addCustomInputHandler(getUUID(), new PaletteInputHandler(this));
		CalicoInputManager.registerStickyItem(this);
	}

	private void setupPaletteItems(int numItemsShown) {
		ArrayList<PNode> newChildren = new ArrayList<PNode>();
		

		
		if (true)
		{
			
			newChildren.add(new NewPalette());
			newChildren.add(new SavePalette());
			newChildren.add(new OpenPalette());
			newChildren.add(new ClosePalette());
			newChildren.add(new ImportImages());
//			newChildren.add(new HideMenuBarIcons());
		}
		else
		{
//			newChildren.add(new ShowMenuBarIcons());
		}
		
		newChildren.add(new ShiftToLeftPalette());
		
		newChildren.add(new ShiftToRightPalette());
		
		
		newChildren.addAll(addPaletteItems(numItemsShown));
		
		int widthOfItems = itemBuffer + (PalettePlugin.PALETTE_ITEM_WIDTH + itemBuffer) * newChildren.size();
		
		
		setBounds(xLoc, yLoc, widthOfItems, defaultHeight + itemBuffer * 2);
		
		this.removeAllChildren();
		this.addChildren(newChildren);
		
		this.setPaint(Color.GRAY);
	}

	private ArrayList<PNode> addPaletteItems(int numItemsShown) {
		ArrayList<PNode> ret = new ArrayList<PNode>();
		ArrayList<CalicoPacket> paletteItems = PalettePlugin.getActivePalette().getPaletteItems();
		for (int i = 0; i < Math.max(numItemsShown,paletteItems.size()); i++)
		{
			PNode item;
			if (paletteItems.size() > i)
			{
				CalicoPacket packet = paletteItems.get(i);
				packet.rewind();
				int comm = packet.getInt();
				
				if (comm != PaletteNetworkCommands.PALETTE_PACKET)
				{
					item = new PaletteBarItem(0l, 0l, null);
					continue;
				}
				
				long uuid = packet.getLong();
				
				long paletteUUID = packet.getLong();
				
				Image paletteItemImage = packet.getBufferedImage();
				
				item = new PaletteBarItem(this.getUUID(), uuid, paletteItemImage);
			}
			else
			{
				item = new PaletteBarItem(0l, 0l, null);
			}
			ret.add(item);
		}
		return ret;
	}
	
	public void layoutChildren() {
		double xOffset = xLoc + itemBuffer;
		double yOffset = yLoc + itemBuffer;
			
		Iterator i = getChildrenIterator();
		
		while (i.hasNext()) {
			PNode each = (PNode) i.next();
			each.setOffset(xOffset - each.getX(), yOffset);
			xOffset += each.getFullBoundsReference().getWidth() + itemBuffer;
		}
	}
	
	public boolean visible()
	{
		long cuid = CCanvasController.getCurrentUUID();
		PCamera camera = CCanvasController.canvasdb.get(cuid).getCamera();
		
		int paletteIndex = -1;
		for (int i = 0; i < camera.getChildrenCount(); i++)
			if (camera.getChild(i) instanceof PaletteBar
				&& ((PaletteBar)camera.getChild(i)).getUUID() == this.getUUID())
				paletteIndex = i;
		
		return paletteIndex != -1 && super.getVisible();
	}
	
	public long getUUID()
	{
		return uuid;
	}

	@Override
	public void handleCalicoEvent(int event, CalicoPacket p) {
		if (event == PaletteNetworkCommands.PALETTE_PACKET)
		{
			p.rewind();
			p.getInt();
			long itemUUID = p.getLong();
			long paletteUUID = p.getLong();
			setupPaletteItems(numItemsShown);
			repaint();
		}
		else if (event == PaletteNetworkCommands.PALETTE_SWITCH_VISIBLE_PALETTE)
		{
			setupPaletteItems(numItemsShown);
			repaint();
		}
		else if (event == PaletteNetworkCommands.PALETTE_HIDE_MENU_BAR_ICONS)
		{
			hideMenuBarIcons();
		}
		else if (event == PaletteNetworkCommands.PALETTE_SHOW_MENU_BAR_ICONS)
		{
			showMenuBarIcons();
		}
		else if (event == PaletteNetworkCommands.PALETTE_PASTE_ITEM)
		{
			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).repaint();
		}
		
//		if (PalettePlugin.getActivePalette().contains(itemUUID))
		
		
	}

	@Override
	public boolean containsPoint(Point p) {
		return getGlobalBounds().contains(p);
	}
	
	public void setVisible(boolean visible)
	{
		if (visible)
			CalicoInputManager.registerStickyItem(this);
		else
			CalicoInputManager.unregisterStickyItem(this);
		super.setVisible(visible);
	}
	
	public void hideMenuBarIcons()
	{
		menuBarIconsVisible = false;
//		Image initialImage = this.toImage();
//		final BufferedImage bInitialImage = new BufferedImage(initialImage.getWidth(null), initialImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//		bInitialImage.getGraphics().drawImage(initialImage, 0, 0, null);
//		
//		PActivity flash = new PActivity(500,70, System.currentTimeMillis()) {
//			long step = 0;
//      
//		    protected void activityStep(long time) {
//		            super.activityStep(time);
//		            float t = 1.0f - 1.0f * step/5;
//		            
//		            //perform drawing here
//		            
//		            step++;
//		            if (t <= 0)
//		            	terminate();
//		    }
//		    
//		    protected void activityFinished() {
//
//		    }
//		};
//		
//		if (getRoot() != null)
//			getRoot().addActivity(flash);
		setupPaletteItems(numItemsShown);
		repaint();
		
		
	}
	
	public void showMenuBarIcons()
	{
		menuBarIconsVisible = true;
		setupPaletteItems(numItemsShown);
		repaint();
	}
	
	public boolean MenuBarIconsVisible()
	{
		return menuBarIconsVisible;
	}
	

	

	
	

}
