package calico.components.menus;

import java.awt.Font;
import java.awt.Rectangle;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.menus.buttons.EmailButton;
import calico.components.menus.buttons.EmailGridButton;
import calico.components.menus.buttons.GridSessionMenuButton;
import calico.components.menus.buttons.GridViewportChangeButton;
import calico.controllers.CCanvasController;



public class GridBottomMenuBar extends CanvasGenericMenuBar
{
	private static final long serialVersionUID = 1L;
	
	
	private long cuid = 0L;
	
	
	public GridBottomMenuBar(long c)
	{
		super(CanvasGenericMenuBar.POSITION_BOTTOM);
		Calico.logger.debug("loaded generic menu bar for GridBottomMenuBar");
		cuid = c;
		
		addCap(CanvasGenericMenuBar.ALIGN_START);
		
		
//		addIcon(new GridViewportChangeButton(GridViewportChangeButton.BUT_MINUS));
//		addIcon(new GridViewportChangeButton(GridViewportChangeButton.BUT_PLUS));
		
//		addSpacer();
//
//		addIcon(new GridSessionMenuButton(1));
//		addIcon(new GridSessionMenuButton(0));
		
//		addSpacer();
//		
//		addText(
//				"view clients", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(Rectangle boundingBox) {
//						CalicoDataStore.gridObject.drawClientList(boundingBox);
//					}
//				}
//				);
		
//		addSpacer();
		
//		addText(
//				"sessions", 
//				new Font("Verdana", Font.BOLD, 12),
//				new CanvasTextButton(cuid) {
//					public void actionMouseClicked(Rectangle boundingBox) {
//						Calico.showSessionPopup();
//					}
//				}
//				);
		
//		addSpacer();
		
		addTextEndAligned(
				"  Exit  ", 
				new Font("Verdana", Font.BOLD, 12),
				new CanvasTextButton(cuid) {
					public void actionMouseClicked(Rectangle boundingBox) {
						Calico.exit();
					}
				}
		);
		addSpacer(ALIGN_END);
		addIconRightAligned(new EmailGridButton());
		
		
		//addCap();
		
	}
	
	
}
