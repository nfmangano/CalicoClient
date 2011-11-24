package calico.controllers;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.components.CCanvas;
import calico.components.CGroup;
import calico.components.CViewportCanvas;
import calico.components.grid.CGrid;
import calico.components.menus.CanvasGenericMenuBar;
import calico.components.menus.buttons.GridViewportChangeButton;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.NetworkCommand;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;

public class CViewportController {
	private static Logger logger = Logger.getLogger(CCanvasController.class.getName());
	
	private static Long2ReferenceOpenHashMap<Image> imagedb = null;
	
	/**
	 * Viewport sizes
	 */
	//2x2
	private static int SIZE1W;
	private static int SIZE1H;
	//3x3
	private static int SIZE2W;
	private static int SIZE2H;
	//4x4
	private static int SIZE3W;
	private static int SIZE3H;
	//2x2
	/* static int SIZE4W;
	private static int SIZE4H;
	//2.5x2.5
	private static int SIZE5H;
	private static int SIZE5W;
	//3x3
	private static int SIZE6H;
	private static int SIZE6W;
	//3.5x3.5
	private static int SIZE7H;
	private static int SIZE7W;*/
	
	
	
	private static boolean SIZES_SET=false;
	
	private static int viewPortSize=1;
	
	private static Rectangle viewportRectangle;
	
	
	
	public static Rectangle getViewportRectangle() {
		if(viewportRectangle==null){
			viewportRectangle = new Rectangle (CalicoOptions.grid.leftHeaderIconWidth,CalicoOptions.grid.topHeaderIconHeight,SIZE1W, SIZE1H);
		}
		return viewportRectangle;
	}


	public static void setViewportRectangle(Rectangle viewportRectangle) {
		CViewportController.viewportRectangle = viewportRectangle;
	}

	public static boolean isViewportMinSize(){
		if(viewPortSize==1){
			return true;
		}else{
			return false;
		}
	}
	public static int getViewPortSize() {
		return viewPortSize;
	}
	
	private static void startupImageDB(){
		
		if(imagedb==null){
			imagedb =  new Long2ReferenceOpenHashMap<Image>();
		}
		/*for(long cuid:CCanvasController.canvasdb.keySet()){
			Calico.logger.debug("loading image "+cuid);
			CCanvas canvas = CCanvasController.canvasdb.get(cuid);
			PCamera canvasCam =canvas.getCamera();		
			canvasCam.removeChild(canvas.menuBar);
			canvasCam.removeChild(canvas.topMenuBar);		
			Image img = canvasCam.toImage();
			canvasCam.addChild(canvas.menuBar);
			canvasCam.addChild(canvas.topMenuBar);			
			
			imagedb.put(cuid,img);
		}*/
	}
	/**
	 * dumps every image in the DB in case we need to refresh them all forcefully
	 */
	public static void dumpImages(){
		imagedb=null;
	}
	
	public static Image refreshImageInDB(long cuid){
		if(imagedb==null){
			startupImageDB();
		}		
		if(CCanvasController.canvasdb.containsKey(cuid)){
			CCanvas canvas = CCanvasController.canvasdb.get(cuid);
			PCamera canvasCam =canvas.getCamera();
//			PCamera canvasCam =canvas.getLayer().getCamera(0);
			for (int i = canvasCam.getChildrenCount() - 1; i >= 0; i--)
			{
				if (canvasCam.getChild(i) instanceof CanvasGenericMenuBar)
				{
					canvasCam.removeChild(i);
				}
			}
			canvasCam.removeChild(canvas.menuBar);
//			canvasCam.removeChild(canvas.topMenuBar);
			
			
			double scale = (CGrid.getInstance().getImgw()-8)/CGrid.getInstance().getViewportBounds().width;
						
			int width = (int)(scale*CViewportCanvas.PreferredScreenWidth);
			int height = (int)(scale*CViewportCanvas.PreferredScreenHeight);
			
			
			
			
			
			
			
			
			

			Image img = canvasCam.toImage(width, height, null);
			if (canvas.menuBar != null)
				canvasCam.addChild(canvas.menuBar);
//			if (canvas.topMenuBar != null)
//				canvasCam.addChild(canvas.topMenuBar);			
			
			imagedb.put(cuid,img);
			return img;
		}
		return null;
	}
	
	public static Image getScaledGroupImage(long guuid)
	{
		Image ret = null;
		
		CGroup group = CGroupController.groupdb.get(guuid);
		if (group != null)
		{
			float scale = new Double((CGrid.getInstance().getImgw()-8)/CGrid.getInstance().getViewportBounds().width).floatValue();
			ret = group.getFamilyPicture();
			ret = ret.getScaledInstance(Math.round(scale * ret.getWidth(null)), Math.round(scale * ret.getHeight(null)), Image.SCALE_REPLICATE);
		}
		
		return ret;
	}
	
	/**
	 * Takes in the group uuid, and returns its scaled bounds with respect to the camera layer.
	 * 
	 * @param guuid Group UUID
	 * @return
	 */
	public static Rectangle2D getScaledGroupBounds(long guuid)
	{
		Rectangle2D rect = new Rectangle2D.Double();
		
		CGroup group = CGroupController.groupdb.get(guuid);
		if (group != null)
		{
			double scale = (CGrid.getInstance().getImgw()-8)/CGrid.getInstance().getViewportBounds().width;
			Rectangle2D nativeGroupBounds  = group.getBounds().getBounds2D();
			Rectangle2D scaledCanvasBounds = CViewportCanvas.getInstance().getCanvasImageBounds(group.getCanvasUID());
			
			double scaledWidth = nativeGroupBounds.getWidth() * scale;
			double scaledHeight = nativeGroupBounds.getHeight() * scale;
			
			double xPos = scaledCanvasBounds.getX() + nativeGroupBounds.getX() * scale ;
			double yPos = scaledCanvasBounds.getY() + nativeGroupBounds.getY() * scale ;
			
			rect = new Rectangle2D.Double(xPos, yPos, scaledWidth, scaledHeight);
		}
		
		return rect;
	}
	
	public static void refreshImage(long cuid){		
		if(imagedb!=null && CCanvasController.canvasdb.containsKey(cuid)){					
			CViewportCanvas.getInstance().refreshImage(cuid);
		}
	}
	
	public static Image getImage(long cuid){
		if(imagedb==null){
			
			startupImageDB();
		}
		if(imagedb.containsKey(cuid)){
			return imagedb.get(cuid);
		}
		else if(CCanvasController.canvasdb.containsKey(cuid)){
			return refreshImageInDB(cuid);			
		}
		return null;
	}
	
	public static void dumpImage(long cuid){
		if(imagedb!=null){
			imagedb.remove(cuid);
		}
	}


	public static void setViewPortSize(int viewPortSize) {
		CViewportController.viewPortSize = viewPortSize;
	}


	public static void loadViewport()
	{		
		if (CCanvasController.currentCanvasUUID != 0l)
			CCanvasController.canvasdb.get(CCanvasController.currentCanvasUUID).getLayer().setScale(1.0d);
		
		Calico cal = CalicoDataStore.calicoObj;
		if(CalicoDataStore.isViewingGrid){
			CalicoDataStore.isViewingGrid = false;
			CalicoDataStore.isInViewPort = true;
		}else if(CalicoDataStore.isInViewPort){
			if (CViewportCanvas.getInstance() != null)
				CViewportCanvas.getInstance().closeViewport();
		}
		cal.getContentPane().removeAll();
		
		//get the necessary info to start up the viewport
		CGrid grid = CGrid.getInstance();
		PBounds viewportBounds = grid.getViewportBounds(); 
		//CViewportCanvas viewportCanvas = CViewportCanvas.createInstance(viewportBounds, grid.getCanvasesInViewport(), grid.getViewportScale(), grid.getViewportCentralCanvas()); 
		CViewportCanvas.createInstance(viewportBounds, grid.getViewportCentralCanvas());
		CViewportCanvas viewportCanvas = CViewportCanvas.getInstance(); 
		cal.getContentPane().add( viewportCanvas );
		//cal.setJMenuBar(null);
		cal.pack();
		cal.setVisible(true);
		cal.repaint();
		
		//Networking.send(NetworkCommand.CLICK_CANVAS, cellid);
		CCanvasController.setCurrentUUID(viewportCanvas.getCuidWorkingCanvas());
		
		//TODO: check if this line is necessary cause I'm getting sometimes an error
		CArrowController.setOutstandingAnchorPoint(null);
		
		// Make sure the Menu bar has all been redrawn and updated
		//CCanvasController.canvasdb.get(uuid).menuBar.refresh();
		viewportCanvas.drawToolbar();
		
		//MessageObject.showNotice("Zoomed into viewport");
	}
	
	/**
	 * sets up the initial sizes of the viewports
	 * @param imgw the width of one cell in the grid 
	 * @param imgh the height of one cell in the grid
	 */
	public static void setSizes(int imgw, int imgh) {
		//set the sizes
//		SIZE1W=imgw;
//		SIZE1H=imgh;		
//		//1.25x1.25
//		SIZE2W=(int)(imgw*1.25);
//		SIZE2H=(int)(imgh*1.25);
//		//1.5x1.5
//		SIZE3W=(int)(imgw*1.5);
//		SIZE3H=(int)(imgh*1.5);
//		//2x2
//		SIZE4W=(int)(imgw*2);
//		SIZE4H=(int)(imgh*2);
//		//2.5x2.5
//		SIZE5W=(int)(imgw*2.5);
//		SIZE5H=(int)(imgh*2.5);
//		//3x3
//		SIZE6W=(int)(imgw*3);
//		SIZE6H=(int)(imgh*3);
//		//3.5x3.5
//		SIZE7W=(int)(imgw*3.5);
//		SIZE7H=(int)(imgh*3.5);
		
		
		
		//1x1
		SIZE1W=(int)(imgw);
		SIZE1H=(int)(imgh);
		
		//3x3
		SIZE2W=(int)(imgw*3);
		SIZE2H=(int)(imgh*3);
		//6x6
		SIZE3W=(int)(imgw*5);
		SIZE3H=(int)(imgh*5);
		
		SIZES_SET=true;
	}
	
	/**
	 * changes the size of the canvas 
	 * @param type the type of the change (increase or decrease)
	 * @param animate true if we want to animate the movement, false otherwise
	 * @return
	 */
	public static boolean changeViewPortSize(int type, boolean animate) {
		if(!SIZES_SET){
			CGrid.getInstance().setViewportSizes();
		}
		int newSizew=0;
		int newSizeh=0;
		if(type==GridViewportChangeButton.BUT_MINUS){
			//reduce size of viewport
			switch(viewPortSize){
				case 1:
					viewPortSize=1;
					newSizew=SIZE1W;
					newSizeh=SIZE1H;					
					break;
				case 2:
					viewPortSize=1;
					newSizew=SIZE1W;
					newSizeh=SIZE1H;
					break;
				case 3:
					viewPortSize=2;
					newSizew=SIZE2W;
					newSizeh=SIZE2H;
					break;
				case 4:
					viewPortSize=3;
					newSizew=SIZE3W;
					newSizeh=SIZE3H;
					break;
				/*case 5:
					viewPortSize=4;
					newSizew=SIZE4W;
					newSizeh=SIZE4H;
					break;
				case 6:
					viewPortSize=5;
					newSizew=SIZE5W;
					newSizeh=SIZE5H;
					break;
				case 7:
					viewPortSize=6;
					newSizew=SIZE6W;
					newSizeh=SIZE6H;
					break;	*/			
			}		
			
		}
		else if(type==GridViewportChangeButton.BUT_PLUS){
			//increase size of viewport
			switch(viewPortSize){
				case 1:
					viewPortSize=2;
					newSizew=SIZE2W;
					newSizeh=SIZE2H;
					break;
				case 2:
					viewPortSize=3;
					newSizew=SIZE3W;
					newSizeh=SIZE3H;
					break;
				/*case 3:
					viewPortSize=4;
					newSizew=SIZE4W;
					newSizeh=SIZE4H;
					break;
				case 4:
					viewPortSize=5;
					newSizew=SIZE5W;
					newSizeh=SIZE5H;
					break;
				case 5:
					viewPortSize=6;
					newSizew=SIZE6W;
					newSizeh=SIZE6H;
					break;
				case 6:
					viewPortSize=7;
					newSizew=SIZE7W;
					newSizeh=SIZE7H;
					break;*/
				case 3:					
					break;				
			}
			
		}
		if(newSizew!=0&&newSizeh!=0){
			CGrid.getInstance().changeViewPortSize(newSizew, newSizeh, animate);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * returns the smallest size of the viewport
	 */
	public static void setSmallerSize(){
		CGrid.getInstance().changeViewPortSize(SIZE1W, SIZE1H, false);
	}
	
	public static void setSmallestViewableSize(){
		CGrid.getInstance().changeViewPortSize(SIZE2W, SIZE2H, false);
		viewPortSize = 2;
	}
	
	
	/**
	 * changes the focused canvas to the one in point P
	 * @param p the point that contains the new focused canvas
	 */
	public static void changeFocusedCanvas(Point p){
		if (!CalicoDataStore.isInViewPort)
			return;
		
		if(p!=null){
			long newFocused = CViewportCanvas.getInstance().getCanvasIdOfPoint(p);
			if(newFocused!=0l){
				CViewportCanvas.getInstance().changeFocusedCanvas(p);
			}
		}
	}
	
	/**
	 * copies the dragged group to a new canvas if and only if it is outside of the canvas where it was picked
	 * @param p the point where we dropped the dragged group 
	 */
	/*public static void copyDraggedGroupToCanvas(Point p){
		long cuidDrawnGroup = CViewportCanvas.getInstance().getCuidDraggedGroupOriginCanvas();
		long cuidDropped = CViewportCanvas.getInstance().getCanvasIdOfPoint(p);
		if(cuidDropped!=cuidDrawnGroup){
			long groupId = CViewportCanvas.getInstance().getIdDraggedGroup();
			//get the relative position of the point in the dropped canvas
			Point pInDroppedCanvas = CViewportCanvas.getInstance().scalePointToCanvas(p, cuidDropped);
			Point groupStartPointInSourceCanvas = CViewportCanvas.getInstance().getDraggedGroupStartingPoint();
			int xOffset = CViewportCanvas.getInstance().getDraggedGroupOffsetX();
			int yOffset = CViewportCanvas.getInstance().getDraggedGroupOffsetY();
			int finalXOffset = (int)pInDroppedCanvas.getX()-(int)groupStartPointInSourceCanvas.getX();
			int finalYOffset = (int)pInDroppedCanvas.getY()-(int)groupStartPointInSourceCanvas.getY();
			Calico.logger.debug("Copying group "+ groupId+" to canvas "+cuidDropped+" with XOffset:"+finalXOffset+". YOffset:"+finalYOffset);
			Networking.send(NetworkCommand.GROUP_COPY_TO_CANVAS, groupId,cuidDropped,finalXOffset,finalYOffset );
			
		}
		
	}*/


	public static void dragViewport(double deltaX, double deltaY ) {
		//Calico.logger.debug("dragging deltaX:"+deltaX+". deltay:"+deltaY);
		//scale the x and y
		double xScale = CViewportCanvas.getInstance().getVpXScaleToFullScreen();
		double yScale = CViewportCanvas.getInstance().getVpYScaleToFullScreen();
		deltaX = deltaX*xScale*-1;
		deltaY = deltaY*yScale*-1;
		CGrid.getInstance().dragViewPort(deltaX, deltaY);
		CViewportCanvas.getInstance().rezoomCamera();
	}
}
