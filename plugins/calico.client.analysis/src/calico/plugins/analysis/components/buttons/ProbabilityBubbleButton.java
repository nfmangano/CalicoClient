package calico.plugins.analysis.components.buttons;

import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSlider;

import calico.components.piemenu.PieMenuButton;
import calico.controllers.*;
import calico.inputhandlers.*;
import calico.plugins.analysis.AnalysisNetworkCommands;
import calico.plugins.analysis.AnalysisPlugin;
import calico.plugins.analysis.components.activitydiagram.ControlFlow;

public class ProbabilityBubbleButton extends PieMenuButton {
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
	private boolean isActive = false;

	public ProbabilityBubbleButton(long uuid) {
		super(calico.plugins.analysis.iconsets.CalicoIconManager.getIconImage("analysis.service"));
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
		
		ControlFlow cf=(ControlFlow)CConnectorController.connectors.get(uuid);
		int sliderInitValue=50;
		if(cf.getText()!=null && !cf.getText().equals("")) sliderInitValue=(int)(Double.parseDouble(cf.getText())*100);
		
		JFrame parent = new JFrame();
	    JOptionPane optionPane = new JOptionPane();
	    JSlider slider = JSliderOnJOptionPane.getSliderDecision(optionPane, sliderInitValue);
	    optionPane.setMessage(new Object[] { "Select a value: ", slider });
	    optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
	    optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
	    JDialog dialog = optionPane.createDialog(parent, "My Slider");
	    dialog.setVisible(true);

	    Object o=optionPane.getInputValue();
	    Double response=(Double)optionPane.getInputValue();

		if (response != null) {
			AnalysisPlugin.UI_send_command(AnalysisNetworkCommands.ANALYSIS_ADD_PROBABILITY_TO_DECISION_NODE, uuid, response);
		}
		isActive = false;
	}
	
	@Override
	public Point getPreferredPosition()
	{
		Point p = new Point(CConnectorController.connectors.get(uuid).getTail());
		p.x = p.x - 0;
		p.y = p.y - 60;
		return p;
	}
	

}
