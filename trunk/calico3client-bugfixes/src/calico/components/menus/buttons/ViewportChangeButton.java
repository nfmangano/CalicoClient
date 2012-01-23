package calico.components.menus.buttons;

import java.awt.Color;
import java.awt.Point;
import java.net.URL;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.components.CViewportCanvas;
import calico.components.grid.CGrid;
import calico.components.menus.CanvasMenuButton;
import calico.controllers.CCanvasController;
import calico.controllers.CViewportController;
import calico.iconsets.CalicoIconManager;
import edu.umd.cs.piccolo.util.PBounds;



public class ViewportChangeButton extends CanvasMenuButton
{
	private static final long serialVersionUID = 1L;
	public static final int BUT_ZOOMOUT=0;
	public static final int BUT_ZOOMIN=1;
	public static final int BUT_ZOOMTOCANVAS=2;
	
	long cuid;
	private int type;
			
	public ViewportChangeButton( long cuid, int type)
	{
		super();		
		this.type=type;
		this.cuid=cuid;
		setPaint(Color.BLACK);
		try
		{
			/*if(CalicoDataStore.isInViewPort){
				//setTransparency(CalicoOptions.menu.menubar.transparency_disabled);
				setSelected(true);
			}*/
			
			switch(type)
			{
				case BUT_ZOOMOUT:
					setImage(CalicoIconManager.getIconImage("viewport.zoomout"));
					break;					
				case BUT_ZOOMIN:
					setImage(CalicoIconManager.getIconImage("viewport.zoomin"));
					break;
				case BUT_ZOOMTOCANVAS:
					setImage(CalicoIconManager.getIconImage("viewport.zoomtocanvas"));
					if(!CalicoDataStore.isInViewPort){
						setTransparency(CalicoOptions.menu.menubar.transparency_disabled);
					}
					break;
				
			}			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void actionMouseClicked()
	{		
		if(CalicoDataStore.isInViewPort){
			//if size is already big enough for just one canvas go to that view
			
			//resize it in the grid
			boolean changed=false;
			if(type==BUT_ZOOMOUT){
				int resizeType=GridViewportChangeButton.BUT_PLUS;				
				changed = CViewportController.changeViewPortSize(resizeType, false);
				if(!changed){
					//means we should jump to grid view
					CViewportCanvas viewport = CViewportCanvas.getInstance();
					if(viewport!=null){
						viewport.closeViewport();
					}
					CalicoDataStore.isInViewPort=false;
					CalicoDataStore.gridObject = CGrid.getInstance();
					CalicoDataStore.gridObject.refreshCells();
					CalicoDataStore.calicoObj.getContentPane().removeAll();
					CalicoDataStore.calicoObj.getContentPane().add( CalicoDataStore.gridObject.getComponent() );
					CalicoDataStore.calicoObj.pack();
					CalicoDataStore.calicoObj.setVisible(true);
					CalicoDataStore.calicoObj.repaint();
					CalicoDataStore.isViewingGrid = true;
				}
			}
			else if(type==BUT_ZOOMIN){
				int resizeType=GridViewportChangeButton.BUT_MINUS;
				changed = CViewportController.changeViewPortSize(resizeType, false);
				//consider the case where we should jump right into the full canvas view
				if(!changed || CViewportController.getViewPortSize() == 1){
					//means we should jump to full view,
					long cuid = CGrid.getInstance().getViewportCentralCanvas();
					
					CViewportCanvas.getInstance().closeViewport();
					CalicoDataStore.isInViewPort=false;
					
					CCanvasController.loadCanvas(cuid);
					CGrid.getInstance().centerViewportOnCanvas(cuid);
					return;
				}
			}else if(type==BUT_ZOOMTOCANVAS){
				//means we should jump to full view,
				long cuid = CGrid.getInstance().getViewportCentralCanvas();
				
				CViewportCanvas.getInstance().closeViewport();
				CalicoDataStore.isInViewPort=false;
				
				CCanvasController.loadCanvas(cuid);
				CGrid.getInstance().centerViewportOnCanvas(cuid);
				CViewportController.setViewPortSize(1);
				return;
			}
			//re-render if necessary
			if(changed){								
				CViewportCanvas.getInstance().refreshShownImages();
				CViewportCanvas.getInstance().rezoomCamera();
			}
						  
		}//if we are in the full canvas view
		else{
			//zoom out means jump to viewport 
			if(type==BUT_ZOOMOUT){				
				//create viewport in the grid
				CGrid.getInstance().removeViewport();
				CGrid.getInstance().drawViewport();
				long currentUID= CCanvasController.getCurrentUUID();
				PBounds canvasBounds = CGrid.getInstance().getCellBounds(currentUID);
				Point canvasCenter = new Point((int)canvasBounds.getCenterX()+1, (int)canvasBounds.getCenterY()+4); 
				CGrid.getInstance().moveViewPortToPoints(canvasCenter);
				CViewportController.setSmallestViewableSize();
				// go into viewport
								
				//CalicoDataStore.gridObject = CGrid.getInstance();
				
				CalicoDataStore.calicoObj.getContentPane().removeAll();
				CalicoDataStore.calicoObj.getContentPane().add( CalicoDataStore.gridObject.getComponent() );
				CalicoDataStore.calicoObj.pack();
				CalicoDataStore.calicoObj.setVisible(true);
				CalicoDataStore.calicoObj.repaint();
				CalicoDataStore.isViewingGrid = true;
				//CGrid.setMode(CGrid.MODE_VIEWPORT);
				
				CViewportController.loadViewport();				
				return;			
			}
		}
	}
	
}