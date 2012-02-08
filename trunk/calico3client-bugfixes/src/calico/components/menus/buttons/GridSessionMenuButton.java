package calico.components.menus.buttons;

import java.awt.Color;

import calico.Calico;
import calico.CalicoDataStore;
import calico.iconsets.CalicoIconManager;



public class GridSessionMenuButton extends GridMenuButton
{
	private static final long serialVersionUID = 1L;
	
	private int type = 0;
			
	public GridSessionMenuButton(int type)
	{
		super();
		this.type = type;
		setPaint(Color.BLACK);//CalicoOptions.getColor("canvas.menubar.button.background_color"));
		setImage(CalicoIconManager.getIconImage("viewport.minus"));
		
		
	}
	
	public void actionMouseClicked()
	{
		if(type==0) {
		Calico.reconnect(CalicoDataStore.ServerHost, CalicoDataStore.ServerPort);
		}
		else if(type==1) {
			for(int i=0;i<CalicoDataStore.sessiondb.size();i++) {
				System.out.println("SESSIONS: "+CalicoDataStore.sessiondb.get(i).toString());
			}
		}
	}
	
}