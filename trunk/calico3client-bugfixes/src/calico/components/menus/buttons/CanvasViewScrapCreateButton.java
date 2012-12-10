package calico.components.menus.buttons;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

public class CanvasViewScrapCreateButton extends CanvasMenuButton {

	long cuid = 0L;

	public CanvasViewScrapCreateButton(long c)
	{
		super();
		this.cuid = c;
		iconString = "group.text";
		try
		{
			setImage(CalicoIconManager.getIconImage(iconString));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void actionMouseClicked(InputEventInfo event)
	{
		if (event.getAction() == InputEventInfo.ACTION_PRESSED)
		{
			super.onMouseDown();
		}
		else if (event.getAction() == InputEventInfo.ACTION_RELEASED && isPressed)
		{
			String response = JOptionPane.showInputDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent(),
					  "Create Scrap with Text",
					  "Please enter text",
					  JOptionPane.QUESTION_MESSAGE);

			long new_uuid = 0l;
			if (response != null)
			{
				long targetCanvas = (new Long(response)).longValue();
				if (CCanvasController.canvasdb.containsKey(targetCanvas))
				{
					new_uuid = Calico.uuid();
					CGroupController.create_canvas_view_scrap(new_uuid, CCanvasController.getCurrentUUID(), targetCanvas);
//					CGroupController.create_text_scrap(new_uuid, CCanvasController.getCurrentUUID(), response, CalicoDataStore.ScreenWidth / 3, CalicoDataStore.ScreenHeight / 3);
				}
			}
	//		if (this.uuid != 0l && new_uuid != 0l && CGroupController.groupdb.get(new_uuid).getParentUUID() == 0l)
	//		{
	//			CGroupController.move_start(new_uuid);
	//			CGroupController.move_end(new_uuid, ev.getX(), ev.getY());
	//		}
			super.onMouseUp();
		}
	}

}