package calico.plugins.analysis.components.buttons;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.*;
import calico.inputhandlers.*;
import calico.networking.*;
import calico.networking.netstuff.*;
import calico.plugins.analysis.AnalysisPlugin;
import calico.plugins.analysis.components.activitydiagram.ActivityNode;
import calico.plugins.analysis.controllers.ADBubbleMenuController;

public class AddActivityToComponentBubbleButton extends PieMenuButton {
	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_MENU;
	private boolean isActive = false;

	public AddActivityToComponentBubbleButton(long uuid) {
		super("plugins.analysis.plus");

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

		//Aggiungere l'attivita' al component
		ADBubbleMenuController.add_activity_to_component(uuid);
		
		isActive = false;
	}

}