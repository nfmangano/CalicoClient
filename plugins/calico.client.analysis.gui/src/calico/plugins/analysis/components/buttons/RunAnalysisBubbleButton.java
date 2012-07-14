package calico.plugins.analysis.components.buttons;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSlider;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.*;
import calico.inputhandlers.*;
import calico.networking.*;
import calico.networking.netstuff.*;
import calico.plugins.analysis.AnalysisNetworkCommands;
import calico.plugins.analysis.AnalysisPlugin;
import calico.plugins.analysis.components.activitydiagram.ActivityNode;
import calico.plugins.analysis.controllers.ADAnalysisController;
import calico.plugins.analysis.controllers.ADBubbleMenuController;

public class RunAnalysisBubbleButton extends PieMenuButton {
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
	private boolean isActive = false;

	public RunAnalysisBubbleButton(long uuid) {
		super("plugins.analysis.runanalysis");

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
		
		JFrame parent = new JFrame();
	    JOptionPane optionPane = new JOptionPane();
	    JSlider slider = JSliderOnJOptionPane.getSliderAnalysis(optionPane);
	    optionPane.setMessage(new Object[] { "Select a value: ", slider });
	    optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
	    optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
	    JDialog dialog = optionPane.createDialog(parent, "My Slider");
	    dialog.setVisible(true);

	    Integer response=(Integer)optionPane.getInputValue();
		
		AnalysisPlugin.UI_send_command(AnalysisNetworkCommands.ANALYSIS_RUN_ANALYSIS, uuid, response.doubleValue());
		isActive = false;
	}

}