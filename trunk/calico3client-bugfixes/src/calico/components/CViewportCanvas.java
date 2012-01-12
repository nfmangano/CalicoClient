package calico.components;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArraySet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import calico.Calico;

import calico.CalicoOptions;
import calico.components.grid.CGrid;
import calico.components.menus.CanvasMenuBar;
import calico.components.menus.CanvasTopMenuBar;
import calico.components.piemenu.PieMenu;
import calico.controllers.CArrowController;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.controllers.CViewportController;
import calico.input.CalicoMouseListener;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PLine;

public class CViewportCanvas extends PCanvas{

	
	private double width;
	private double height;
	private double xStart;
	private double yStart;
		
	//private static int canImgw = 0;
	//private static int canImgh = 0;
	
	
	public CanvasMenuBar menuBar = null;
//	private CanvasTopMenuBar topMenuBar = null;
	
	public static int PreferredScreenWidth = 0;
	public static int PreferredScreenHeight = 0;
	
	public static boolean inViewportView;
	
	private double scaleFromGCellsToVP;
	
	private double vpXScaleToFullScreen;
	private double vpYScaleToFullScreen;
	
	private long cuidWorkingCanvas;
	private PBounds focusedCanvasCellBounds;
	
	public static final int HEIGHT_REDUCE_SIZE=46;
	

	//temp atributes to test strategy to draw the canvases
	private PLayer canvasLayer;
	private Long2ReferenceOpenHashMap<PImage> canvasImages;
	private Long2ReferenceOpenHashMap<PBounds> canvasImageBounds;

	
	private PLayer borderLayer;
	//private PLayer backgroundLayer;
	
	//private PLayer pressedGroupLayer;
	//private ArrayList<PImage> pressedGroupImages;
	//private PImage pressedGroupMainImage;
	
	//TODO: find out what is the impact of this uuid on the menu bars?
	//public long uuid = 0L;
	
	private Long2ReferenceOpenHashMap<PLayer> canvasImageLayers ;
	
	private Long2ReferenceOpenHashMap<Boolean> canvasesShown;
	

	
	
	/**
	 * attributes related to a dragged group we are moving around in the viewport
	 */
	//private int draggedGroupOffsetX;	
	//private int draggedGroupOffsetY;
	//private long cuidDraggedGroupOriginCanvas=0L;
	//private Point draggedGroupStartingPoint;
	

	//private long idDraggedGroup=0L;
	
	private static CViewportCanvas instance;
		
	
	public static CViewportCanvas createInstance(PBounds viewportBounds,  long cuidWorkingCanvas){
		
		instance =  new CViewportCanvas(viewportBounds, cuidWorkingCanvas);
		
		return instance;
	}
	
	//returns null if no instance has been created yet
	public static CViewportCanvas getInstance()
	{
		return instance;
	}
	
	private CViewportCanvas(PBounds viewportBounds,  long cuidWorkingCanvas){
	
		inViewportView=true;
		//The -8 / +4 buffer is to accomodate for the borders that the grid has between the canvases
		width = viewportBounds.getWidth()-8;
		height = viewportBounds.getHeight()-8;
		
		xStart = viewportBounds.getMinX();
		yStart = viewportBounds.getMinY()-2;		
		scaleFromGCellsToVP=CGrid.getInstance().getViewportScale();
		
		//Calico.logger.debug("drawing viewport. xStart:"+xStart+". yStart:"+yStart);
		//Calico.logger.debug("drawing viewport. Scale:"+scaleFromGCellsToVP);
		
		this.cuidWorkingCanvas = cuidWorkingCanvas;
				
		//relative size of viewport compared to full screen
		vpXScaleToFullScreen = width / (PreferredScreenWidth);
		vpYScaleToFullScreen = height / (PreferredScreenHeight);
		
		//calculate the size of each canvas in this view
		setPreferredSize(new Dimension(PreferredScreenWidth, PreferredScreenHeight));
		
		setBounds(0, 0, PreferredScreenWidth, PreferredScreenHeight);
				
		//get the cameras for these canvases and add them to this canvas		
		canvasLayer = new PLayer();
			
		borderLayer = new PLayer();
		canvasImageLayers = new Long2ReferenceOpenHashMap<PLayer>();
		canvasesShown = new Long2ReferenceOpenHashMap<Boolean>();
				
		//borders are unscaled so we add them to the layer, these will be scaled to the scaling already set to the camera
		this.getLayer().addChild(borderLayer);
		//canvas elements unscaled to the layer, these will be scaled to the scaling already set to the camera
		this.getLayer().addChild(canvasLayer);
		
		
		
		// We are using our own input listener, we dont want piccolo's
		removeInputSources();

		CalicoMouseListener mouseListener = new CalicoMouseListener();
		
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addMouseWheelListener(mouseListener);

		removeInputEventListener(getPanEventHandler());
		removeInputEventListener(getZoomEventHandler());

		CalicoInputManager.addCanvasInputHandler(this.cuidWorkingCanvas);
			
		repaint();
		startUpFocusedCanvas(cuidWorkingCanvas);
	
	}
	
	
	private void setEditableCanvasCamera(long cuid, double xOffset, double yOffset){
		//scale the offset		
		int canvInitX = (int)((xOffset)/vpXScaleToFullScreen);
		int canvInitY = (int)((yOffset)/vpYScaleToFullScreen);
		
		//associate the camera in this component with the camera related to the editable canvas
		
		//get camera from CCanvas version
		if(!CCanvasController.canvasCameras.containsKey(cuid)){
			CCanvasController.canvasCameras.put(cuid,CCanvasController.canvasdb.get(cuid).getCamera());
		}		
		this.setCamera(CCanvasController.canvasCameras.get(cuid));
		
		
		this.getCamera().setViewScale(scaleFromGCellsToVP);
		
		this.getCamera().setViewOffset(canvInitX,canvInitY);
		
	}
	
	/**
	 * Draws all borders for a canvas starting in the given position
	 * @param xOffset
	 * @param yOffset
	 * @param cuid the id of the canvas to draw
	 */
	private void drawCanvasBorders(double xOffset, double yOffset, long cuid) {
		
		int initX = (int)((xOffset));
		int initY = (int)((yOffset));
		
		double borderHeigth = ((PreferredScreenHeight)-HEIGHT_REDUCE_SIZE);
		double borderWidth = ((PreferredScreenWidth ));
		
		int endX = (int)(initX+borderWidth);
		int endY = (int)(initY+borderHeigth);
				
		borderLayer.addChild(drawBorderLine(initX,initY, endX,initY));//top
		borderLayer.addChild(drawBorderLine(initX,initY, initX,endY));//left
		borderLayer.addChild(drawBorderLine(endX,initY, endX,endY));//right
		borderLayer.addChild(drawBorderLine(initX,endY,endX,endY));//bottom
				
		borderLayer.addChild(drawBackgroundLine(initX+7,initY-10, endX-7,initY-10));//top
		borderLayer.addChild(drawBackgroundLine(initX-10,initY-4, initX-10,endY));//left
		borderLayer.addChild(drawBackgroundLine(endX+10,initY-4, endX+10,endY));//right
		borderLayer.addChild(drawBackgroundLine(initX+7,endY+10,endX-7,endY+10));//bottom
		
		String coordtxt = CCanvasController.canvasdb.get(cuid).getGridCoordTxt();
		PText gct = new PText(coordtxt);
		gct.setConstrainWidthToTextWidth(true);
		gct.setFont(new Font("Monospaced", Font.BOLD, 20));
		Rectangle idrect = new Rectangle(initX,initY,16,16);
		gct.setBounds(idrect);
		borderLayer.addChild(gct);
	
		
		
	}
	
	/**
	 * closes the viewport by removing all unnecesary layers and 
	 * returns the camera for the focused canvas to the right place
	 */
	public void closeViewport(){
		getCamera().removeChild(menuBar);
//		getCamera().removeChild(topMenuBar);
		
		getLayer().removeChild(borderLayer);
		getLayer().removeChild(canvasLayer);
		getCamera().setViewScale(1);
		getCamera().setViewOffset(0,0);
		inViewportView=false;	
	}

	/**
	 * Draws the borderline 
	 * @param x the X point where the border starts
	 * @param y the Y point where the border starts
	 * @param x2 the x point where the border ends
	 * @param y2 the y point where the border ends
	 * @return
	 */
	
	private PLine drawBorderLine(int x,int y, int x2, int y2)
	{
		PLine pline = new PLine();
		pline.addPoint(0, x, y);
		pline.addPoint(1, x2, y2);
		pline.setStroke(new BasicStroke( 2.0f ));
		
		pline.setStrokePaint( Color.BLACK );
		/*if(focused){
			pline.setStrokePaint( Color.BLUE );
			pline.setStroke(new BasicStroke( 4.0f ));
		}*/
		return pline;
	}
	
	private PLine drawBackgroundLine(int x,int y, int x2, int y2 )
	{
		PLine pline = new PLine();
		pline.addPoint(0, x, y);
		pline.addPoint(1, x2, y2);
		pline.setStroke(new BasicStroke( 22.0f ));
		
		pline.setStrokePaint( CalicoOptions.viewport.viewport_background_color);
		pline.setTransparency(CalicoOptions.viewport.viewport_background_transparency);
		
		return pline;
	}
	
	/**
	 * draws a canvas THAT IS NOT the focused one
	 * Gets the image from the CViewport controller (so that we don't re-render always)
	 * Sets its bounds and adds it to the given layer 
	 * @param cuid the id of the canvas
	 * @param xOffset the X position in the layer it should start
	 * @param yOffset the Y position in the layer it should start
	 * @param oneCanvasLayer the layer to add this canvas
	 * @param refresh if true refreshes the image in the CViewportController
	 */
	private void drawCanvas(long cuid, double xOffset, double yOffset, PLayer oneCanvasLayer, boolean refresh, boolean show) {			
		int canvInitX = (int)((xOffset));
		int canvInitY = (int)((yOffset));
		if(refresh){
			CViewportController.refreshImageInDB(cuid);
		}
		PImage canvasImage;
		if(show){
			canvasImage = new PImage(CViewportController.getImage(cuid));
		}else{
			canvasImage = new PImage();
		}
		int canvasYadditionalOffset=0;
		int canvasYsizeReduc=40;
		int canvasXsizeReduc=4;
		canvasImage.setBounds(canvInitX,canvInitY+canvasYadditionalOffset,PreferredScreenWidth-canvasXsizeReduc, PreferredScreenHeight-canvasYsizeReduc);
		canvasImageBounds.put(cuid, new PBounds(canvInitX,canvInitY,PreferredScreenWidth-canvasXsizeReduc, PreferredScreenHeight-canvasYsizeReduc));
		if(show){
			//Calico.logger.debug("Canvas "+ cuid+" is currently in the viewport, so well include it in the layer");
			oneCanvasLayer.addChild(canvasImage);
		}
		canvasImages.put(cuid, canvasImage);
	}
	/**
	 * finds out if a point is on the menu bar
	 * @param point the point 
	 * @return true if the point is on the menu bar
	 */
	public boolean isPointOnMenuBar(Point point)
	{
		if (this.menuBar == null)
			return false;
		
		return (this.menuBar.isPointInside(point) /*|| this.topMenuBar.isPointInside(point)*/);
	}
	
	/**
	 * draws the top toolbar. it is the same top toolbar used by the regular canvas view
	 */
//	private void drawTopToolbar()
//	{
//		if(topMenuBar!=null)
//		{
//			getCamera().removeChild(topMenuBar);
//			topMenuBar = null;
//		}
//		CCanvasController.canvasdb.get(cuidWorkingCanvas).drawTopToolbar();
//		topMenuBar = CCanvasController.canvasdb.get(cuidWorkingCanvas).topMenuBar;
//		getCamera().addChild(topMenuBar);
//	}
	
	/**
	 * draws the bottom toolbar. it is the same bottom toolbar used by the regular canvas view
	 */
	private void drawBottomToolbar()	{
		
		if(this.menuBar!=null)
		{	
			getCamera().removeChild(this.menuBar);
			this.menuBar = null;
		}
		CCanvasController.canvasdb.get(cuidWorkingCanvas).drawMenuBars();
		this.menuBar = CCanvasController.canvasdb.get(cuidWorkingCanvas).menuBarLeft;
		
		getCamera().addChild(this.menuBar);
	}
	
	/**
	 * draws the top and bottom toolbar
	 */
	public void drawToolbar()
	{
//		drawTopToolbar();
		drawBottomToolbar();
	}
	
	/**
	 * receives a point in the currently focused canvas within the viewport and
	 * scales the point so it becomes the equivalent point if we were in the full canvas view
	 * so that it can be processed as if we were in the full canvas view 
	 * @param initPoint the point to scale
	 * @return the point scaled (the parameter is not modified)
	 */
	public Point scalePointToFocusedCanvas(Point initPoint){
		//Calico.logger.debug("scaling point."+initPoint);
		Point ret = new Point();
		//remove the offset
		//offset:gwidth,gheight+30
		double xPoint=initPoint.getX();
		double yPoint=initPoint.getY();
		//calculate the focus canvas offset
						
		double canvasXStart = ((focusedCanvasCellBounds.getMinX())- xStart)/vpXScaleToFullScreen;
		double canvasYStart = ((focusedCanvasCellBounds.getMinY())- yStart)/vpYScaleToFullScreen;
		//Calico.logger.debug("scaling point. canvasXStart:"+canvasXStart +".canvasYStart:"+canvasYStart  );
		
		//offset it
		xPoint = (xPoint-canvasXStart);
		yPoint = (yPoint-canvasYStart);
		//Calico.logger.debug("scaling point. OFFSET X:"+xPoint +".Y:"+yPoint  );

		//scale it
		xPoint=xPoint/scaleFromGCellsToVP;
		yPoint=yPoint/scaleFromGCellsToVP;
		//Calico.logger.debug("scale:"+scaleFromGCellsToVP);
		
		ret.setLocation(xPoint, yPoint);
		//Calico.logger.debug("scaling point. SCALED:"+initPoint);		
		return ret;
	}
	
	/**
	 * Scales a point in the viewport view to its relative position in a selected canvas
	 * @param initPoint the point in the viewport
	 * @param cuid the id of the canvas we want to scale to 
	 * @return the scaled point 
	 */
	public Point scalePointToCanvas(Point initPoint, long cuid){
		//Calico.logger.debug("scaling point."+initPoint);
		Point ret = new Point();
		//remove the offset
		//offset:gwidth,gheight+30
		double xPoint=initPoint.getX();
		double yPoint=initPoint.getY();
		//calculate the focus canvas offset
		PBounds cellBounds = CGrid.getInstance().getCellBounds(cuid);		
		
		double canvasXStart = (cellBounds.getMinX()- xStart)/vpXScaleToFullScreen;
		double canvasYStart = (cellBounds.getMinY()- yStart)/vpYScaleToFullScreen;
		
		//offset it
		xPoint = (xPoint-canvasXStart);
		yPoint = (yPoint-canvasYStart);
		//Calico.logger.debug("scaling point. OFFSET X:"+xPoint +".Y:"+yPoint  );

		//scale it
		xPoint=xPoint/scaleFromGCellsToVP;
		yPoint=yPoint/scaleFromGCellsToVP;
		//Calico.logger.debug("scale:"+scaleFromGCellsToVP);
		
		ret.setLocation(xPoint, yPoint);
		//Calico.logger.debug("scaling point. SCALED:"+initPoint);		
		return ret;
	}
	
	public int scaleXShift(int shift_x){
		return (int)(shift_x/scaleFromGCellsToVP);
	}
	
	public int scaleYShift(int shift_y){
		return (int)(shift_y/scaleFromGCellsToVP);
	}
	
	
	
	/**
	 * Scales a point from its relative position in a canvas to its relative position in the viewport view
	 * @param initPoint the point in its position in the canvas
	 * @param cuid the id of the canvas
	 * @return the point in the viewport view
	 */
	public Point scalePointFromCanvas(Point initPoint, long cuid){
		
		Point ret = new Point();
		//remove the offset
		//offset:gwidth,gheight+30
		
		double xPoint=initPoint.getX();
		double yPoint=initPoint.getY();
		
		//calculate the focus canvas offset
		PBounds cellBounds = CGrid.getInstance().getCellBounds(cuid);
		
		double canvasXStart = (cellBounds.getMinX()- xStart)/vpXScaleToFullScreen;
		double canvasYStart = (cellBounds.getMinY()- yStart)/vpYScaleToFullScreen;
		
		//scale it
		xPoint=xPoint*scaleFromGCellsToVP;
		yPoint=yPoint*scaleFromGCellsToVP;
		
		//offset it
		xPoint = (xPoint+canvasXStart);
		yPoint = (yPoint+canvasYStart);
				
		ret.setLocation(xPoint, yPoint);
		//Calico.logger.debug("scaling point. SCALED:"+initPoint);		
		return ret;
	}
	
	/**
	 * Receives a point that has been scaled up from the viewport to the full canvas view
	 * and scales it back down. So that if we want to e.g. draw the pie menu where a click was made
	 * the actual pie menu is drawn in the place it should in the viewport and not in the equivalent
	 * point in the full canvas view
	 * @param initPoint the point to scale
	 * @return the point scaled (the parameter is not modified)
	 */
	public Point unscalePointFromFocusedCanvas(Point initPoint){		
		Point ret = new Point();
		double xPoint=initPoint.getX();
		double yPoint=initPoint.getY();
		
		PBounds fCellBounds = CGrid.getInstance().getCellBounds(cuidWorkingCanvas);
		double canvasXStart = (fCellBounds.getMinX()- xStart)/vpXScaleToFullScreen;
		double canvasYStart = (fCellBounds.getMinY()- yStart)/vpYScaleToFullScreen;
				
		//scale it
		xPoint=xPoint*scaleFromGCellsToVP;
		yPoint=yPoint*scaleFromGCellsToVP;
				
		//offset it
		xPoint = (xPoint+canvasXStart);
		yPoint = (yPoint+canvasYStart);
		ret.setLocation(xPoint, yPoint);		
		
		return ret;
	}
	
	/**
	 * There is one focused canvas at a time.
	 * This method finds if a point is on this one focused	  
	 * @param p the point
	 * @return true if its in the focused canvas
	 */
	public boolean isClickOnFocusedCanvas(Point p){
		/*double cHeight = (PreferredScreenHeight-HEIGHT_REDUCE_SIZE)*scaleFromGCellsToVP;
		double cWidth = (PreferredScreenWidth-8)*scaleFromGCellsToVP;		
		double canvasXStart = ((focusedCanvasCellBounds.getMinX())- xStart)/vpScaleToFullScreen;
		double canvasYStart = ((focusedCanvasCellBounds.getMinY()+4)- yStart)/vpScaleToFullScreen;
		PBounds canvasBounds=new PBounds(canvasXStart+4, canvasYStart+4, cWidth,cHeight);
		if(canvasBounds.contains(p)){
			return true;
		}*/
		if(canvasImageBounds.get(cuidWorkingCanvas).contains(scalePointToFocusedCanvas(p))){
			return true;
		}
		return false;
	}
	
	/**
	 * finds which canvas in the viewport contains a point 
	 * @param p
	 * @return
	 */
	public long getCanvasIdOfPoint(Point p){
		
		long ret=0l;
		/*double cHeight = (PreferredScreenHeight-HEIGHT_REDUCE_SIZE)*scaleFromGCellsToVP;
		double cWidth = (PreferredScreenWidth-8)*scaleFromGCellsToVP;
		PBounds canvasBounds=null;
		PBounds scaledBounds=null;*/
		for (long oneCUID:canvasesShown.keySet()){			
			/*PBounds fCellBounds = CGrid.getInstance().getCellBounds(oneCUID);
			double canvasXStart = ((fCellBounds.getMinX())- xStart)/vpScaleToFullScreen;
			double canvasYStart = ((fCellBounds.getMinY()+4)- yStart)/vpScaleToFullScreen;
			 
			
			canvasBounds = new PBounds(canvasXStart+4, canvasYStart+4, cWidth,cHeight);
			
			if(canvasBounds .contains(p)){				
				ret = oneCUID;
			}*/
			
			if(canvasImageBounds.get(oneCUID).contains(scalePointToFocusedCanvas(p))){
				ret = oneCUID;
			}
		}
		return ret;
	}
	
	public PBounds getCanvasImageBounds(long cuuid)
	{
		PBounds ret = null;
			
		if (canvasImageBounds.containsKey(cuuid))
		{
			PBounds cellBounds = CGrid.getInstance().getCellBounds(cuuid);
//			return canvasImageBounds.get(cuuid);
			
			Point retP = new Point();
			
			double xPoint=0;
			double yPoint=0;
			
			PBounds fCellBounds = cellBounds;
			double canvasXStart = (fCellBounds.getMinX()- xStart)/vpXScaleToFullScreen;
			double canvasYStart = (fCellBounds.getMinY()- yStart)/vpYScaleToFullScreen;
					
			//scale it
			xPoint=xPoint*scaleFromGCellsToVP;
			yPoint=yPoint*scaleFromGCellsToVP;
					
			//offset it
			xPoint = (xPoint+canvasXStart);
			yPoint = (yPoint+canvasYStart);
			retP.setLocation(xPoint, yPoint);
			
			ret = new PBounds(retP.getX(), retP.getY(), cellBounds.getWidth(), cellBounds.getHeight());
			
			
//			double fxOffset = cellBounds.getX()+xStart;
//			double fyOffset = cellBounds.getY()+yStart;
//			
//			double wScaleFromGridCellToFullScreen = ((double)CGrid.getInstance().getImgw()-8)/(double)(PreferredScreenWidth);
//			double hScaleFromGridCellToFullScreen = ((double)CGrid.getInstance().getImgh()-8)/(double)(PreferredScreenHeight);
//			double xOffset = ((cellBounds.getMinX()-(focusedCanvasCellBounds.getMinX()))/wScaleFromGridCellToFullScreen);
//			double yOffset = ((cellBounds.getMinY()-(focusedCanvasCellBounds.getMinY()))/hScaleFromGridCellToFullScreen)+24;
//			
////			ret = cellBounds;
//			ret = new PBounds(xOffset, yOffset, cellBounds.getWidth(), cellBounds.getHeight());
		}
			
		return ret;
	}
	
	/**
	 * changes the focused canvas
	 * There is one focused canvas at a time in the viewport.
	 * This method re-renders the whole viewport (all of the layers, all of the images, changes the active camera)
	 * Only the image from the previously focused canvas is actually refreshed
	 * If you want to for some other reason refresh a specific image (e.g. when some other user changed it)
	 *  use the refreshImage method in the CViewportController 
	 * @param p the point that indicates the new canvas in focus
	 */
	public void changeFocusedCanvas(Point p){		 
		//scale the point first of all
		
		
		CCanvasController.canvasdb.get(cuidWorkingCanvas).removeTopMenuBar();
		if(PieMenu.isPieMenuActive()){
			PieMenu.clearMenu();
		}
		long oneCUID = getCanvasIdOfPoint(p);
		
		canvasLayer.removeAllChildren();
		borderLayer.removeAllChildren();
		startUpFocusedCanvas(oneCUID);
		
		drawToolbar();
		//return;
	}

	
	
	private void startUpFocusedCanvas(long oneCUID){
		
		long[] canvases = CCanvasController.getCanvasIDList();
		focusedCanvasCellBounds = CGrid.getInstance().getCellBounds(oneCUID); 
		Calico.logger.debug("");
		double fxOffset = (focusedCanvasCellBounds.getMinX())-xStart;
		double fyOffset = (focusedCanvasCellBounds.getMinY())-yStart;
				
		removeEditableCanvasCamera();				
		setEditableCanvasCamera(oneCUID, fxOffset, fyOffset);
		
		double wScaleFromGridCellToFullScreen = ((double)CGrid.getInstance().getImgw()-8)/(double)(PreferredScreenWidth);
		double hScaleFromGridCellToFullScreen = ((double)CGrid.getInstance().getImgh()-8)/(double)(PreferredScreenHeight);
		
		canvasLayer = new PLayer();
		borderLayer = new PLayer();
				
		canvasImages = new Long2ReferenceOpenHashMap<PImage>();
		canvasImageBounds = new Long2ReferenceOpenHashMap<PBounds>();
		for (int i=0; i<canvases.length;i++){
			
			PBounds cellBounds = CGrid.getInstance().getCellBounds(canvases[i]);
			PLayer oneCLayer = new PLayer();
			//CCanvas canvas = CCanvasController.canvasdb.get(oneCLayerID);
			
			double xOffset = ((cellBounds.getMinX()-(focusedCanvasCellBounds.getMinX()))/wScaleFromGridCellToFullScreen);
			double yOffset = ((cellBounds.getMinY()-(focusedCanvasCellBounds.getMinY()))/hScaleFromGridCellToFullScreen)+24;
			
						
			boolean drawNow = CGrid.getInstance().isCanvasInViewport(canvases[i]);
			canvasesShown.put(canvases[i], new Boolean(drawNow));
			
			//draw the borders of each canvas
			drawCanvasBorders( xOffset, yOffset, canvases[i]);				
			
			
			boolean refresh = false;
			if(canvases[i]==cuidWorkingCanvas){
				refresh=true;
			}
			
			drawCanvas(canvases[i],xOffset, yOffset, oneCLayer, refresh, drawNow);
			
			if(canvases[i]!=oneCUID){				
				canvasLayer.addChild(oneCLayer);
			}
			canvasImageLayers.put(canvases[i], oneCLayer);
			
		}
		
		this.getLayer().addChild(borderLayer);				
		this.getLayer().addChild(canvasLayer);

		//set the new cuidWorkingCanvas
		cuidWorkingCanvas=oneCUID;
//		CCanvasController.setCurrentUUID(cuidWorkingCanvas);
		CCanvasController.initializeCanvas(cuidWorkingCanvas);

	}
	
	private void removeEditableCanvasCamera() {
		getCamera().removeChild(menuBar);
//		getCamera().removeChild(topMenuBar);
		
		this.getLayer().removeChild(borderLayer);
		this.getLayer().removeChild(canvasLayer);
		getCamera().setViewScale(1.0);
		getCamera().setViewOffset(0,0);		
	}

	public boolean isClickOnBoundaries(Point point) {		
		if (getCanvasIdOfPoint(point)==0l){			
			return true;
		}else{			
			return false;
		}
	}
	
	/**
	 * returns the id of the focused canvas (the one we are currently able to edit)
	 * @return
	 */
	public long getCuidWorkingCanvas() {
		return cuidWorkingCanvas;
	}
	
	
	public void rezoomCamera(){
		
		PBounds viewportBounds = CGrid.getInstance().getViewportBounds();
		
		height = viewportBounds.getHeight()-8;
		width = viewportBounds.getWidth()-8;
		
		xStart = viewportBounds.getMinX();
		yStart = viewportBounds.getMinY()-2;
		
		
		//PBounds fCellBounds = CGrid.getInstance().getCellBounds(cuidWorkingCanvas);
		double fxOffset = focusedCanvasCellBounds.getMinX()-xStart;
		double fyOffset = (focusedCanvasCellBounds.getMinY()-yStart);
		
		scaleFromGCellsToVP=CGrid.getInstance().getViewportScale();
		vpXScaleToFullScreen = width / (PreferredScreenWidth);
		vpYScaleToFullScreen = height / (PreferredScreenHeight);
		
		int canvInitX = (int)((fxOffset)/vpXScaleToFullScreen);
		int canvInitY = (int)((fyOffset)/vpYScaleToFullScreen);
		
		//decide if there are any canvases that were outside that now need to be shown 
		Long2ReferenceOpenHashMap<Boolean> canvInVP = CGrid.getInstance().getCanvasesInViewport();
		LongArraySet addedOnes= new LongArraySet();
		LongArraySet removedOnes= new LongArraySet();
		for(long key: canvasesShown.keySet()){
			boolean shown = canvasesShown.get(key).booleanValue();
			Boolean inVPBool = canvInVP.get(key);
			if(inVPBool!=null){
				boolean inVP =inVPBool.booleanValue(); 
				if(inVP==true&&shown==false){
					//now show it
					addedOnes.add(key);
				}else if(inVP==false&&shown==true){
					//remove it
					removedOnes.add(key);
				}
			}
		}
		//for each one I have to add do
		/*if(addedOnes.size()>0||removedOnes.size()>0){
			Calico.logger.debug("Reezoming the camera, IN this case I will remove or include canvases");
		}*/
		for(long cuidAddedCanvas:addedOnes){
			//Calico.logger.debug("Canvas "+ cuidAddedCanvas+" is NOW INSIDE of the viewport, so well include it from the layer");
			// new PImage(CViewportController.getImage(cuid));
			canvasImages.get(cuidAddedCanvas).setImage(CViewportController.getImage(cuidAddedCanvas));
			canvasImages.get(cuidAddedCanvas).setBounds(canvasImageBounds.get(cuidAddedCanvas));
			canvasImageLayers.get(cuidAddedCanvas).addChild(canvasImages.get(cuidAddedCanvas));
			canvasesShown.put(cuidAddedCanvas, new Boolean(true));
		}
		
		//decide if there are any canvases that were NOT outside that now ARE outside		
		//for each one i have to remove do:
		for(long cuidRemovedCanvas:removedOnes){
			//Calico.logger.debug("Canvas "+ cuidRemovedCanvas+" is NOW outside of the viewport, so well remove it from the layer");
			canvasImageLayers.get(cuidRemovedCanvas).removeChild(canvasImages.get(cuidRemovedCanvas));
			Image n=null;
			canvasImages.get(cuidRemovedCanvas).setImage(n);
			CViewportController.dumpImage(cuidRemovedCanvas);
			canvasesShown.put(cuidRemovedCanvas, new Boolean(false));
		}
		
		//Calico.logger.debug("changing camera scale"+scaleFromGCellsToVP);		
		this.getCamera().setViewScale(scaleFromGCellsToVP);		
		this.getCamera().setViewOffset(canvInitX,canvInitY);
		//Calico.logger.debug("changing camera offset: X:"+canvInitX+".Y:"+canvInitY);
	}
	
	
	//
	// 
	/**
	 *checks if an event is inside of a non focused canvas and if so if it is inside of a group in the cell
	 *and if it is returns the id of that group 
	 */
	private long[] getGroupPressedInCell(InputEventInfo ev){
		Calico.logger.debug("CViewportCanvas:isGroupPressed:starting");
		Point point = ev.getPoint();
		
		long clickedCanvasId = getCanvasIdOfPoint(point);
		
		if(clickedCanvasId!=0l && clickedCanvasId !=cuidWorkingCanvas){
			//scale the point relatively to the cell
			//Calico.logger.debug("CViewportCanvas:isGroupPressed:scaling point"+point);
			point = scalePointToCanvas(point, clickedCanvasId);
			//Calico.logger.debug("CViewportCanvas:isGroupPressed:point SCALED"+point);
			
			CCanvas canvas = CCanvasController.canvasdb.get(clickedCanvasId);
			long[] grplist = canvas.getChildGroups();
			if(grplist.length>0)
			{
				LongArrayList groupsAtPoint = new LongArrayList();
				int groupsMatched = 0;

				for(int i=0;i<grplist.length;i++)
				{	
					if(CGroupController.groupdb.get(grplist[i]).isFinished() && CGroupController.groupdb.get(grplist[i]).containsPoint(point.x, point.y))
					{
						groupsAtPoint.add(grplist[i]);
						groupsMatched++;
					}
				}
				if(groupsMatched>0)
				{					
					return groupsAtPoint.toLongArray();
				}
				else
				{
					
					return null;
				}
			}
		}
		return null;
	}
	

	/**
	 *gets the smallest group in the point of an event 
	 */
	public long getSmallestGroupPressedInCell(InputEventInfo ev)
	{
		long[] matches = getGroupPressedInCell(ev);

		// No matches were found.
		if(matches==null)
		{
			return 0L;
		}

		double biggestArea = Double.MAX_VALUE;
		long chosenGUID = 0L;

		for(int i=0;i<matches.length;i++)
		{
			if( CGroupController.groupdb.get(matches[i]).getArea() < biggestArea )
			{
				chosenGUID = matches[i];
				biggestArea = CGroupController.groupdb.get(matches[i]).getArea();
			}//if contained
		}//for groups
		return chosenGUID;
	}
	
	/**
	 * Draws a group selected in a non focused canvas starting in the x,y selected position
	 * @param groupId
	 * @param x
	 * @param y
	 */
	/*public void drawPressedGroup(long groupId, int x, int y){
		pressedGroupLayer = new PLayer();
		pressedGroupImages =  new ArrayList<PImage>();
		CGroup groupToDraw= CGroupController.groupdb.get(groupId);
		long clickedCanvasId = getCanvasIdOfPoint(new Point(x,y));
		//scale the min and max to know where to start drawing
		Point groupStart = new Point((int) groupToDraw.getBounds().getMinX(), (int)groupToDraw.getBounds().getMinY());
		Point scaledGroupStart = scalePointFromCanvas(groupStart, clickedCanvasId);
		//save the offset between the place the user clicked and the starting point where to draw
		draggedGroupOffsetX = x- (int)scaledGroupStart.getX(); 
		draggedGroupOffsetY = y- (int)scaledGroupStart.getY();
		//save the cuid and source point of the origin of the drawn group
		cuidDraggedGroupOriginCanvas = clickedCanvasId;
		draggedGroupStartingPoint = groupStart; 
		idDraggedGroup = groupId;
		//now draw the group
		pressedGroupMainImage = drawContainedGroup(groupToDraw,(int)scaledGroupStart.getX(),(int)scaledGroupStart.getY());	
				
		
		for(PImage p:pressedGroupImages){
			pressedGroupLayer.addChild(p);
		}
		this.getCamera().addChild(pressedGroupLayer);
		repaint();
	}*/
	
	/**
	 * removes a group that was selected a is moving around (possibly cause the user dropped it in another canvas)
	 */
	/*public void removePressedGroup(){
		if(pressedGroupImages!=null){
			pressedGroupLayer.removeAllChildren();
			pressedGroupImages=null;
			this.getCamera().removeChild(pressedGroupLayer);
			cuidDraggedGroupOriginCanvas=0l;
			repaint();
		}
	}*/
	

	/**
	 *draws (as an image) the group, its strokes and arrows, adds the images to pressedGroupImages 
	 */
	/*private PImage drawContainedGroup(CGroup groupToDraw, int x, int y){
		Image img = groupToDraw.toImage();
		int width = (int)groupToDraw.getWidth();
		int height =(int)groupToDraw.getHeight();
		
		//scale the group to the smaller focused canvas size
		double scale = scaleFromGCellsToVP;		
		double scaledWidth = width*scale;
		double scaledHeight = height*scale;
		
		PImage groupImage = new PImage(img);
		groupImage.setBounds(x,y,scaledWidth,scaledHeight);
		pressedGroupImages.add(groupImage);
		
		// get the strokes in the group
		long[] bgelist = groupToDraw.getChildStrokes();
		
		//get the arrows in the group		
		long[] arrowlist = groupToDraw.getChildArrows();
		
		//double cellscale = getCellScale();
		
		//get the bounds of the group in the original canvas to know where to draw the elements inside the group
		PBounds groupBounds = groupToDraw.getBounds();
		double groupImageX= groupBounds.getMinX();
		double groupImageY= groupBounds.getMinY();
		
		//draw all the child strokes
		drawStrokes(bgelist,scale,groupBounds,x,y);
		
		//draw all the child arrows
		drawArrows(arrowlist,scale,groupBounds,x,y);
		
		//draw all the child groups
		long[] grouplist = groupToDraw.getChildGroups();
		for (int i=0;i<grouplist.length;i++){
			
			CGroup subGroupToDraw= CGroupController.groupdb.get(grouplist[i]);
			if(subGroupToDraw!=null){
				//get the x and y offset
				double startX=subGroupToDraw.getBounds().getMinX();
				double startY=subGroupToDraw.getBounds().getMinY();
				double xOffset = (startX - groupImageX)*scale;
				double yOffset = (startY - groupImageY)*scale;
				
				drawContainedGroup(subGroupToDraw,x+(int)xOffset,y+(int)yOffset);
			}
		}
		return groupImage;
	}*/
	
	
	
	/**
	 * draws the strokes inside of a group and adds the images to pressedGroupImages
	 * @param bgelist the list of strokes to draw
	 * @param scale the scale in which to draw the arrows
	 * @param groupBounds the bounds of the group
	 * @param x the starting point
	 * @param y the starting point
	 */
	/*private void drawStrokes(long[] bgelist, double scale,  PBounds groupBounds, int x, int y){
		
		double groupImageX= groupBounds.getMinX();
		double groupImageY= groupBounds.getMinY();
		
		for(int i=0; i<bgelist.length;i++){
			
			if(CStrokeController.exists(bgelist[i])){
				CStroke element=CStrokeController.strokes.get(bgelist[i]);
				
				//get and scale its position relative from the group's position
				
				double startX=element.getBounds().getMinX();
				double startY=element.getBounds().getMinY();
				double xOffset = startX - groupImageX;
				double yOffset = startY - groupImageY;
				
				
				//get and scale the size
				int w= (int)element.getWidth();
				int h=(int)element.getHeight();
				double scW= w*scale;
				double scH= h*scale;
				
				//turn the element to an image of it
				Image stkImage=element.toImage();				
				PImage pimg = new PImage(stkImage);
				
				double startXScaled = xOffset*scale;
				double startYScaled = yOffset*scale;
				
				//set the bounds for the image
				pimg.setBounds(x+startXScaled,y+startYScaled,scW,scH);
				
				pressedGroupImages.add(pimg);
			}
		}	
	}*/
	
	/**
	 * Draws arrows contained in a group
	 * @param arrowlist the list of arrows to draw
	 * @param scale the scale in which to draw the arrows
	 * @param groupBounds the bounds of the group
	 * @param x the starting point
	 * @param y the starting point
	 */
	/*private void drawArrows(long[] arrowlist, double scale,  PBounds groupBounds, int x, int y){
		
		double groupImageX= groupBounds.getMinX();
		double groupImageY= groupBounds.getMinY();
		
		for(int i=0; i<arrowlist.length;i++){
			
			//if(CArrowController.exists(arrowlist[i])){
			CArrow element=CArrowController.arrows.get(arrowlist[i]);
			
			//get and scale its position relative from the group's position
			
			double startX=element.getBounds().getMinX();
			double startY=element.getBounds().getMinY();
			double xOffset = startX - groupImageX;
			double yOffset = startY - groupImageY;
			
			
			//get and scale the size
			int w= (int)element.getWidth();
			int h=(int)element.getHeight();
			double scW= w*scale;
			double scH= h*scale;
			
			//turn the element to an image of it
			Image stkImage=element.toImage();				
			PImage pimg = new PImage(stkImage);
			
			double startXScaled = xOffset*scale;
			double startYScaled = yOffset*scale;
			
			//set the bounds for the image
			pimg.setBounds(x+startXScaled,y+startYScaled,scW,scH);
			
			pressedGroupImages.add(pimg);
			//}
		}	
	}*/
	
	
	/**
	 * moves a pressed group (and everything inside) to the new point 
	 */	
	/*public void movePressedGroup(int x, int y){
		//if there are pressed group images 
		if(pressedGroupImages!=null){
			//remove everything from the layer
			pressedGroupLayer.removeAllChildren();
			
			//add the offset of the drawn group 
			
			x = x-draggedGroupOffsetX;
			y = y-draggedGroupOffsetY;
			//get the bounds from the head group
			PBounds groupBounds = pressedGroupMainImage.getBounds();
			double groupImageX= groupBounds.getMinX();
			double groupImageY= groupBounds.getMinY();
			
			//the actual amount to move the images inside the group
			double xOffset = x - groupImageX;
			double yOffset = y - groupImageY;
			
			for(PImage p:pressedGroupImages){
				//get the bounds for this image
				PBounds imgBounds =p.getBounds();
				double imageX= imgBounds.getMinX();
				double imageY= imgBounds.getMinY();
				
				//set the new bounds by adding the offset to the previous position				
				p.setBounds(xOffset+imageX,yOffset+imageY,p.getWidth(),p.getHeight());
				pressedGroupLayer.addChild(p);
			}			
		}
	}*/
	
	/**
	 * Returns the id of the canvas where we picked up a group we are dragging
	 * @return
	 */
	/*public long getCuidDraggedGroupOriginCanvas() {
		return cuidDraggedGroupOriginCanvas;
	}*/
	
	/**
	 * gets the id of the drawn group
	 * @return
	 */
	/*public long getIdDraggedGroup() {
		return idDraggedGroup;
	}*/

	/*public Point getDraggedGroupStartingPoint() {
		return draggedGroupStartingPoint;
	}
	
	public int getDraggedGroupOffsetX() {
		return draggedGroupOffsetX;
	}
	
	public int getDraggedGroupOffsetY() {
		return draggedGroupOffsetY;
	}*/
	
	public void refreshImage(long cuid ){		
		if(canvasesShown.get(cuid)!=null&&canvasesShown.get(cuid).booleanValue()&&(cuid!=cuidWorkingCanvas)){			 
			canvasImages.get(cuid).setImage(CViewportController.refreshImageInDB(cuid));
			canvasImages.get(cuid).setBounds(canvasImageBounds.get(cuid));
			canvasImageLayers.get(cuid).addChild(canvasImages.get(cuid));			
		}
	}
	
	public double getVpXScaleToFullScreen() {
		return vpXScaleToFullScreen;
	}
	
	public double getVpYScaleToFullScreen() {
		return vpYScaleToFullScreen;
	}
	
	/**
	 * refreshes all images show if for example we change the size of the viewport 
	 */
	public void refreshShownImages() {
		for(long key: canvasesShown.keySet()){
			refreshImage(key);
		}
	}
	
}
