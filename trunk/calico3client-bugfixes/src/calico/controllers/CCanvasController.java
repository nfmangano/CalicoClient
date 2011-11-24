package calico.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.RepaintManager;

import org.apache.log4j.Logger;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.components.CArrow;
import calico.components.CCanvas;
import calico.components.CGroup;
import calico.components.CStroke;
import calico.components.CViewportCanvas;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.events.CalicoEventHandler;
import calico.modules.MessageObject;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.plugins.CalicoPluginManager;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;

/**
 * This handles all canvas requests
 * 
 * @author Mitch Dempsey
 */
public class CCanvasController {
	private static Logger logger = Logger.getLogger(CCanvasController.class
			.getName());

	public static Long2ReferenceOpenHashMap<CCanvas> canvasdb = new Long2ReferenceOpenHashMap<CCanvas>();

	public static Long2ReferenceOpenHashMap<PCamera> canvasCameras = new Long2ReferenceOpenHashMap<PCamera>();

	public static CGroup currentGroup = null;

	static long currentCanvasUUID = 0L;

	// Does nothing right now
	public static void setup() {
		canvasdb.clear();
		canvasCameras.clear();
	}

	public static boolean exists(long uuid) {
		return canvasdb.containsKey(uuid);
	}

	public static void no_notify_clear(long uuid) {
		if (!exists(uuid)) {
			return;
		}

		canvasdb.get(uuid).clear();
		CalicoDataStore.gridObject.updateCell(uuid);
	}

	public static void clear(long uuid) {
		Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_CLEAR,
				uuid));
		// no_notify_clear(uuid);
	}

	public static void no_notify_clear_for_state_change(long uuid) {
		// TODO: This should somehow cache the groups

//		RepaintManager.currentManager(canvasdb.get(uuid)).
//		canvasdb.get(uuid).setBuffering(true);
		canvasdb.get(uuid).getLayer().setVisible(false);
		canvasdb.get(uuid).setEnabled(false);
		
//		canvasdb.get(uuid).setEnabled(false);
//		canvasdb.get(uuid).setDoubleBuffered(true);
		
//		canvasdb.get(uuid).setInteracting(true);
//		canvasdb.get(uuid).setIgnoreRepaint(true);
		canvasdb.get(uuid).resetLock();
		
		long[] groups = canvasdb.get(uuid).getChildGroups();
		long[] strokes = canvasdb.get(uuid).getChildStrokes();
		long[] arrows = canvasdb.get(uuid).getChildArrows();

		if (strokes.length > 0) {
			for (int i = 0; i < strokes.length; i++) {
				CCanvasController.canvasdb.get(uuid).getLayer().removeChild(
						CStrokeController.strokes.get(strokes[i]));
				CStrokeController.no_notify_delete(strokes[i]);
			}
		}
		
		if (arrows.length > 0) {
			for (int i = 0; i < arrows.length; i++) {
				CCanvasController.canvasdb.get(uuid).getLayer().removeChild(
						CArrowController.arrows.get(arrows[i]));
				CGroupController.no_notify_delete(arrows[i]);
			}
		}

		if (groups.length > 0) {
			for (int i = groups.length - 1; i >= 0; i--) {
				CGroupController.no_notify_delete(groups[i]);
			}
		}



		CCanvasController.canvasdb.get(uuid).repaint();
		CalicoDataStore.gridObject.updateCell(uuid);

	}

	public static void no_notify_state_change_complete(long uuid) {
		// just repaint it
		canvasdb.get(uuid).validate();
		if (CalicoDataStore.isInViewPort) {
			CViewportCanvas.getInstance().repaint();
		}
		
		Networking.synchroized = true;
		if (CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()) != null)
			CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).drawToolbar();
//		canvasdb.get(uuid).setEnabled(true);
//		canvasdb.get(uuid).setInteracting(false);
//		canvasdb.get(uuid).setIgnoreRepaint(false);
//		canvasdb.get(uuid).setDoubleBuffered(false);
		canvasdb.get(uuid).setEnabled(true);
		canvasdb.get(uuid).getLayer().setVisible(true);
		
		canvasdb.get(uuid).repaint();
//		canvasdb.get(uuid).setBuffering(false);
		
		CalicoEventHandler.getInstance().fireEvent(NetworkCommand.STATUS_SENDING_LARGE_FILE_FINISHED, CalicoPacket.getPacket(NetworkCommand.STATUS_SENDING_LARGE_FILE_FINISHED, 1d, 1d, ""));
	}

	public static long[] getCanvasIDList() {
		return canvasdb.keySet().toLongArray();
	}

	public static long getCurrentUUID() {
		return currentCanvasUUID;
	}

	public static void setCurrentUUID(long u) {
		currentCanvasUUID = u;		
	}

	public static void windowResized() {
		// processese the resize of the windows
		redrawMenuBars();
	}

	public static void redrawMenuBars() {
		redrawMenuBars(getCurrentUUID());
	}

	public static void redrawMenuBars(long uuid) {
		if (!canvasdb.containsKey(uuid))
		{
			System.out.println("Warning, attempting to draw menu bar on a canvas that doesn't exist! Key: " + uuid);
			return;
		}
		
		canvasdb.get(uuid).drawToolbar();
		if (CalicoDataStore.isInViewPort) {
			CViewportCanvas.getInstance().drawToolbar();

		}

		// TODO do this also for grid and for viewport views

	}
	
	public static void redrawToolbar_clients(long uuid) {
		if (!canvasdb.containsKey(uuid))
		{
			System.out.println("Warning, attempting to draw menu bar on a canvas that doesn't exist! Key: " + uuid);
			return;
		}
		
		canvasdb.get(uuid).redrawToolbar_clients();		
	}
	
	public static int getGlobalClientCount()
	{
		int totalClients = 0;

		long[] cuids = CCanvasController.canvasdb.keySet().toLongArray();
		for(int x=0; x < cuids.length; x++)
		{
			totalClients = totalClients + CCanvasController.canvasdb.get(cuids[x]).getClients().length;
		}
		return totalClients;
	}

	/**
	 * Reloads the top menu bar (this shows state changes and stuff)
	 */
	public static void redrawTopMenubar() {
		canvasdb.get(getCurrentUUID()).drawTopToolbar();
		// TODO do this also for grid and for viewport views
	}

	public static long getCanvasAtPos(int x, int y) {
		// This will give the UUID for the canvas at the specific X/Y pos

		long[] cuids = getCanvasIDList();
		for (int i = 0; i < cuids.length; i++) {
			if (canvasdb.get(cuids[i]).isGridPos(x, y)) {
				return cuids[i];
			}
		}
		return 0L;
	}

	public static long getCanvasAtPoint(Point point) {
		// This will give the UUID for the canvas at the specific X/Y pos

		long[] cuids = getCanvasIDList();
		for (int i = 0; i < cuids.length; i++) {
			if (canvasdb.get(cuids[i]).isClickedGridThumb(point.x, point.y)) {
				return cuids[i];
			}
		}
		return 0L;
	}

	public static void loadCanvas(long uuid) {
		
//		if (CCanvasController.getCurrentUUID() == uuid)
//			return;
//		
//		CCanvasController.setCurrentUUID(uuid);
		//restore scale of old canvas
		if (CCanvasController.currentCanvasUUID != 0l)
			CCanvasController.canvasdb.get(CCanvasController.currentCanvasUUID).getLayer().setScale(1.0d);
		
		Calico cal = CalicoDataStore.calicoObj;
		CalicoDataStore.isViewingGrid = false;
//		cal.getContentPane().removeAll();
		
		Component[] comps = CalicoDataStore.calicoObj.getContentPane().getComponents();
		
		// This code here is to fix the bug where the viewport messes up the
		// cameras of the canvas
		CCanvas canvas = CCanvasController.canvasdb.get(uuid);
		canvas.drawBottomToolbar();
		
//		//get bounds of contents on canvas
//		Rectangle boundsOfChildren = CCanvasController.canvasdb.get(uuid).getLayer().getUnionOfChildrenBounds(null).getBounds();
//		Rectangle boundsOfCanvas = CCanvasController.canvasdb.get(uuid).getBounds();
//		
//		//if bounds of contents on canvas is larger than screen, then zoom out.
//		if ((boundsOfCanvas.width < boundsOfChildren.width || boundsOfCanvas.height < boundsOfChildren.height) && !CalicoDataStore.isInViewPort)
//		{
//			double widthScale = (double)boundsOfCanvas.width / (double)boundsOfChildren.width;
//			double heightScale = (double)boundsOfCanvas.height / (double)boundsOfChildren.height;
//			
//			double scale = (widthScale < heightScale)?widthScale:heightScale;
//			scale *= .95d;
//			
//			CCanvasController.canvasdb.get(uuid).getLayer().setScale(scale);
//		}
		
		if (CCanvasController.canvasCameras.containsKey(uuid)) {
			canvas.setCamera(CCanvasController.canvasCameras.get(uuid));
		}
		// end of bug fix
		cal.getContentPane().add(canvas);
		
		for (int i = 0; i < comps.length; i++)
			CalicoDataStore.calicoObj.getContentPane().remove(comps[i]);
		
		cal.setJMenuBar(null);
		cal.pack();
		initializeCanvas(uuid);
		cal.setVisible(true);
		cal.repaint();

//		initializeCanvas(uuid);
		
		cal.requestFocus();
	}

	public static void initializeCanvas(long uuid) {
		// Networking.send(NetworkCommand.CLICK_CANVAS, cellid);
		if (CCanvasController.getCurrentUUID() != 0L) {

			Networking.send(NetworkCommand.PRESENCE_LEAVE_CANVAS,
					CCanvasController.getCurrentUUID(), uuid);
		}
		Networking.send(NetworkCommand.PRESENCE_VIEW_CANVAS, uuid);
		Networking.send(NetworkCommand.PRESENCE_CANVAS_USERS, uuid);
		CCanvasController.setCurrentUUID(uuid);

		CArrowController.setOutstandingAnchorPoint(null);
//		calico.events.CalicoEventHandler.getInstance().fireEvent(NetworkCommand.PRESENCE_CANVAS_USERS, CalicoPacket.getPacket(NetworkCommand.PRESENCE_CANVAS_USERS, uuid));
		calico.events.CalicoEventHandler.getInstance().fireEvent(NetworkCommand.VIEWING_SINGLE_CANVAS, CalicoPacket.getPacket(NetworkCommand.VIEWING_SINGLE_CANVAS, uuid));
		CalicoPluginManager
				.FireEvent(new calico.plugins.events.ui.ViewSingleCanvas(uuid));

		// Make sure the Menu bar has all been redrawn and updated
//		canvas.drawToolbar();
//		canvas.menuBar.invalidateFullBounds();
		
		MessageObject.showNotice("Viewing canvas "
				+ CCanvasController.canvasdb.get(uuid).getGridCoordTxt());
	}

	/**
	 * @deprecated
	 * @param uuid
	 */
	public static void drawCanvasMenubars(long uuid) {

	}

	public static Image image(long uuid) {
		return canvasdb.get(uuid).toImage();
	}

	public static int get_signature(long uuid) {
		return canvasdb.get(uuid).getSignature();
	}

	public static void no_notify_add_child_stroke(long cuid, long uuid,
			boolean addToPiccolo) {
		if (!canvasdb.containsKey(cuid))
		{
			logger.warn("Attempting to add a stroke to non-existing canvas: " + cuid + " !!");
			return;
		}
		
		
		canvasdb.get(cuid).addChildStroke(uuid);

		// add to the painter
		if (addToPiccolo) {
			canvasdb.get(cuid).getLayer().addChild(
					CStrokeController.strokes.get(uuid));
		}
	}

	public static void no_notify_add_child_stroke(long cuid, long uuid) {
		no_notify_add_child_stroke(cuid, uuid, true);
	}

	public static void no_notify_delete_child_group(long cuid, long uuid) {
		canvasdb.get(cuid).deleteChildGroup(uuid);
	}

	public static void no_notify_delete_child_stroke(long cuid, long uuid) {
		canvasdb.get(cuid).deleteChildStroke(uuid);
	}

	public static void no_notify_delete_child_list(long cuid, long uuid) {
		canvasdb.get(cuid).deleteChildList(uuid);
	}

	public static void no_notify_flush_dead_objects() {
		long[] cuids = canvasdb.keySet().toLongArray();

		for (int i = 0; i < cuids.length; i++) {

			int children = canvasdb.get(cuids[i]).getLayer().getChildrenCount();
			for (int c = children - 1; c >= 0; c--) {
				PNode childobj = canvasdb.get(cuids[i]).getLayer().getChild(c);

				if (childobj instanceof CStroke) {
					if (!canvasdb.get(cuids[i]).hasChildStroke(
							((CStroke) childobj).getUUID())) {
						canvasdb.get(cuids[i]).getLayer().removeChild(c);
					}
				} else if (childobj instanceof CGroup) {
					if (!canvasdb.get(cuids[i]).hasChildGroup(
							((CGroup) childobj).getUUID())) {
						canvasdb.get(cuids[i]).getLayer().removeChild(c);
					}
				} else if (childobj instanceof CArrow) {
					if (!canvasdb.get(cuids[i]).hasChildArrow(
							((CArrow) childobj).getUUID())) {
						canvasdb.get(cuids[i]).getLayer().removeChild(c);
					}
				}

			}

		}// for cuids
	}

	public static boolean canvas_has_child_group_node(long cuid, long uuid) {
		if (!CGroupController.exists(uuid)) {
			return false;
		}

		return (canvasdb.get(cuid).getLayer().indexOfChild(
				CGroupController.groupdb.get(uuid)) != -1);
	}

	public static boolean canvas_has_child_stroke_node(long cuid, long uuid) {
		if (!CStrokeController.exists(uuid)) {
			return false;
		}

		return (canvasdb.get(cuid).getLayer().indexOfChild(
				CStrokeController.strokes.get(uuid)) != -1);
	}
	
	public static void lock_canvas(long canvas, boolean lock, String lockedBy, long time) {
		no_notify_lock_canvas(canvas, lock, lockedBy, time);
		
		Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_LOCK, canvas, lock, lockedBy, time));
	}

	public static void no_notify_lock_canvas(long canvas, boolean lock, String lockedBy, long time) {
		if (!exists(canvas)) { return; }
		
		canvasdb.get(canvas).setCanvasLock(lock, lockedBy, time);
		if (CalicoDataStore.gridObject != null)
			CalicoDataStore.gridObject.updateCell(canvas);
		
		canvasdb.get(canvas).drawToolbar();
	}
	
	public static boolean isEmpty(long cuid)
	{
		if (!exists(cuid))
			return false;
		
		CCanvas dest = canvasdb.get(cuid);
		if (dest.getChildStrokes().length==0&&dest.getChildGroups().length==0&&dest.getChildArrows().length==0)
			return true;
		return false;
	}
	
	public static void show_canvas_piemenu(Point point)
	{
		CGroup group = new CGroup(0l,0l,0l,false);
		ObjectArrayList<Class<?>> pieMenuButtons = group.getPieMenuButtons();
		
		ArrayList<PieMenuButton> buttons = new ArrayList<PieMenuButton>();
		try 
		{
			
			for (int i = 0; i < pieMenuButtons.size(); i++)
			{
				if (pieMenuButtons.get(i).getName().compareTo("calico.components.piemenu.canvas.ArrowButton") == 0
						|| pieMenuButtons.get(i).getName().compareTo("calico.components.piemenu.canvas.ImageCreate") == 0)
				{
					buttons.add((PieMenuButton) pieMenuButtons.get(i).getConstructor(long.class).newInstance(0l));
				}
				else
				{
					buttons.add(new PieMenuButton(new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB)));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		PieMenu.displayPieMenuArray(point, buttons.toArray(new PieMenuButton[buttons.size()]));
		
	}
	
	public static CCanvas getRestoredCanvas(CalicoPacket[] p)
	{
		CCanvas canvas = new CCanvas(-1, "-1", -1, -1);
		
		for (int i = 0; i < p.length; i++)
		{
			p[i].rewind();
			int comm = p[i].getInt();
			
			if (comm == NetworkCommand.ARROW_CREATE)
			{
				
			}
			else if (comm == NetworkCommand.GROUP_LOAD)
			{
				
			}
			else if (comm == NetworkCommand.STROKE_LOAD)
			{
				
			}
			
		}
		
		
		return null;
	}
	
}
