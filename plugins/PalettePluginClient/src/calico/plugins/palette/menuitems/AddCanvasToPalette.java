package calico.plugins.palette.menuitems;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.SwingUtilities;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoDraw;
import calico.components.CGroup;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.plugins.palette.*;
import calico.plugins.palette.iconsets.CalicoIconManager;

public class AddCanvasToPalette extends PaletteBarMenuItem {

	public AddCanvasToPalette()
	{
		super();
		this.setImage(CalicoIconManager.getIconImage("palette.add"));
	}
	
	@Override
	public void onClick(InputEventInfo ev) {
		//create scrap that takes up entire  canvas
		final long uuid = Calico.uuid();
		final long cuuid = CCanvasController.getCurrentUUID();
	

		SwingUtilities.invokeLater(
				new Runnable() { public void run() { 
					Networking.ignoreConsistencyCheck = true;
					final CGroup group = new CGroup(uuid, cuuid);
					final Rectangle bounds = new Rectangle(0, 0, CalicoDataStore.serverScreenWidth, CalicoDataStore.serverScreenHeight);
					group.setShapeToRoundedRectangle(bounds, 0);
					CGroupController.no_notify_start(uuid, cuuid, 0l, true, group);
					group.setPaint(Color.white);
					CGroupController.setCurrentUUID(uuid);
					CGroupController.no_notify_finish(uuid, true, false, false);
					//add scrap to palette
					PalettePlugin.addGroupToPalette(PalettePlugin.getActivePaletteUUID(), uuid);
					//drop the scrap
					CGroupController.drop(uuid);
					Networking.ignoreConsistencyCheck = false;
					group.setVisible(false);
				}});
	}

}
