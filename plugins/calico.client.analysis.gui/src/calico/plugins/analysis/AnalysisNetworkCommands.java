package calico.plugins.analysis;

/**
 * This class contains the commands accepted by the plugin
 * The list of commands should be managed by the Analysis Plugin
 * @author motta
 *
 */
public class AnalysisNetworkCommands {
	
	//command example
	//public static final int PALETTE_LOAD = 7701;
	
	public static final int ANALYSIS_ADD_SERVICETIME_TO_ACTIVITY_NODE = 7802;
	public static final int ANALYSIS_CREATE_ACTIVITY_NODE = 7803;
	public static final int ANALYSIS_CREATE_DECISION_NODE = 7804;
	public static final int ANALYSIS_CREATE_FINAL_NODE= 7805;
	public static final int ANALYSIS_CREATE_FORK_NODE=7806;
	public static final int ANALYSIS_CREATE_INITIAL_NODE=7807;
	public static final int ANALYSIS_ADD_PROBABILITY_TO_DECISION_NODE = 7808;
	public static final int ANALYSIS_RUN_ANALYSIS = 7809;
	
	public static final int ANALYSIS_ACTIVITY_NODE_LOAD = 7810;

}
