package calico.plugins.analysis;

import java.awt.Polygon;

import org.apache.log4j.Logger;

import calico.CalicoOptions;
import calico.components.CGroup;
import calico.components.bubblemenu.BubbleMenuButton;
import calico.components.menus.CanvasStatusBar;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.events.CalicoEventHandler;
import calico.events.CalicoEventListener;
import calico.networking.Networking;
import calico.networking.PacketHandler;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.plugins.CalicoPlugin;
import calico.plugins.analysis.components.activitydiagram.ActivityNode;
import calico.plugins.analysis.components.buttons.ComponentServiceTimeBubbleButton;
import calico.plugins.analysis.components.buttons.CreateActivityNodeButton;
import calico.plugins.analysis.components.buttons.CreateComponentButton;
import calico.plugins.analysis.components.buttons.CreateDecisionNodeButton;
import calico.plugins.analysis.components.buttons.CreateFinalNodeButton;
import calico.plugins.analysis.components.buttons.CreateForkNodeButton;
import calico.plugins.analysis.components.buttons.CreateInitialNodeButton;
import calico.plugins.analysis.components.buttons.RunAnalysisBubbleButton;
import calico.plugins.analysis.controllers.ADBubbleMenuController;
import calico.plugins.analysis.controllers.ADMenuController;
import calico.plugins.analysis.iconsets.CalicoIconManager;
import calico.plugins.analysis.utils.ActivityShape;
import edu.umd.cs.piccolo.PCamera;


/*
 * The entry point to load the plugin and the main logic is here
 * Contains all the static references to the graphical objects
 * and it is in care of modifying how they appear
 */
public class AnalysisPlugin extends CalicoPlugin implements CalicoEventListener {

	//the logger for this plugin
	public static Logger logger = Logger.getLogger(AnalysisPlugin.class.getName());
	
	public AnalysisPlugin() {
		super();
		PluginInfo.name = "Analysis";
		calico.plugins.analysis.iconsets.CalicoIconManager.setIconTheme(this.getClass(), CalicoOptions.core.icontheme);
	}


	/*************************************************
	 * INHERITED
	 *************************************************/
	
	@Override
	/**
	 * Plugin specific commands can be defined
	 * in the analysis network commands class
	 */
	public void handleCalicoEvent(int event, CalicoPacket p) {
		switch (event) {
			case NetworkCommand.VIEWING_SINGLE_CANVAS:
				VIEWING_SINGLE_CANVAS(p);
				break;	
			case AnalysisNetworkCommands.ANALYSIS_ADD_SERVICETIME_TO_ACTIVITY_NODE:
				ANALYSIS_ADD_SERVICETIME_TO_ACTIVITY_NODE(p);
				break;
			case AnalysisNetworkCommands.ANALYSIS_ACTIVITY_NODE_LOAD:
				ANALYSIS_ACTIVITY_NODE_LOAD(p);
				break;
		}
	}

	
	private void ANALYSIS_ACTIVITY_NODE_LOAD(CalicoPacket p) {
		p.rewind();
		p.getInt();
		//taken from PacketHandler.GROUP_LOAD()		
		long uuid = p.getLong();
		long cuid = p.getLong();
		long puid = p.getLong();
		boolean isperm = p.getBoolean();
		int count = p.getCharInt();
		
		if(count<=0)
		{
			return;
		}
		
		int[] xArr = new int[count], yArr = new int[count];
		for(int i=0;i<count;i++)
		{
			xArr[i] = p.getInt();
			yArr[i] = p.getInt();
		}
		boolean captureChildren = p.getBoolean();
		double rotation = p.getDouble();
		double scaleX = p.getDouble();
		double scaleY = p.getDouble();
		String text = p.getString();
		//begin analysis node
		
		Polygon poly = new Polygon(xArr, yArr, count);
		
		double servicetime=p.getDouble();
		ADMenuController.no_notify_create_activitydiagram_node(uuid, cuid, poly, ActivityNode.class, ActivityShape.ACTIVITY, "activity");
		ADBubbleMenuController.add_servicetime(uuid, servicetime);
	}

	/**
	 * Called by the User interface
	 * @param uuid
	 * @param servicetime
	 */
	public static void UI_add_service_time(long uuid, double servicetime){
		//Create the packet
		CalicoPacket p=CalicoPacket.getPacket(AnalysisNetworkCommands.ANALYSIS_ADD_SERVICETIME_TO_ACTIVITY_NODE, uuid, servicetime);
		//Send the packet locally
		PacketHandler.receive(p);
		//Send the packet to the network (server)
		Networking.send(p);	
	}
	
	private void ANALYSIS_ADD_SERVICETIME_TO_ACTIVITY_NODE(CalicoPacket p) {
		//CalicoPacket.getPacket(AnalysisNetworkCommands.ANALYSIS_ADD_SERVICETIME_TO_ACTIVITY_NODE, uuid, response.doubleValue())
		//Rewind the packet
		p.rewind();
		p.getInt();
		long uuid=p.getLong();
		double servicetime=p.getDouble();
		
		ADBubbleMenuController.add_servicetime(uuid, servicetime);
	}


	@Override
	public void onException(Exception arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPluginEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPluginStart() {
		//Add the analysis button to the status bar at the bottom
		CanvasStatusBar.addMenuButtonRightAligned(CreateActivityNodeButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(CreateDecisionNodeButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(CreateInitialNodeButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(CreateFinalNodeButton.class);
		CanvasStatusBar.addMenuButtonRightAligned(CreateForkNodeButton.class);
		//CanvasStatusBar.addMenuButtonRightAligned(CreateComponentButton.class);
		
		//Add an additional voice to the bubble menu
		//CGroup.registerPieMenuButton(SaveToPaletteButton.class);
		
		//Register to the events I am interested in 
		CalicoEventHandler.getInstance().addListener(NetworkCommand.VIEWING_SINGLE_CANVAS, this, CalicoEventHandler.PASSIVE_LISTENER);
		for (Integer event : this.getNetworkCommands())
			CalicoEventHandler.getInstance().addListener(event.intValue(), this, CalicoEventHandler.ACTION_PERFORMER_LISTENER);
		
		//Initialize the controllers
		ADMenuController.initialize();

	}

	@Override
	public Class<?> getNetworkCommandsClass() {
		return AnalysisNetworkCommands.class;
	}
	
	/*************************************************
	 * COMMANDS
	 *************************************************/
	
	private void VIEWING_SINGLE_CANVAS(CalicoPacket p) {
		p.rewind();
		p.getInt();
		long cuid = p.getLong();
		
		//If you have to do some actions
		//when the canvas is opened do it here (using one of your controllers)
		CGroup.registerPieMenuButton(ComponentServiceTimeBubbleButton.class);
		CGroup.registerPieMenuButton(RunAnalysisBubbleButton.class);
		//example: MenuController.getInstance().showMenu(cuid);
	}


}
