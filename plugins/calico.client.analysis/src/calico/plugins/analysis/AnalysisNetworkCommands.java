package calico.plugins.analysis;

/**
 * This class contains the commands accepted by the plugin
 * The list of commands should be managed by the Analysis Plugin
 * @author motta
 *
 */
public class AnalysisNetworkCommands {
	
	/****************************
	 * USER INTERFACE COMMANDS	*
	 ****************************/
	public static final int ANALYSIS_ADD_SERVICETIME_TO_ACTIVITY_NODE = 7802;
	public static final int ANALYSIS_CREATE_ACTIVITY_NODE_TYPE = 7803;
	public static final int ANALYSIS_ADD_PROBABILITY_TO_DECISION_NODE = 7808;
	public static final int ANALYSIS_RUN_ANALYSIS = 7809;

	/****************************
	 * SCRAP TRANSFERS COMMANDS	*
	 ****************************/	
	public static final int ANALYSIS_ACTIVITY_NODE_LOAD = 7810;
	public static final int ANALYSIS_CONTROL_FLOW_LOAD = 7811;

}
