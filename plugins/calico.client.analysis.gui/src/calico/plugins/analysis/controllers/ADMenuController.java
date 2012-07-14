package calico.plugins.analysis.controllers;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.lang.reflect.InvocationTargetException;

import sl.shapes.RegularPolygon;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoDraw;
import calico.components.CGroup;
import calico.components.bubblemenu.BubbleMenu;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.networking.Networking;
import calico.networking.netstuff.NetworkCommand;
import calico.plugins.analysis.components.activitydiagram.ActivityNode;
import calico.plugins.analysis.components.activitydiagram.DecisionNode;
import calico.plugins.analysis.components.activitydiagram.FinalNode;
import calico.plugins.analysis.components.activitydiagram.ForkNode;
import calico.plugins.analysis.components.activitydiagram.InitialNode;
import calico.plugins.analysis.components.buttons.ComponentServiceTimeBubbleButton;
import calico.plugins.analysis.components.buttons.CreateActivityNodeButton;
import calico.plugins.analysis.components.componentdiagram.Component;
import calico.plugins.analysis.utils.ActivityShape;
import calico.utils.Geometry;
import edu.umd.cs.piccolo.PCamera;

/**
 * The Activity Diagram Menu Controller is in charge of managing the actions related to 
 * the bottom right analysis menu for the creation of the analysis elements
 * @author motta
 *
 */
public class ADMenuController {
	
	public static ADMenuController getInstance()
	{
		return INSTANCE;
	}

	public static void initialize()
	{
		INSTANCE = new ADMenuController();
	}

	private static ADMenuController INSTANCE;
	
	/*************************************************
	 * GUI FIELDS
	 * 
	 * The elements of the view used by
	 * this controller
	 *************************************************/
	//The button of the menu at the bottom right
	//private CreateActivityButton analysisButton;
	
	/*************************************************
	 * BEHAVIORS OF THE CONTROLLER
	 * 
	 * The summary of the actions that can be
	 * invoked by the view elements
	 *************************************************/	
	public static void create_activity_node(){
		int x=CalicoDataStore.ScreenWidth / 3;
		int y=CalicoDataStore.ScreenHeight / 3;
		long new_uuid = Calico.uuid();
		
		no_notify_create_activitydiagram_node(new_uuid, CCanvasController.getCurrentUUID(), x, y, ActivityNode.class, ActivityShape.ACTIVITY, "activity");

	}
	
	public static void create_diamond_node(){
		int x=CalicoDataStore.ScreenWidth / 3;
		int y=CalicoDataStore.ScreenHeight / 3;
		long new_uuid = Calico.uuid();
		no_notify_create_activitydiagram_node(new_uuid, CCanvasController.getCurrentUUID(), x, y, DecisionNode.class, ActivityShape.DECISION, "");
		//TODO: make it server side
		Networking.send(NetworkCommand.GROUP_CREATE_TEXT_GROUP, new_uuid, CCanvasController.getCurrentUUID(), "", x, y);
	}
	
	public static void create_initial_node(){
		int x=CalicoDataStore.ScreenWidth / 3;
		int y=CalicoDataStore.ScreenHeight / 3;
		long new_uuid = Calico.uuid();
		no_notify_create_activitydiagram_node(new_uuid, CCanvasController.getCurrentUUID(), x, y, InitialNode.class, ActivityShape.INITIALNODE, "");
		//TODO: make it server side
		Networking.send(NetworkCommand.GROUP_CREATE_TEXT_GROUP, new_uuid, CCanvasController.getCurrentUUID(), "", x, y);
	}		
	
	public static void create_final_node(){
		int x=CalicoDataStore.ScreenWidth / 3;
		int y=CalicoDataStore.ScreenHeight / 3;
		long new_uuid = Calico.uuid();
		no_notify_create_activitydiagram_node(new_uuid, CCanvasController.getCurrentUUID(), x, y, FinalNode.class, ActivityShape.FINALNODE, "");
		//TODO: make it server side
		Networking.send(NetworkCommand.GROUP_CREATE_TEXT_GROUP, new_uuid, CCanvasController.getCurrentUUID(), "", x, y);
	}	

	public static void create_fork_node() {
		int x=CalicoDataStore.ScreenWidth / 3;
		int y=CalicoDataStore.ScreenHeight / 3;
		long new_uuid = Calico.uuid();
		//I reuse this method even if the name is not the best one because I am creating a component (component diagram)
		no_notify_create_activitydiagram_node(new_uuid, CCanvasController.getCurrentUUID(), x, y, ForkNode.class, ActivityShape.FORK, "");
		//TODO: make it server side
		Networking.send(NetworkCommand.GROUP_CREATE_TEXT_GROUP, new_uuid, CCanvasController.getCurrentUUID(), "", x, y);
	}
	
	public static void create_component_node() {
		int x=CalicoDataStore.ScreenWidth / 3;
		int y=CalicoDataStore.ScreenHeight / 3;
		long new_uuid = Calico.uuid();
		//I reuse this method even if the name is not the best one because I am creating a component (component diagram)
		no_notify_create_activitydiagram_node(new_uuid, CCanvasController.getCurrentUUID(), x, y, Component.class, ActivityShape.ACTIVITY, "component");
		//TODO: make it server side
		Networking.send(NetworkCommand.GROUP_CREATE_TEXT_GROUP, new_uuid, CCanvasController.getCurrentUUID(), "", x, y);
	}
	
	/*************************************************
	 * NO NOTIFY METHODS
	 * 
	 * Methods which use the CGroupController
	 * to create the actual nodes
	 *************************************************/		
	//Create an activity diagram node {decision, activity, etc...}  inside the cuuid canvas
	//at position x,y with type activity {es: ActivityNode.class} and shape as {es: ActivityShape.ACTIVITY}
	public static void no_notify_create_activitydiagram_node(long uuid, long cuuid, int x, int y, Class<?> activity, ActivityShape as, String optText){

		Polygon p = get_group_shape(uuid, as, x,y);

		no_notify_create_activitydiagram_node(uuid, cuuid, p, activity, as, optText);
	}
	
	public static void no_notify_create_activitydiagram_node(long uuid, long cuuid, Polygon p, Class<?> activity, ActivityShape as, String optText){
		no_notify_start(uuid, cuuid, 0l, true, activity);
		CGroupController.setCurrentUUID(uuid);
		create_custom_shape(uuid, p);
		//Set the optional text to identify the scrap
		CGroupController.no_notify_set_text(uuid, optText);
		CGroupController.no_notify_finish(uuid, false, false, true);
		CGroupController.no_notify_set_permanent(uuid, true);
		CGroupController.recheck_parent(uuid);
	}
	
	
	/*************************************************
	 * UTILITY METHODS
	 *************************************************/		
	//Starts the creation of any of the activity diagram scrap
	public static void no_notify_start(long uuid, long cuid, long puid, boolean isperm, Class<?> activity)
	{
		if (!CCanvasController.exists(cuid))
			return;
		if(CGroupController.exists(uuid))
		{
			CGroupController.logger.debug("Need to delete group "+uuid);
			// WHOAA WE NEED TO DELETE THIS SHIT
			//CCanvasController.canvasdb.get(cuid).getLayer().removeChild(groupdb.get(uuid));
			CalicoDraw.removeChildFromNode(CCanvasController.canvasdb.get(cuid).getLayer(), CGroupController.groupdb.get(uuid));
			//CCanvasController.canvasdb.get(cuid).getCamera().repaint();
		}
		
		// Add to the GroupDB
		try {
			CGroupController.groupdb.put(uuid, (CGroup)activity.getConstructor(long.class,long.class,long.class).newInstance(uuid, cuid, puid));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CCanvasController.canvasdb.get(cuid).addChildGroup(uuid);
		//CCanvasController.canvasdb.get(cuid).getLayer().addChild(groupdb.get(uuid));
		CalicoDraw.addChildToNode(CCanvasController.canvasdb.get(cuid).getLayer(), CGroupController.groupdb.get(uuid));
		CGroupController.groupdb.get(uuid).drawPermTemp(true);
		//CCanvasController.canvasdb.get(cuid).repaint();
	}

	//The scrap with id uuid will have the shape defined by as at position x,y
	public static Polygon get_group_shape(long uuid, ActivityShape as, int x, int y){
		//Getting the shape of the polygon and centering it in x,y
		Polygon p= as.getShape(x,y);
		
		return p;
	}
	
	//Add the points defined in p to the scrap with id uuid
	public static void create_custom_shape(long uuid, Polygon p){
		for (int i = 0; i < p.npoints; i++)
		{
			CGroupController.no_notify_append(uuid, p.xpoints[i], p.ypoints[i]);
			CGroupController.no_notify_append(uuid, p.xpoints[i], p.ypoints[i]);
			CGroupController.no_notify_append(uuid, p.xpoints[i], p.ypoints[i]);
		}
	}

}
