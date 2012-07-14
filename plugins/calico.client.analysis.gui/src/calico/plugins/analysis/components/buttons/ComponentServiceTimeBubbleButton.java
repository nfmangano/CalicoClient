package calico.plugins.analysis.components.buttons;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSlider;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.*;
import calico.events.CalicoEventHandler;
import calico.inputhandlers.*;
import calico.networking.*;
import calico.networking.netstuff.*;
import calico.plugins.analysis.AnalysisNetworkCommands;
import calico.plugins.analysis.AnalysisPlugin;
import calico.plugins.analysis.components.activitydiagram.ActivityNode;
import calico.plugins.analysis.controllers.ADBubbleMenuController;
import calico.plugins.analysis.AnalysisPlugin;

public class ComponentServiceTimeBubbleButton extends PieMenuButton {
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
	private boolean isActive = false;

	public ComponentServiceTimeBubbleButton(long uuid) {
		super("plugins.analysis.service");

		this.uuid = uuid;

	}

	public void onPressed(InputEventInfo ev) {
		if (!CGroupController.exists(this.uuid) || isActive) {
			return;
		}

		isActive = true;
	}

	public void onReleased(InputEventInfo ev) {
		ev.stop();
		
		ActivityNode an=(ActivityNode)CGroupController.groupdb.get(uuid);
		
		JFrame parent = new JFrame();
	    JOptionPane optionPane = new JOptionPane();
	    JSlider slider = JSliderOnJOptionPane.getSliderActivity(optionPane, (int)an.getResponseTime());
	    optionPane.setMessage(new Object[] { "Select a value: ", slider });
	    optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
	    optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
	    JDialog dialog = optionPane.createDialog(parent, "My Slider");
	    dialog.setVisible(true);

	    Integer response=(Integer)optionPane.getInputValue();
	    
		if (response != null) {
			//don't do this
			//ADBubbleMenuController.add_servicetime(uuid, response.doubleValue());
			
			//do this
			AnalysisPlugin.UI_add_service_time(uuid, response.doubleValue());
		}
		isActive = false;
	}

}
