package calico.plugins.analysis.controllers;

import java.awt.Point;
import java.awt.Polygon;

import calico.components.CGroup;
import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.analysis.components.activitydiagram.ActivityNode;
import calico.plugins.analysis.components.activitydiagram.ControlFlow;
import calico.plugins.analysis.components.activitydiagram.DecisionNode;
import calico.plugins.analysis.components.componentdiagram.Component;

/**
 * This Activity Diagram Bubble Menu Controller is in charge of managing all the actions
 * related to feeding the data of the different activity diagram 
 * elements
 * @author motta
 *
 */
public class ADBubbleMenuController {

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
	
	/**
	 * Adds the response time to an activity node with id uuid
	 * @param uuid 
	 * @param responseTime
	 */	
	public static void add_servicetime(long uuid, double responseTime){
		ActivityNode an=(ActivityNode)CGroupController.groupdb.get(uuid);
		an.setText(String.valueOf(responseTime));
		an.setResponseTime(responseTime);
	}
	
	/**
	 * Adds the probability to a decision node when a certain control flow with id cuuid is tapped
	 * @param cuuid 
	 * @param probability
	 */
	public static void add_probability(long cuuid, double probability){
		ControlFlow cf=(ControlFlow)CConnectorController.connectors.get(cuuid);
		cf.setText(Double.toString(probability));
		if(CGroupController.groupdb.get(cf.getIncomingNode().getUUID()) instanceof DecisionNode){
			DecisionNode dn=(DecisionNode) CGroupController.groupdb.get(cf.getIncomingNode().getUUID());
			dn.addProbabilityPath(cuuid, probability);
		}
	}

	public static void add_activity_to_component(long uuid) {
		Polygon p=CStrokeController.strokes.get(uuid).getPolygon();

		//This is very bad, I will find a better place
		//Find the activity node containing the first point of the (temp) stroke
		CGroup g1=null;
		//Find the component containing the last point of the (temp) stroke
		CGroup g2=null;
		Point firstpoint=new Point();
		firstpoint.x=p.xpoints[0];
		firstpoint.y=p.ypoints[0];
		Point lastpoint=new Point();
		lastpoint.x=p.xpoints[p.npoints-1];
		lastpoint.y=p.ypoints[p.npoints-1];
		
		long first_uuid=CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), firstpoint);
		long second_uuid=CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), lastpoint);
		
		if(CGroupController.groupdb.get(first_uuid) instanceof ActivityNode && CGroupController.groupdb.get(second_uuid) instanceof Component){
			((Component)CGroupController.groupdb.get(second_uuid)).addActivity(first_uuid);
		}
		CStrokeController.strokes.get(uuid).delete();
	}
}
