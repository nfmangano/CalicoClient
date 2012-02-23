package calico.components.bubblemenu;

import java.awt.Image;
import java.net.URL;

import calico.components.piemenu.PieMenuButton;

public class BubbleMenuButton extends PieMenuButton {
	protected long guuid;
		
	// Set the Icon
	public BubbleMenuButton(String str)
	{
		super(str);
	}
	public BubbleMenuButton(URL url)
	{
		super(url);
	}
	public BubbleMenuButton(Image img)
	{
		super(img);
	}
	
	public long getGroupUUID()
	{
		return guuid;
	}
	
	public void setGroupUUID(long uuid)
	{
		guuid = uuid;
	}
	
}
