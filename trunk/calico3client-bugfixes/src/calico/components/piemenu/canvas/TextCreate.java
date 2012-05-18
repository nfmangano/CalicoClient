package calico.components.piemenu.canvas;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

@Deprecated
public class TextCreate extends PieMenuButton {

	public static int SHOWON = PieMenuButton.SHOWON; //PieMenuButton.SHOWON_SCRAP_MENU;
	long uuid = 0L;
	
	public TextCreate()
	{
		super("canvas.text");		
	}
	
	public TextCreate(long u)
	{
		super("canvas.text");
		this.uuid = u;
	}
	
	public void onClick(InputEventInfo ev)
	{
		super.onClick(ev);
		String response = JOptionPane.showInputDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent(),
				  "Create Scrap with Text",
				  "Please enter text",
				  JOptionPane.QUESTION_MESSAGE);
		
		long new_uuid = 0l;
		if (response != null)
		{
			if (isImageURL(response))
			{
				new_uuid = Calico.uuid();
				Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_IMAGE_DOWNLOAD, new_uuid, CCanvasController.getCurrentUUID(), response, CalicoDataStore.ScreenWidth / 2, CalicoDataStore.ScreenHeight / 2));
			}
			else
			{
				new_uuid = Calico.uuid();
				CGroupController.create_text_scrap(new_uuid, CCanvasController.getCurrentUUID(), response, CalicoDataStore.ScreenWidth / 2, CalicoDataStore.ScreenHeight / 2);
			}
		}
//		if (this.uuid != 0l && new_uuid != 0l && CGroupController.groupdb.get(new_uuid).getParentUUID() == 0l)
//		{
//			CGroupController.move_start(new_uuid);
//			CGroupController.move_end(new_uuid, ev.getX(), ev.getY());
//		}
	}
	
	private boolean isImageURL(String text)
	{
		String regex = "((https?|ftp|gopher|telnet|file|notes|ms-help):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)+\\.(?:gif|jpg|jpeg|png|bmp|GIF|JPEG|JPG|PNG|BMP|Gif|Jpg|Jpeg|Png|Bmp)$";
		Pattern pattern = Pattern.compile(regex); 
		Matcher matcher = pattern.matcher(text); 
		return matcher.matches();
	}
}
