package calico.components.menus.buttons;

import java.awt.Rectangle;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.menus.CanvasTextButton;
import calico.inputhandlers.InputEventInfo;

public class ExitButton extends CanvasTextButton
{
	public ExitButton()
	{
		this(0L);
	}
	
	public ExitButton(long cuid)
	{
		super(cuid);
	}
	
	public void actionMouseClicked(InputEventInfo event, Rectangle boundingBox)
	{
		if (event.getAction() == InputEventInfo.ACTION_PRESSED)
		{
			isPressed = true;
		}
		else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
		{
			isPressed = false;

			int userOption = JOptionPane.showConfirmDialog(CalicoDataStore.calicoObj, "Would you like to exit Calico?", "Exit requested!",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

			if (userOption == JOptionPane.YES_OPTION)
			{
				Calico.exit();
			}
		}
	}
}
