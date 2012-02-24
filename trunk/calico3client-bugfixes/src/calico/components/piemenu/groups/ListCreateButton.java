package calico.components.piemenu.groups;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import calico.Calico;
import calico.CalicoUtils;
import calico.components.CGroup;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CGroupDecoratorController;
import calico.controllers.CStrokeController;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.*;

public class ListCreateButton extends PieMenuButton
{

	public static int SHOWON = PieMenuButton.SHOWON_SCRAP_CREATE | PieMenuButton.SHOWON_SCRAP_MENU;
	long uuidToBeDecorated;
	long cuid;
	
	
	public ListCreateButton(long cuid, long uuid)
	{
		super("lists.create");
		uuidToBeDecorated = uuid;
		this.cuid = cuid;
	}
	public ListCreateButton(long uuid)
	{
		this(CCanvasController.getCurrentUUID(), uuid);
	}
	
	public void onPressed(InputEventInfo ev)
	{
		super.onPressed(ev);
	}
	
	public void onReleased(InputEventInfo ev)
	{
		//super.onClick(ev);
		
		if (CGroupController.exists(this.uuidToBeDecorated))
		{
			long listuuid = createList(this.uuidToBeDecorated);
			CGroupController.show_group_bubblemenu(listuuid, new Point(0,0));
		}
		else if (CStrokeController.exists(this.uuidToBeDecorated))
		{
			long new_uuid = Calico.uuid();
			CStrokeController.makeScrap(this.uuidToBeDecorated, new_uuid);
			CGroupController.set_permanent(new_uuid, true);
			long listuuid = createList(new_uuid);
			CGroupController.show_group_bubblemenu(listuuid, new Point(0,0));
		}
		ev.stop();
	}
	public long createList(long groupToBeDecorated) {
		long newuuid = Calico.uuid();

//		CGroupController.shrink_to_contents(groupToBeDecorated);
		CGroupDecoratorController.list_create(groupToBeDecorated, newuuid);
		return newuuid;
	}
}
