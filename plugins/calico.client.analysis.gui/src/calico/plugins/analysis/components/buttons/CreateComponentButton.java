package calico.plugins.analysis.components.buttons;

import javax.swing.SwingUtilities;

import calico.CalicoDraw;
import calico.components.menus.CanvasMenuButton;
import calico.inputhandlers.InputEventInfo;
import calico.plugins.analysis.AnalysisPlugin;
import calico.plugins.analysis.controllers.ADMenuController;
import calico.plugins.analysis.iconsets.CalicoIconManager;

public class CreateComponentButton extends CanvasMenuButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CreateComponentButton(long c) {
		super();
		cuid = c;

		this.iconString = "analysis.component";
		try {
			setImage(calico.plugins.analysis.iconsets.CalicoIconManager.getIconImage(iconString));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionMouseClicked(InputEventInfo event) {
		if (event.getAction() == InputEventInfo.ACTION_PRESSED) {
			super.onMouseDown();
		} else if (event.getAction() == InputEventInfo.ACTION_RELEASED
				&& isPressed) {
			ADMenuController.create_component_node();
			super.onMouseUp();
		}

	}
	
	public void highlight_on()
	{
		if (!isPressed)
		{
			isPressed = true;
			setSelected(true);
			final CanvasMenuButton tempButton = this;
			SwingUtilities.invokeLater(
					new Runnable() { public void run() { 
						double tempX = tempButton.getX();
						double tempY = tempButton.getY();
								
						setImage(CalicoIconManager.getIconImage(iconString));
						tempButton.setX(tempX);
						tempButton.setY(tempY);
					}});
			//CalicoDraw.setNodeX(this, tempX);
			//CalicoDraw.setNodeY(this, tempY);
			//this.repaintFrom(this.getBounds(), this);
			CalicoDraw.repaintNode(this);
		}
	}
	
	public void highlight_off()
	{
		if (isPressed)
		{
			isPressed = false;
			setSelected(false);
			final CanvasMenuButton tempButton = this;
			SwingUtilities.invokeLater(
					new Runnable() { public void run() { 
						double tempX = tempButton.getX();
						double tempY = tempButton.getY();
								
						setImage(CalicoIconManager.getIconImage(iconString));
						tempButton.setX(tempX);
						tempButton.setY(tempY);
					}});
			//CalicoDraw.setNodeX(this, tempX);
			//CalicoDraw.setNodeY(this, tempY);
			//this.repaintFrom(this.getBounds(), this);
			CalicoDraw.repaintNode(this);
		}
	}	
}
