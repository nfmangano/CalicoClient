package calico.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.shodor.util11.PolygonUtils;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.CGroup;
import calico.components.CGroupImage;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.decorators.CGroupDecorator;
import calico.components.decorators.CListDecorator;
import calico.components.piemenu.PieMenu;
import calico.components.piemenu.PieMenuButton;
import calico.components.piemenu.groups.GroupRotateButton;
import calico.events.CalicoEventListener;
import calico.inputhandlers.CalicoInputManager;
import calico.networking.Networking;
import calico.networking.netstuff.ByteUtils;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.utils.Geometry;

/**
 * This handles all canvas requests
 * CGroupController
 * @author Mitch Dempsey
 */
public class CGroupController
{
	public static Logger logger = Logger.getLogger(CGroupController.class.getName());
	
	public static Long2ReferenceOpenHashMap<CGroup> groupdb = new Long2ReferenceOpenHashMap<CGroup>();
	
	private static LongArraySet delete_groups = new LongArraySet();
		
	private static long currentGroupUUID = 0L;
	private static long lastGroupUUID = 0L;
	
	private static long group_copy_uuid = 0L;
	public static boolean restoreOriginalStroke = false;
	public static long originalStroke = 0l;
	
	

	
	
	public static void setCopyUUID(long u)
	{
		group_copy_uuid = u;
	}
	public static long getCopyUUID()
	{
		return group_copy_uuid;
	}
	
	
	
	public static boolean dq_add(long uuid)
	{
		return delete_groups.add(uuid);
	}
	
	
	
	
	// Does nothing right now
	public static void setup()
	{
		groupdb.clear();
//		pieMenuButtons.clear();
		delete_groups.clear();
		
//		registerPieMenuButton(calico.components.piemenu.groups.GroupDropButton.class);
//		registerPieMenuButton(calico.components.piemenu.groups.GroupSetPermanentButton.class);
//		registerPieMenuButton(calico.components.piemenu.groups.GroupShrinkToContentsButton.class);
//		registerPieMenuButton(calico.components.piemenu.groups.ListCreateButton.class);
//		registerPieMenuButton(calico.components.piemenu.groups.GroupMoveButton.class);
//		registerPieMenuButton(calico.components.piemenu.groups.GroupCopyDragButton.class);
//		registerPieMenuButton(calico.components.piemenu.groups.GroupRotateButton.class);
//		registerPieMenuButton(calico.components.piemenu.canvas.ArrowButton.class);
//		registerPieMenuButton(calico.components.piemenu.groups.GroupDeleteButton.class);
//		registerPieMenuButton(calico.components.piemenu.canvas.TextCreate.class);
		
		
//		registerPieMenuButton(calico.components.piemenu.groups.GroupCopyButton.class);
//		registerPieMenuButton(calico.components.piemenu.groups.GroupConvexHullButton.class);
//		registerPieMenuButton(calico.components.piemenu.groups.GroupIncreaseSizeButton.class);
	}
	
	public static boolean exists(long uuid)
	{
		return groupdb.containsKey(uuid);
	}
	
	
	public static long getCurrentUUID()
	{
		return currentGroupUUID;
	}
	public static void setCurrentUUID(long u)
	{
		currentGroupUUID = u;
	}
	
	public static void setLastCreatedGroupUUID(long u)
	{
		lastGroupUUID = u;
	}
	public static long getLastCreatedGroupUUID()
	{
		return lastGroupUUID;
	}
	
	
	
	/**
	 * This will make sure to remove whatever was the last temporary group that you created
	 */
	public static void checkToRemoveLastTempGroup()
	{
		checkToRemoveLastTempGroup(0L);
	}
	
	// actionUUID == the uuid we are working with (if it matches the group uuid, we abort)
	public static void checkToRemoveLastTempGroup(long actionUUID)
	{
		if(lastGroupUUID==0L || actionUUID==lastGroupUUID)
			return;
		
		if(!exists(lastGroupUUID))
		{
			lastGroupUUID = 0L;
			return;
		}
		
		if(!groupdb.get(lastGroupUUID).isPermanent())
		{
			drop(lastGroupUUID);
		}
	}
	
	public static boolean checkIfLastTempGroupExists()
	{
		if(!exists(lastGroupUUID))
		{
			return false;
		}
		
		if(!groupdb.get(lastGroupUUID).isPermanent())
		{
			return false;
		}
		
		return true;
	}
	
	
	
	
	public static boolean is_parented_to(long uuid, long puid)
	{
		if (groupdb.get(uuid) == null)
			return false;
		return (groupdb.get(uuid).getParentUUID()==puid);
	}
	
	
	public static void no_notify_add_arrow(long uuid, long auuid)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).addChildArrow(auuid);
	}
	

	
	public static int get_signature(long uuid)
	{
		if(!exists(uuid)){return 0;}
		
		return groupdb.get(uuid).get_signature();
	}
	
	public static String get_signature_debug_output(long uuid)
	{
		if(!exists(uuid)){return "";}
		
		return groupdb.get(uuid).get_signature_debug_output();
	}
	
	
	
	/*
	 * TODO:setup some kind of nonotify_move functions to allow the server to not send (and not need an if/else)  
	 *
	 */
	
	public static void no_notify_start(long uuid, long cuid, long puid, boolean isperm)
	{
		if (!CCanvasController.exists(cuid))
			return;
		if(exists(uuid))
		{
			logger.debug("Need to delete group "+uuid);
			// WHOAA WE NEED TO DELETE THIS SHIT
			CCanvasController.canvasdb.get(cuid).getLayer().removeChild(groupdb.get(uuid));
//			CCanvasController.canvasdb.get(cuid).getCamera().repaint();
		}
		
		// Add to the GroupDB
		groupdb.put(uuid, new CGroup(uuid, cuid, puid, isperm));
		
		CCanvasController.canvasdb.get(cuid).addChildGroup(uuid);
		
		CCanvasController.canvasdb.get(cuid).getLayer().addChild(groupdb.get(uuid));
		
		groupdb.get(uuid).drawPermTemp(true);
		
		
		
		
		//CCanvasController.canvasdb.get(cuid).repaint();
	}
	
	public static void no_notify_append(long uuid, int x, int y)
	{
		// If we don't know wtf this UUID is for, then just eject
		if(!exists(uuid))
		{
			logger.warn("APPEND for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).append(x, y);
	}
	
	public static void no_notify_append(long uuid, int[] x, int[] y)
	{
		// If we don't know wtf this UUID is for, then just eject
		if(!exists(uuid))
		{
			logger.warn("APPEND for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).append(x, y);
	}
	
	public static void no_notify_move(long uuid, int x, int y)
	{
		// If we don't know wtf this UUID is for, then just eject
		if(!exists(uuid))
		{
			logger.warn("MOVE for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).move(x, y);
	}
	
	public static void no_notify_delete(final long uuid)
	{
		// If we don't know wtf this UUID is for, then just eject
		if(!exists(uuid))
		{
			logger.warn("DELETE for non-existant group "+uuid);
			return;
		}
		
		if (restoreOriginalStroke && CStrokeController.exists(originalStroke))
		{
			CStrokeController.unhideStroke(originalStroke);
			originalStroke = 0l;
			restoreOriginalStroke = false;
		}
		else if (originalStroke != 0l)
		{
			CStrokeController.delete(originalStroke);
			originalStroke = 0l;
		}
		
		groupdb.get(uuid).setTransparency(0f);
		if (groupdb.get(uuid).getBounds() != null)
		{
		int buffer = 5;
			Rectangle bounds = groupdb.get(uuid).getBounds().getBounds();
			Rectangle boundsWithBuffer =  new Rectangle(bounds.x - buffer, bounds.y - buffer, bounds.width + buffer * 2, bounds.height + buffer * 2);
			CCanvasController.canvasdb.get(groupdb.get(uuid).getCanvasUID()).repaint(boundsWithBuffer);
			groupdb.get(uuid).repaint();
		}
		
		// TODO: This should also delete the elements inside of the group first
		groupdb.get(uuid).delete();

		//CCanvasController.canvasdb.get(groupdb.get(uuid).getCanvasUID()).getLayer().removeChild(groupdb.get(uuid));
		if(CCanvasController.canvas_has_child_group_node(groupdb.get(uuid).getCanvasUID(), uuid))
		{
			groupdb.get(uuid).removeFromParent();
			groupdb.remove(uuid);
//			SwingUtilities.invokeLater(
//					new Runnable() { 
//						public void run() {
//
//						}
//					}
//			);
			
		}
//		
		
		dq_add(uuid);
	}
	
	public static void no_notify_finish(long uuid, boolean captureChildren)
	{
		boolean checkParenting = true;
		no_notify_finish(uuid, captureChildren, checkParenting, true);
		
	}


	public static void no_notify_finish(long uuid, boolean captureChildren,
			boolean checkParenting, boolean fade) {
		// If we don't know wtf this UUID is for, then just eject
		if(!exists(uuid))
		{
			logger.warn("Error: FINISH for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).finish(fade);
		if (checkParenting)
			recheck_parent(uuid);
		if (captureChildren)
			no_notify_calculate_parenting(uuid, true);
		
//		setLastCreatedGroupUUID(uuid);
	}
	
	public static void no_notify_calculate_parenting(final long uuid, final boolean includeStrokes)
	{
		if(!exists(uuid)){return;}
		
		Point2D mid = groupdb.get(uuid).getMidPoint();
		no_notify_calculate_parenting(uuid, includeStrokes, (int)mid.getX(), (int)mid.getY());
	}
	
	public static void no_notify_calculate_parenting(final long uuid, final boolean includeStrokes, int x, int y)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).calculateParenting(includeStrokes, x, y);
	}
	
	public static void recheck_parent(final long uuid)
	{
		if(!exists(uuid)){return;}// old one doesnt exist
		groupdb.get(uuid).recheckParentAfterMove();
	}
	
	public static void no_notify_bold(long uuid)
	{
	}
	public static void no_notify_unbold(long uuid)
	{
	}
	
	public static void no_notify_remove_child_group(final long uuid, final long cguuid)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).deleteChildGroup(cguuid);
	}
	
	
	public static void no_notify_drop(long uuid)
	{
		// If we don't know wtf this UUID is for, then just eject
		if(!exists(uuid))
		{
			logger.warn("DROP for non-existant group "+uuid);
			return;
		}
		
		CGroup group = groupdb.get(uuid);
		long[] child_strokes = group.getChildStrokes();
		long[] child_groups = group.getChildGroups();
		long[] child_arrows = group.getChildArrows();
		
		group.unparentAllChildren();
		
		no_notify_delete(uuid);
		// what is it's current parent?

		// Remove from the canvas
//		CCanvasController.no_notify_delete_child_group(group.getCanvasUID(), uuid);
//		groupdb.remove(uuid);
		
		
		// Reparent any strokes
		if(child_strokes.length>0)
		{
			for(int i=0;i<child_strokes.length;i++)
			{
				CStrokeController.strokes.get(child_strokes[i]).calculateParent();
			}
		}
		
		// Reparent any groups
		if(child_groups.length>0)
		{
			for(int i=0;i<child_groups.length;i++)
			{
				if (groupdb.containsKey(child_groups[i]))
					groupdb.get(child_groups[i]).recheckParentAfterMove();
				else
					System.err.println("Invalid key found for child group while parenting! Key: " + child_groups[i]);
			}
		}
		
		// Reparent any arrows
		if(child_arrows.length>0)
		{
			for(int i=0;i<child_arrows.length;i++)
			{
				CArrowController.arrows.get(child_arrows[i]).calculateParent();
			}
		}
	
		
		CalicoInputManager.unlockHandlerIfMatch(uuid);
		// XXXXXXXXXXXXXXXX
//		CCanvasController.canvasdb.get(group.getCanvasUID()).repaint();
	}
	
	public static void no_notify_set_parent(long uuid, long bguuid)
	{
		if(!exists(uuid)){return;}
		
		long curpuid = groupdb.get(uuid).getParentUUID();
		long undecoratedParent = getDecoratedGroup(bguuid); 
		
		if (curpuid == undecoratedParent)
			return;
		
		if (curpuid != 0L && groupdb.get(curpuid) instanceof CGroupDecorator)
			return;
		
		if(curpuid!=0L && exists(curpuid))
		{
			// We should update the current parent
			no_notify_delete_child_group(curpuid, uuid);
		}
		
		groupdb.get(uuid).setParentUUID(undecoratedParent);
		
		if(bguuid!=0L && exists(undecoratedParent))
		{
			long decoratedParent = getDecoratorParent(bguuid);
			Point2D midPoint = CGroupController.groupdb.get(uuid).getMidPoint();
			no_notify_add_child_group(decoratedParent, uuid, (int)midPoint.getX(), (int)midPoint.getY());
		}
	}
	
	public static boolean hasChildGroup(long uuid, long cuuid)
	{
		return groupdb.get(uuid).hasChildGroup(cuuid);
	}
	
	public static void no_notify_set_children(long uuid, long[] bgeuuids, long[] grpuuids)
	{
		// If we don't know wtf this UUID is for, then just eject
		if(!exists(uuid))
		{
			logger.warn("SET_CHILDREN for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).setChildStrokes(bgeuuids);
		groupdb.get(uuid).setChildGroups(grpuuids);
	}
	
	public static void no_notify_add_child_bge(long uuid, long childUUID)
	{
		// If we don't know wtf this UUID is for, then just eject
		if(!groupdb.containsKey(uuid))
		{
			logger.warn("ADD_CHILD_BGE for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).addChildStroke(childUUID);
	}
	public static void no_notify_add_child_grp(long uuid, long childUUID, int x, int y)
	{
		// If we don't know wtf this UUID is for, then just eject
		if(!groupdb.containsKey(uuid))
		{
			logger.warn("ADD_CHILD_GROUP for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).addChildGroup(childUUID, x, y);
	}
	
	public static void no_notify_set_permanent(long uuid, boolean isperm)
	{
		if(!exists(uuid))
		{
			logger.warn("GROUP_SET_PERM for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).setPermanent(isperm);
	}
	
	public static void no_notify_set_text(long uuid, String text)
	{
		if(!exists(uuid))
		{
			logger.warn("GROUP_SET_TEXT for non-existant group"+uuid);
			return;
		}
		
		groupdb.get(uuid).setText(text);
	}
	
	public static void no_notify_rectify(long uuid)
	{
		if(!exists(uuid))
		{
			logger.warn("rectify for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).rectify();
	}
	public static void no_notify_circlify(long uuid)
	{
		if(!exists(uuid))
		{
			logger.warn("circlify for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).circlify();
	}
	
	public static void no_notify_set_children_color(long uuid, Color col)
	{
	}
	
	
	public static void no_notify_reload_remove(long uuid)
	{
	}
	public static void no_notify_reload_start(long uuid, long cuid, long puid, boolean isPerm)
	{
	}
	public static void no_notify_reload_finish(long uuid)
	{
	}
	public static void no_notify_reload_coords(long uuid, int x, int y)
	{
	}
	public static void no_notify_reload_coords(long uuid, int[] x, int[] y)
	{
	}
	
	
	
	
	/*
	 * THESE VERSIONS WILL SEND OUT PACKETS TO NOTIFY OF MANIPULATION
	 * 
	 */
	public static void append(long uuid, int x, int y)
	{
		no_notify_append(uuid, x, y);
		
//		CalicoPacket p = new CalicoPacket( ByteUtils.SIZE_OF_INT + ByteUtils.SIZE_OF_LONG + ByteUtils.SIZE_OF_SHORT + ByteUtils.SIZE_OF_SHORT );
//		p.putInt(NetworkCommand.GROUP_APPEND);
//		p.putLong(uuid);
//		p.putInt(x);
//		p.putInt(y);
//		Networking.send(p);
	}
	public static void move(long uuid, int x, int y)
	{
		if (x == 0 && y == 0)
			return;
		
		no_notify_move(uuid, x, y);
		Networking.send(NetworkCommand.GROUP_MOVE, uuid, x, y);
	}

	public static void finish(long uuid, boolean captureChildren)
	{
		no_notify_finish(uuid, captureChildren);
//		Networking.send(NetworkCommand.GROUP_FINISH, uuid);
		loadGroup(uuid, captureChildren);
		setLastCreatedGroupUUID(uuid);
		
		
		//groupdb.get(uuid).extra_submitToDesignMinders();
		
	}
	
	public static void loadGroup(long guuid, boolean captureChildren)
	{
		if (!groupdb.containsKey(guuid))
		{
			System.err.println("Attempting to load a group that does not exist!");
			(new Exception()).printStackTrace();
			return;
		}

		CalicoPacket[] packets = groupdb.get(guuid).getUpdatePackets(captureChildren);
		
		for(int i=0;i<packets.length;i++)
		{
			Networking.send(packets[i]);
		}
	}
	
	
	
	public static void drop(long uuid)
	{

		System.out.println("Dropping group: " +  uuid);
		no_notify_drop(uuid);
		Networking.send(NetworkCommand.GROUP_DROP, uuid);
	}
	public static void delete(long uuid)
	{
		// WE WAIT FOR THE SERVER TO SEND US A DELETE PACKET!
		no_notify_delete(uuid);
		Networking.send(NetworkCommand.GROUP_DELETE, uuid);
	}
	// TODO: Maybe remove the puid alltogether, since we will always force 0L
	
	/**
	 * @deprecated
	 * @see #start(long, long, long, boolean)
	 */
	public static void start(long uuid, long cuid, long puid, int x, int y)
	{
		puid = 0L;//forcing - the server manaages the parents/children
		start(uuid, cuid, puid, false);
		append(uuid, x, y);
	}
	
	public static void start(long uuid, long cuid, long puid, boolean isperm)
	{
		no_notify_start(uuid, cuid, puid, isperm);
//		Networking.send(NetworkCommand.GROUP_START, uuid, cuid, puid, (isperm ? 1 : 0));
	}
	
	
	public static void set_parent(long uuid, long newparent)
	{
		no_notify_set_parent(uuid,newparent);
		Networking.send(NetworkCommand.GROUP_SET_PARENT, uuid, newparent);
	}
	public static void rectify(long uuid)
	{
		no_notify_rectify(uuid);
		Networking.send(NetworkCommand.GROUP_RECTIFY, uuid);
	}
	public static void circlify(long uuid)
	{
		no_notify_circlify(uuid);
		Networking.send(NetworkCommand.GROUP_CIRCLIFY, uuid);
	}
	
	public static void set_permanent(long uuid, boolean isperm)
	{
		no_notify_set_permanent(uuid,isperm);
		Networking.send(NetworkCommand.GROUP_SET_PERM, uuid, (isperm ? 1 : 0) );
	}
	
	public static void set_children_color(long uuid, Color col)
	{
		no_notify_set_children_color(uuid,col);
		Networking.send(NetworkCommand.GROUP_CHILDREN_COLOR, uuid, col.getRed(), col.getGreen(), col.getBlue());
	}

	public static void rotate(long uuid, double theta) {
		no_notify_rotate(uuid, theta);
		Networking.send(NetworkCommand.GROUP_ROTATE, uuid, theta);
	}

	public static void scale(long uuid, double scaleX, double scaleY) {
		no_notify_scale(uuid, scaleX, scaleY);
		Networking.send(NetworkCommand.GROUP_SCALE, uuid, scaleX, scaleY);
	}
	
	public static void set_text(long uuid, String str) 
	{
		if(!exists(uuid)){return;}
		no_notify_set_text(uuid,str);
		Networking.send(NetworkCommand.GROUP_SET_TEXT, uuid, str);
	}
	
	public static void create_text_scrap(long uuid, long cuuid, String text, int x, int y)
	{
		no_notify_create_text_scrap(uuid, cuuid, text, x, y);
		Networking.send(NetworkCommand.GROUP_CREATE_TEXT_GROUP, uuid, cuuid, text, x, y);
	}
	
	public static void no_notify_clear_child_strokes(long uuid)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).clearChildStrokes();
	}
	public static void no_notify_add_child_stroke(long uuid, long childuid)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).addChildStroke(childuid);
	}
	public static void no_notify_set_child_strokes(long uuid, long[] children)
	{
		if(!exists(uuid)){return;}
		
		
		groupdb.get(uuid).setChildStrokes(children);
		for(int i=0;i<children.length;i++)
		{
			if (CStrokeController.exists(children[i]))
			{
				if (CStrokeController.strokes.get(children[i]).getParentUUID() != uuid)
					CStrokeController.no_notify_set_parent(children[i], uuid);
			}
		}
	}
	
	
	public static void no_notify_set_child_groups(long uuid, long[] children)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).setChildGroups(children);
		
		groupdb.get(uuid).resetViewOrder();
//		for(int i=0;i<children.length;i++)
//		{
////			no_notify_add_child_group(uuid, children[i]);
//			if (groupdb.get(children[i]).getParentUUID() != uuid)
//				CGroupController.no_notify_set_parent(children[i], uuid);
//		}
	}
	public static void no_notify_clear_child_groups(long uuid)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).clearChildGroups();
	}
	public static void no_notify_add_child_group(long uuid, long childuid, int x, int y)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).addChildGroup(childuid, x, y);
	}
	public static void no_notify_delete_child_group(long uuid, long childuid)
	{
		if(!exists(uuid)){return;}
		
		long duuid = getDecoratorParent(uuid);
		
		groupdb.get(duuid).deleteChildGroup(childuid);
	}
	
	public static void no_notify_delete_child_stroke(long uuid, long childuid)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).deleteChildStroke(childuid);
	}
	public static void no_notify_delete_child_arrow(long uuid, long childuid)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).deleteChildArrow(childuid);
	}
	
	private static long getDecoratorParent(long uuid)
	{
		if (!exists(uuid)) { return 0l; }
		
		if (CGroupController.exists(groupdb.get(uuid).getParentUUID())
			&& groupdb.get(groupdb.get(uuid).getParentUUID()) instanceof CGroupDecorator)
			return getDecoratorParent(groupdb.get(uuid).getParentUUID());
		else
			return uuid;
	}
	
	private static long getDecoratedGroup(long uuid)
	{
		if (!exists(uuid)) { return 0l; }
		
		if (groupdb.get(uuid) instanceof CGroupDecorator)
			return getDecoratedGroup(((CGroupDecorator)groupdb.get(uuid)).getDecoratedUUID());
		else
			return uuid;
	}
	
	
	public static void no_notify_clear_child_arrows(long uuid)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).clearChildArrows();
	}
	public static void no_notify_set_child_arrows(long uuid, long[] children)
	{
		if(!exists(uuid)){return;}
		
		no_notify_clear_child_arrows(uuid);
		for(int i=0;i<children.length;i++)
		{
			no_notify_add_child_arrow(uuid, children[i]);
		}
	}
	public static void no_notify_add_child_arrow(long uuid, long childuid)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).addChildArrow(childuid);
	}
	
	
	public static void verify_hash(long uuid, byte[] hash)
	{
		if(!exists(uuid)){return;}
		
		byte[] cur_hash = groupdb.get(uuid).getHashCode();
		
		if(Arrays.equals(hash, cur_hash))
		{
			logger.debug("GROUP "+uuid+" PASSED HASH CHECK");
		}
		else
		{
			logger.warn("GROUP "+uuid+" FAILED HASH CHECK");
		}
	}
	
	public static void show_group_piemenu(long uuid, Point point)
	{
		show_group_piemenu(uuid, point, PieMenuButton.SHOWON_SCRAP_MENU);
	}
	
	public static void show_group_piemenu(long uuid, Point point, int showfilter)
	{
		//Class<?> pieMenuClass = calico.components.piemenu.PieMenu.class;
		if (!exists(uuid))
			return;
		
		ObjectArrayList<Class<?>> pieMenuButtons = CGroupController.groupdb.get(uuid).getPieMenuButtons();
		
		
		int curPos = 0;
		int totalButtons = 0;
		int[] bitmasks = new int[pieMenuButtons.size()];
		
		
		
		if(pieMenuButtons.size()>0)
		{
			ArrayList<PieMenuButton> buttons = new ArrayList<PieMenuButton>();
			
			for(int i=0;i<pieMenuButtons.size();i++)
			{
				try
				{
//					if (pieMenuButtons.get(i).getName().compareTo("GroupRotateButton") == 0
//							&& CGroupController.groupdb.get(i).getText().length() > 0)
//						continue;
					bitmasks[i] = pieMenuButtons.get(i).getField("SHOWON").getInt(null);
					if( ( bitmasks[i] & showfilter) == showfilter)
					{
						if (pieMenuButtons.get(i).getName().compareTo("calico.components.piemenu.groups.GroupShrinkToContentsButton") == 0
								&& groupdb.get(uuid).getBoundsOfContents().isEmpty()
								
							|| pieMenuButtons.get(i).getName().compareTo("calico.components.piemenu.groups.ListCreateButton") == 0
								&& groupdb.get(uuid).getChildGroups().length == 0)
						{
							buttons.add(new PieMenuButton(new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB)));
							continue;
						}
						
						buttons.add((PieMenuButton) pieMenuButtons.get(i).getConstructor(long.class).newInstance(uuid));
//						buttons[curPos++] = (PieMenuButton) pieMenuButtons.get(i).getConstructor(long.class).newInstance(uuid);
					}
					else
						buttons.add(new PieMenuButton(new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB)));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
//			for(int i=0;i<pieMenuButtons.size();i++)
//			{
//				try
//				{
////					if (pieMenuButtons.get(i).getName().compareTo("calico.components.piemenu.groups.GroupRotateButton") == 0
////							&& CGroupController.groupdb.get(uuid).getText().length() > 0)
////						continue;
//					bitmasks[i] = pieMenuButtons.get(i).getField("SHOWON").getInt(null);
//					if( (bitmasks[i] & showfilter) == showfilter)
//					{
//						totalButtons++;
//						//buttons[curPos++] = (PieMenuButton) pieMenuButtons.get(i).getConstructor(long.class).newInstance(uuid);
//					}
//				}
//				catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			PieMenuButton[] buttons = new PieMenuButton[totalButtons];
//			
//			for(int i=0;i<pieMenuButtons.size();i++)
//			{
//				try
//				{
////					if (pieMenuButtons.get(i).getName().compareTo("GroupRotateButton") == 0
////							&& CGroupController.groupdb.get(i).getText().length() > 0)
////						continue;
//					if( ( bitmasks[i] & showfilter) == showfilter)
//					{
//						buttons[curPos++] = (PieMenuButton) pieMenuButtons.get(i).getConstructor(long.class).newInstance(uuid);
//					}
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//			}

			PieMenu.displayPieMenuArray(point, buttons.toArray(new PieMenuButton[buttons.size()]));
			//BubbleMenu.displayBubbleMenu(point, CGroupController.groupdb.get(uuid).getBounds(),buttons.toArray(new PieMenuButton[buttons.size()]));
			CGroupController.groupdb.get(uuid).highlight_on();
			
		}
		
		
	}
	
	public static void show_group_bubblemenu(long uuid, Point point)
	{
		show_group_bubblemenu(uuid, point, PieMenuButton.SHOWON_SCRAP_MENU);
	}
	
	public static void show_group_bubblemenu(long uuid, Point point, int showfilter)
	{
		//Class<?> pieMenuClass = calico.components.piemenu.PieMenu.class;
		if (!exists(uuid))
			return;
		
		ObjectArrayList<Class<?>> pieMenuButtons = CGroupController.groupdb.get(uuid).getPieMenuButtons();
		
		
		int curPos = 0;
		int totalButtons = 0;
		int[] bitmasks = new int[pieMenuButtons.size()];
		
		
		
		if(pieMenuButtons.size()>0)
		{
			ArrayList<PieMenuButton> buttons = new ArrayList<PieMenuButton>();
			
			for(int i=0;i<pieMenuButtons.size();i++)
			{
				try
				{
//					if (pieMenuButtons.get(i).getName().compareTo("GroupRotateButton") == 0
//							&& CGroupController.groupdb.get(i).getText().length() > 0)
//						continue;
					bitmasks[i] = pieMenuButtons.get(i).getField("SHOWON").getInt(null);
					if( ( bitmasks[i] & showfilter) == showfilter)
					{
						if (pieMenuButtons.get(i).getName().compareTo("calico.components.piemenu.groups.GroupShrinkToContentsButton") == 0
								&& groupdb.get(uuid).getBoundsOfContents().isEmpty()
								
							|| pieMenuButtons.get(i).getName().compareTo("calico.components.piemenu.groups.ListCreateButton") == 0
								&& groupdb.get(uuid).getChildGroups().length == 0)
						{
							buttons.add(new PieMenuButton(new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB)));
							continue;
						}
						
						buttons.add((PieMenuButton) pieMenuButtons.get(i).getConstructor(long.class).newInstance(uuid));
//						buttons[curPos++] = (PieMenuButton) pieMenuButtons.get(i).getConstructor(long.class).newInstance(uuid);
					}
					else
						buttons.add(new PieMenuButton(new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB)));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
//			for(int i=0;i<pieMenuButtons.size();i++)
//			{
//				try
//				{
////					if (pieMenuButtons.get(i).getName().compareTo("calico.components.piemenu.groups.GroupRotateButton") == 0
////							&& CGroupController.groupdb.get(uuid).getText().length() > 0)
////						continue;
//					bitmasks[i] = pieMenuButtons.get(i).getField("SHOWON").getInt(null);
//					if( (bitmasks[i] & showfilter) == showfilter)
//					{
//						totalButtons++;
//						//buttons[curPos++] = (PieMenuButton) pieMenuButtons.get(i).getConstructor(long.class).newInstance(uuid);
//					}
//				}
//				catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			PieMenuButton[] buttons = new PieMenuButton[totalButtons];
//			
//			for(int i=0;i<pieMenuButtons.size();i++)
//			{
//				try
//				{
////					if (pieMenuButtons.get(i).getName().compareTo("GroupRotateButton") == 0
////							&& CGroupController.groupdb.get(i).getText().length() > 0)
////						continue;
//					if( ( bitmasks[i] & showfilter) == showfilter)
//					{
//						buttons[curPos++] = (PieMenuButton) pieMenuButtons.get(i).getConstructor(long.class).newInstance(uuid);
//					}
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//			}

			//PieMenu.displayPieMenuArray(point, buttons.toArray(new PieMenuButton[buttons.size()]));
			BubbleMenu.displayBubbleMenu(point, CGroupController.groupdb.get(uuid).getBounds(),buttons.toArray(new PieMenuButton[buttons.size()]));
			CGroupController.groupdb.get(uuid).highlight_on();
			
		}
		
		
	}
	
	public static boolean group_contains_stroke(final long containerUUID, final long checkUUID)
	{
		if (!CStrokeController.exists(checkUUID))
		{
//			logger.warn("CGroupController.group_contains_stroke: Stroke " + checkUUID + " doesn't exist!");
			return false;
		}
		else if (!exists(containerUUID))
		{
//			logger.warn("CGroupController.group_contains_stroke: Group " + containerUUID + " doesn't exist!");
			return false;
		}
		else if (CStrokeController.strokes.get(checkUUID).isTempInk())
		{
			return false;
		}
		else
			return groupdb.get(containerUUID).containsShape(CStrokeController.strokes.get(checkUUID).getPathReference());
			//return CStrokeController.strokes.get(checkUUID).isContainedInPath(CGroupController.groupdb.get(containerUUID).getPathReference());
	}
	
	public static boolean group_contains_group(final long containerUUID, final long checkUUID)
	{
		if(!groupdb.containsKey(containerUUID) || !groupdb.containsKey(checkUUID)){return false;}
		
		return CGroupController.groupdb.get(containerUUID).containsShape(groupdb.get(checkUUID).getPathReference());
//		return group_contains_path(containerUUID, CGroupController.groupdb.get(checkUUID).getPathReference() );
	}
	
	public static boolean group_contains_shape(final long containerUUID, Shape shape)
	{
		if(!groupdb.containsKey(containerUUID)){return false;}
		
		return CGroupController.groupdb.get(containerUUID).containsShape(shape);
//		return group_contains_path(containerUUID, CGroupController.groups.get(checkUUID).getPathReference() );
	}
	
	/**
	 * Check to see if the requested group contains the entirety of the polygon
	 * @param containerUUID
	 * @param polygon
	 * @return
	 */
//	public static boolean group_contains_path(final long containerUUID, GeneralPath path)
//	{
//		if(!groupdb.containsKey(containerUUID)){return false;}
//		
//		Polygon polygon = Geometry.getPolyFromPath(path.getPathIterator(null));
//		GeneralPath containerGroup = CGroupController.groupdb.get(containerUUID).getPathReference();
//		for(int i=0;i<polygon.npoints;i++)
//		{
//			if (!containerGroup.contains(new Point(polygon.xpoints[i], polygon.ypoints[i])))
//			{
//				return false;
//			}
//		}
//		return true;
//	}
	
	/**
	 * Check to see if the requested group contains the entirety of the polygon
	 * @param containerUUID
	 * @param polygon
	 * @return
	 */
//	public static boolean group_contains_polygon(final long containerUUID, Polygon polygon)
//	{
//		if(!groupdb.containsKey(containerUUID)){return false;}
//		
//		
//		Polygon containerGroupPoints = CGroupController.groupdb.get(containerUUID).getPolygon();
//		for(int i=0;i<polygon.npoints;i++)
//		{
//			if(!PolygonUtils.insidePoly(containerGroupPoints, new Point(polygon.xpoints[i], polygon.ypoints[i])))
//			{
//				return false;
//			}
//		}
//		return true;
//	}


	public static void makeRectangle(long guuid, int x, int y, int width, int height) {
		no_notify_make_rectangle(guuid, x, y, width, height);
		Networking.send(NetworkCommand.GROUP_MAKE_RECTANGLE, guuid, x, y, width, height);
	}
	
	public static void no_notify_make_rectangle(long guuid, int x, int y, int width, int height) {
		if (!exists(guuid))
			return;
		
		CGroup group = CGroupController.groupdb.get(guuid);
		if (!group.isPermanent())
			CGroupController.no_notify_set_permanent(guuid, true);
		
//		Rectangle rect = group.getBoundsOfContents();
		Rectangle rect = new Rectangle(x, y, width, height);
		
		group.setShapeToRoundedRectangle(rect);
		
		group.repaint();

	}
	
//	public static void shrink_to_contents(long guuid) {
//		no_notify_set_permanent(guuid, true);
//		no_notify_shrink_to_contents(guuid);
////		Networking.send(NetworkCommand.GROUP_SHRINK_TO_CONTENTS, guuid);
//	}
//	
//	public static void no_notify_shrink_to_contents(long guuid)
//	{
//		if (groupdb.containsKey(guuid))
//		{
//		CGroupController.no_notify_set_permanent(guuid, true);
//		CGroupController.groupdb.get(guuid).shrinkToContents();
//		CGroupController.groupdb.get(guuid).repaint();
//		}
//		else
//			logger.warn("Attempting to shrink to contents on non-existing scrap: " + guuid + " !");
//	}

	public static void shrinkToConvexHull(long guuid) {
		no_notify_shrink_to_convex_hull(guuid);
		//Networking.
		
	}
	
	public static void no_notify_shrink_to_convex_hull(long guuid)
	{
		CGroupController.groupdb.get(guuid).shrinkToConvexHull();
	}

	public static void create_image_group(long uuid, long cuid, long puid, String imgURL, int imgX, int imgY, int imgW, int imgH)
	{
		
		no_notify_create_image_group(uuid, cuid, puid, imgURL, imgX, imgY, imgW, imgH);
		
		Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_IMAGE_LOAD, uuid, cuid, puid, imgURL, imgX, imgY, imgW, imgH, true, false, 0.0d, 1.0d, 1.0d));
	}

	public static void no_notify_create_image_group(long uuid, long cuid, long puid, String imgURL, int imgX, int imgY, int imgW, int imgH) {
		// TODO Auto-generated method stub
		//taken from start(...)
		
		if (groupdb.containsKey(uuid))
			no_notify_delete(uuid);
		
		groupdb.put(uuid, new CGroupImage(uuid, cuid, puid, imgURL, imgX, imgY, imgW, imgH));		
		CCanvasController.canvasdb.get(cuid).addChildGroup(uuid);		
		CCanvasController.canvasdb.get(cuid).getLayer().addChild(groupdb.get(uuid));		
		groupdb.get(uuid).drawPermTemp(true);
		CGroupController.no_notify_finish(uuid, false);
		
		//set to the same size as the screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		double scale = (dim.getHeight() - 20)/imgH;
//		groupdb.get(uuid).scale(scale, scale);
		
		//move to origin
//		this.moveTo(0, 0);
		
		
	}



	
	public static void no_notify_rotate(long uuid, double theta) {
		// If we don't know wtf this UUID is for, then just eject
		if(!exists(uuid))
		{
			logger.warn("ROTATE for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).rotate(theta);
	}
	


	public static void no_notify_scale(long uuid, double scaleX, double scaleY) 
	{
		// If we don't know wtf this UUID is for, then just eject
		if(!exists(uuid))
		{
			logger.warn("SCALE for non-existant group "+uuid);
			return;
		}
		
		groupdb.get(uuid).scale(scaleX, scaleY);
	}
	
	public static void no_notify_create_text_scrap(long uuid, long cuuid, String text, int x, int y)
	{

		CGroupController.no_notify_start(uuid, cuuid, 0l, true);
		CGroupController.setCurrentUUID(uuid);
		CGroupController.no_notify_append(uuid, x, y);
		CGroupController.no_notify_set_text(uuid, text);
		CGroupController.no_notify_finish(uuid, false);
		CGroupController.no_notify_set_permanent(uuid, true);
		Rectangle rect = groupdb.get(uuid).getBoundsOfContents();
		CGroupController.no_notify_make_rectangle(uuid, rect.x, rect.y, rect.width, rect.height);
		CGroupController.recheck_parent(uuid);
//		CGroupController.no_notify_shrink_to_contents(uuid);
	}
	
	public static long get_smallest_containing_group_for_point(long canvas_uuid, Point p)
	{
		long[] uuids = CCanvasController.canvasdb.get(canvas_uuid).getChildGroups();
		
		long group_uuid = 0L;
		double group_area = Double.MAX_VALUE;
		CGroup temp;
		
		if(uuids.length>0)
		{
			for(int i=0;i<uuids.length;i++)
			{
				temp = CGroupController.groupdb.get(uuids[i]);
				if (temp == null || temp.getPathReference() == null)
					continue;
				if( (temp.getArea()< group_area) && temp.getPathReference().contains(p)
						&& (temp.getParentUUID() == 0l || !(CGroupController.groupdb.get(temp.getParentUUID()) instanceof CGroupDecorator)))
				{
					group_area = CGroupController.groupdb.get(uuids[i]).getArea();
					group_uuid = uuids[i];
				}
			}
		}
		return group_uuid;
	}
	
	public static boolean canParentChild(long potentialParent, long child, int x, int y)
	{
		if (!exists(potentialParent) || !exists(child))
			return false;
		
		return groupdb.get(potentialParent).canParentChild(child, x, y);
	}
	
	public static void no_notify_move_end(final long uuid, int x, int y)
	{
		if(!exists(uuid)){return;}
		
		groupdb.get(uuid).recheckParentAfterMove(x, y);
		
	}
	
	public static void move_end(long uuid, int x, int y) {		no_notify_move_end(uuid, x, y);
		CGroupController.groupdb.get(CGroupController.groupdb.get(uuid).getTopmostParent()).moveInFrontOf(null);
		Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_MOVE_END, uuid, x, y));
	}


	public static void move_start(long guuid) {
		no_notify_move_start(guuid);
		Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_MOVE_START, guuid));
	}
	
	public static void no_notify_move_start(long guuid) {
		no_notify_set_parent(guuid, 0);
		CGroupController.groupdb.get(guuid).moveToFront();
		CGroupController.groupdb.get(guuid).moveInFrontOf(null);
		CalicoInputManager.group = guuid;
	}
	
	public static boolean group_is_ancestor_of(long ancestor, long group)
	{
		long uuid = group, parent;
		
		while (exists(uuid) && (parent = CGroupController.groupdb.get(uuid).getParentUUID()) != 0l)
		{
			 if (parent == ancestor)
				 return true;
			 uuid = parent;
		}
		
		return false;
	}
	
	public static void sendRandomTestPacket()
	{
		if (Calico.numUUIDs() < 1)
			return;
		
		Random r = new Random();
		
		int type = r.nextInt(100);
		
		long[] canvases = CCanvasController.getCanvasIDList();
		if (canvases.length < 10)
			return;
		long cuid = canvases[r.nextInt(canvases.length-1)];
		
		if (type < 10)
		{
//			PalettePlugin.sendRandomTestPacket();
		}
		else if (type < 60)
		{
			int x = r.nextInt(1500) + 100;
			int y = r.nextInt(1500) + 100;
			
			int width = r.nextInt(500) + 300;
			int height = r.nextInt(500) + 100;
			
			long uuid = Calico.uuid();
			
			
			no_notify_start(uuid, cuid, 0, true);
			no_notify_make_rectangle(uuid, x, y, width, height);
			finish(uuid, true);
		}
		else
		{
			long uuid = Calico.uuid();
			int x = r.nextInt(1500) + 100;
			int y = r.nextInt(1500) + 100;
			int x2 = r.nextInt(1500) + 100;
			int y2 = r.nextInt(1500) + 100;
			CStrokeController.no_notify_start(uuid, cuid, 0l, CalicoDataStore.PenColor, CalicoDataStore.PenThickness);
			CStrokeController.append(uuid, x, y);
			CStrokeController.append(uuid, x2, y2);
			CStrokeController.finish(uuid);
		}
		
	}
	
}
