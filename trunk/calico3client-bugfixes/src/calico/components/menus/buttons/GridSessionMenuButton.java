package calico.components.menus.buttons;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import calico.*;
import calico.components.*;
import calico.components.grid.*;
import calico.controllers.CCanvasController;
import calico.controllers.CViewportController;
import calico.iconsets.CalicoIconManager;
import calico.modules.*;
import calico.networking.*;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.pswing.*;

import java.net.*;

import edu.umd.cs.piccolo.event.*;



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