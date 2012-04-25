package calico.components.menus;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.menus.buttons.EmailGridButton;
import calico.inputhandlers.InputEventInfo;

public class GridBottomMenuBar extends CanvasGenericMenuBar
{
	private static final long serialVersionUID = 1L;

	private long cuid = 0L;

	private static ObjectArrayList<Class<?>> externalButtons = new ObjectArrayList<Class<?>>();
	private static ObjectArrayList<Class<?>> externalButtons_rightAligned = new ObjectArrayList<Class<?>>();

	public GridBottomMenuBar(long c)
	{
		super(CanvasGenericMenuBar.POSITION_BOTTOM);
		Calico.logger.debug("loaded generic menu bar for GridBottomMenuBar");
		cuid = c;

		addCap(CanvasGenericMenuBar.ALIGN_START);

		// addIcon(new GridViewportChangeButton(GridViewportChangeButton.BUT_MINUS));
		// addIcon(new GridViewportChangeButton(GridViewportChangeButton.BUT_PLUS));

		// addSpacer();
		//
		// addIcon(new GridSessionMenuButton(1));
		// addIcon(new GridSessionMenuButton(0));

		// addSpacer();
		//
		// addText(
		// "view clients",
		// new Font("Verdana", Font.BOLD, 12),
		// new CanvasTextButton(cuid) {
		// public void actionMouseClicked(Rectangle boundingBox) {
		// CalicoDataStore.gridObject.drawClientList(boundingBox);
		// }
		// }
		// );

		// addSpacer();

		// addText(
		// "sessions",
		// new Font("Verdana", Font.BOLD, 12),
		// new CanvasTextButton(cuid) {
		// public void actionMouseClicked(Rectangle boundingBox) {
		// Calico.showSessionPopup();
		// }
		// }
		// );

		// addSpacer();

		addTextEndAligned("  Exit  ", new Font("Verdana", Font.BOLD, 12), new CanvasTextButton(cuid) {
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
		});
		addSpacer(ALIGN_END);
		addIconRightAligned(new EmailGridButton());

		try
		{
			for (Class<?> button : externalButtons)
			{
				addSpacer();
				addIcon((CanvasMenuButton) button.getConstructor(long.class).newInstance(cuid));
			}

			for (Class<?> button : externalButtons_rightAligned)
			{
				addSpacer(ALIGN_END);
				addIconRightAligned((CanvasMenuButton) button.getConstructor(long.class).newInstance(cuid));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// addCap();

	}

	public static void addMenuButton(Class<?> button)
	{
		externalButtons.add(button);
	}

	public static void addMenuButtonRightAligned(Class<?> button)
	{
		externalButtons_rightAligned.add(button);
	}

}
