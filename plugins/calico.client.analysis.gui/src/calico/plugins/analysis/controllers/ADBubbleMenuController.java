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
	 * BEHAVIORS OF THE CONTROLLER
	 * 
	 * Those behaviors are invoked by the analysis
	 * plugins in response to network commands
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
		cf.setProbability(probability);
	}

}
