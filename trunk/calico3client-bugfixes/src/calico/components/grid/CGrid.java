package calico.components.grid;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.JComponent;

import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.components.CCanvas;
import calico.components.menus.GridBottomMenuBar;
import calico.controllers.CCanvasController;
import calico.input.CalicoMouseListener;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.perspectives.GridPerspective;
import calico.utils.Geometry;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;
import edu.umd.cs.piccolox.nodes.PLine;

/**
 * This is the grid system for calico
 *
 * @author Mitch Dempsey
 */
public class CGrid 
{
	private static final long serialVersionUID = 1L;
	
	//public static final int MODE_NONE = 0;
	//public static final int MODE_VIEWPORT = 1;
	
	private final ContainedCanvas canvas = new ContainedCanvas();

	private Long2ReferenceOpenHashMap<CGridCell> cells = new Long2ReferenceOpenHashMap<CGridCell>();
	private PLayer cellLayer;

	public static int gwidth = 0;
	public static int gheight = 0;

	private int imgw = 0;
	private int imgh = 0;
	
	private GridBottomMenuBar menuBar = null;
	
	//public static int mode = MODE_NONE;

	public static CGrid instance;
	
	//attributes for dragging a cell to copy or move
	public static boolean draggingCell=false;
	private PImage pressedCellMainImage;
	private long cuidDraggedCanvas;
	//Copy canvas deletes from the destination cell and copies the contents of the selected cell (does not delete selected cell)
	public static final int COPY_CANVAS=1;	
	//Cut canvas removes the existing content from the selected cell
	public static final int CUT_CANVAS=2;
	
	
	public static final int NO_ACTION=0;
	public static int canvasAction=NO_ACTION;
	
	public static int moveDelta=1;
	public static int moveDelay=100;
	
	public static CGrid getInstance(){
		if(instance==null){
			instance = new CGrid();
		}
		//instance.drawBottomToolbar();
		instance.repaint();
		return instance;
	}
	
	public PCamera getCamera()
	{
		return canvas.getCamera();
	}
	
	public PLayer getLayer()
	{
		return canvas.getLayer();
	}
	
	public JComponent getComponent()
	{
		return canvas;
	}
	
	public void repaint()
	{
		canvas.repaint();
	}
	
	public void setBounds(int x, int y, int w, int h)
	{
		canvas.setBounds(x, y, w, h);
	}
	
	/**
	 * Creates a new isntance of the grid
	 */
	private CGrid()
	{
//		CGrid.exitButtonBounds = new Rectangle(CalicoDataStore.ScreenWidth-32,5,24,24);

		CalicoDataStore.gridObject = this;
		
		canvas.setPreferredSize(new Dimension(CalicoDataStore.ScreenWidth, CalicoDataStore.ScreenHeight));
		setBounds(0, 0, CalicoDataStore.ScreenWidth, CalicoDataStore.ScreenHeight);

		CalicoInputManager.addGridInputHandler();

		canvas.removeInputSources();

		canvas.addMouseListener(new CalicoMouseListener());
		canvas.addMouseMotionListener(new CalicoMouseListener());

		canvas.removeInputEventListener(canvas.getPanEventHandler());
		canvas.removeInputEventListener(canvas.getZoomEventHandler());


//		PText pt = new PText(" Calico Grid ("+CalicoDataStore.Username+") ");
//		pt.setConstrainWidthToTextWidth(true);
//		pt.setFont(new Font("SansSerif", Font.BOLD, 20));
//		pt.setTextPaint(Color.BLACK);
//		pt.setBounds(5,5,500,29);
//		getLayer().addChild(0,pt);

		
		//create the exit image on top of the grid
//		try
//		{
//			PImage b = new PImage( new URL(CalicoIconManager.getIcon("exit")) );
//			b.setBounds(CGrid.exitButtonBounds);
//			getLayer().addChild(0,b);
//		}
//		catch(Exception e)
//		{
//		}



		repaint();

		drawBottomToolbar();
		
		int bottomMenuBarHeight = this.menuBar.getBounds().getBounds().height;

		int topHeaderIconHeight = CalicoOptions.grid.topHeaderIconHeight;
		int leftHeaderIconWidth = CalicoOptions.grid.leftHeaderIconWidth;

		imgw = (int) Math.ceil( (CalicoDataStore.ScreenWidth - leftHeaderIconWidth) / CalicoDataStore.GridCols );
		imgh = (int) Math.ceil( (CalicoDataStore.ScreenHeight - topHeaderIconHeight - bottomMenuBarHeight) / CalicoDataStore.GridRows );

		CGrid.gwidth = imgw;
		CGrid.gheight = imgh;
		
		//add header labels... A... B... etc
		PText headerIcon;
		for (int i = 0; i < CalicoDataStore.GridCols; i++)
		{
			headerIcon = new PText(" " + ((char)('A' + i)) + " ");
			headerIcon.setConstrainWidthToTextWidth(true);
			headerIcon.setFont(new Font("SansSerif", Font.BOLD, 20));
			headerIcon.setTextPaint(Color.BLACK);
			Rectangle textBounds = Geometry.getTextBounds(headerIcon.getText(), headerIcon.getFont());
			headerIcon.setBounds(leftHeaderIconWidth + imgw*(i+1) - imgw/2 - textBounds.width/2,5,textBounds.width,textBounds.height);
			getLayer().addChild(0,headerIcon);	
		}
		
		for (int i = 0; i < CalicoDataStore.GridRows; i++)
		{
			headerIcon = new PText(" " + ((char)('1' + i)) + " ");
			headerIcon.setConstrainWidthToTextWidth(true);
			headerIcon.setFont(new Font("SansSerif", Font.BOLD, 20));
			headerIcon.setTextPaint(Color.BLACK);
			Rectangle textBounds = Geometry.getTextBounds(headerIcon.getText(), headerIcon.getFont());
			headerIcon.setBounds(5,topHeaderIconHeight + imgh*(i+1) - imgh/2 - textBounds.width/2,textBounds.width,textBounds.height);
			getLayer().addChild(0,headerIcon);	
		}

		//sets the initial sizes of the viewports in the viewport controller
		int cellindex = 0;

		long[] canvasuids = CCanvasController.getCanvasIDList();
		cellLayer = new PLayer();	
		for(int can=0;can<canvasuids.length;can++)
		{
			long canuuid = canvasuids[can];
			CGridCell img = new CGridCell(canuuid, cellindex,30,30,imgw,imgh);
			cellLayer.addChild(img);
			cells.put(canuuid, img);			
			cellindex++;
		}
		getLayer().addChild(cellLayer);
		repaint();
	}
	
	
	public void refreshCells(){
		cells = new Long2ReferenceOpenHashMap<CGridCell>();
		getLayer().removeChild(cellLayer);
		cellLayer = new PLayer();
		int cellindex = 0;

		long[] canvasuids = CCanvasController.getCanvasIDList();
			
		for(int can=0;can<canvasuids.length;can++)
		{
			long canuuid = canvasuids[can];
			CGridCell img = new CGridCell(canuuid, cellindex,30,30,imgw,imgh);
			cellLayer.addChild(img);
			cells.put(canuuid, img);;
			cellindex++;
		}
		getLayer().addChild(cellLayer);
		//drawBottomToolbar();
		repaint();
	}
	
	public void drawBottomToolbar()
	{		
		GridBottomMenuBar temp = new GridBottomMenuBar(1L);
		getCamera().addChild(temp);
		
		if(this.menuBar!=null)
		{
			getCamera().removeChild(this.menuBar);
			this.menuBar = null;
		}
		
		this.menuBar = temp;
	}

	/**
	 * Updates the cells if they have changed
	 */
	public void updateCells()
	{
		for(int i=0;i<cells.size();i++)
		{
			if (cells.get(i) != null)
				
				cells.get(i).renderIfChanged();
				
		}
	}

	public boolean isPointOnMenuBar(Point point)
	{
		return (this.menuBar.isPointInside(point));
	}

	public void clickMenuBar(Point point) {
		this.menuBar.clickMenu(point);	
	}
	
	/**
	 * gets the bounds of a cell 
	 * @param cuid
	 * @return
	 */
	public PBounds getCellBounds(long cuid){
		CGridCell cell = cells.get(cuid);
		if(cell!= null){
			return cell.getBounds();		
		}
		return null;
	}
	
	public int getImgw() {
		return imgw;
	}

	public int getImgh() {
		return imgh;
	}
	
	public void addMouseListener(MouseListener listener)
	{
		canvas.addMouseListener(listener);
	}
	
	public void removeMouseListener(MouseListener listener)
	{
		canvas.removeMouseListener(listener);
	}
	
	/**
	 * Draws a slightly smaller, yellow background version of a cell in the given position
	 * @param cuid
	 * @param x
	 * @param y
	 */
	public void drawSelectedCell(long cuid, int x, int y){		
		if(!draggingCell){
			draggingCell=true;
			CCanvas canvas = CCanvasController.canvasdb.get(cuid);
			PCamera canvasCam =canvas.getContentCamera();		
//			canvasCam.removeChild(canvas.menuBar);
//			canvasCam.removeChild(canvas.topMenuBar);
			Image img = canvasCam.toImage(imgw-16, imgh-16, Color.YELLOW);			
			pressedCellMainImage =  new PImage(img);
			
			pressedCellMainImage.setBounds(x-((imgw-24)/2), y-((imgh-24)/2), imgw-24, imgh-24);
			pressedCellMainImage.setTransparency(CalicoOptions.group.background_transparency);
			getLayer().addChild(pressedCellMainImage);
			cuidDraggedCanvas=cuid;			
		}
	}
	
	/**
	 * removes the dragged cell after copying or cutting
	 */
	public void removeDraggedCell(){
		if(pressedCellMainImage!=null){
			getLayer().removeChild(pressedCellMainImage);
			pressedCellMainImage=null;
			draggingCell=false;
			cuidDraggedCanvas=0l;
			canvasAction=NO_ACTION;
		}
	}
	
	/**
	 * moves a cell that is beeign dragged to cut or copy a canvas
	 * @param x the new x point to drag to
	 * @param y the new y point to drag to
	 */
	public void moveDraggedCell(int x, int y){
		pressedCellMainImage.setBounds(x-((imgw-24)/2), y-((imgh-24)/2), imgw-24, imgh-24);		
	}


	/**
	 * deletes the contents of a canvas
	 * prompts the user to confirm before deleting
	 * @param cuid he canvas id to delete
	 */
	public void deleteCanvas(long cuid) {
		//send the message to delete the contents of the canvas
		Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_CLEAR, 
				cuid				
			));
	}
	/**
	 * executes the action to copy or cut a canvas from the dragged canvas id to the destination canvas in the point signaled by the event 
	 * @param ev the event where the button was released
	 */
	public void execActionCanvas( InputEventInfo ev){
		long cuidDest = CCanvasController.getCanvasAtPoint( ev.getPoint() );
		if(cuidDest!=0l){

			//send package to add the contents from the source to dest cell
			Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_COPY, 
					cuidDraggedCanvas, 
					cuidDest				
				));
			if(canvasAction==CUT_CANVAS){
				//if cut was the action removes the contents of the source canvas				
				//send package to delete contents from source cell
				Networking.send(CalicoPacket.getPacket(NetworkCommand.CANVAS_CLEAR, 
						cuidDraggedCanvas				
					));
				if (CCanvasController.canvasdb.get(cuidDraggedCanvas).getLockValue() == true)
				{
					CCanvasController.lock_canvas(cuidDraggedCanvas, false, "clean move action", (new Date()).getTime());
					CCanvasController.lock_canvas(cuidDest, true, "clean move action", (new Date()).getTime());
				}
			}		
		}
	}
	
	private PLine drawDashedBorderLine(double x,double y, double x2, double y2)
	{
		PLine pline = new PLine();
		pline.addPoint(0, x, y);
		pline.addPoint(1, x2, y2);
		float[] dashFloat = {5f, 10f};
		pline.setStroke(new BasicStroke( 5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, dashFloat ,0f));
		pline.setStrokePaint( CalicoOptions.grid.item_border );
		return pline;
	}
	

	private PComposite clientListPopup = null;
	
	public void drawClientList(Rectangle boundingBox) {
		
		if(this.clientListPopup!=null) {
			this.clientListPopup.removeFromParent();
			CalicoDataStore.calicoObj.getContentPane().getComponent(0).repaint();
			this.clientListPopup = null;
			return;
		}
		
		this.clientListPopup = new PComposite();
		
		StringBuilder str = new StringBuilder();
		str.append("Clients on this server:");
		
		if(CCanvasController.getGlobalClientCount()==0)
		{
			str.append("\nNo clients are on a canvas");
		}
		else
		{
		
			long[] cuids = CCanvasController.canvasdb.keySet().toLongArray();
			for(int x=0; x < cuids.length; x++)
			{
				int[] clients = CCanvasController.canvasdb.get(cuids[x]).getClients();
				if(clients.length>0) {
					for(int i=0;i<clients.length;i++) {
						if(CalicoDataStore.clientInfo.containsKey(clients[i])) {
							str.append("\n"+CalicoDataStore.clientInfo.get(clients[i])+" ("+CCanvasController.canvasdb.get(cuids[x]).getGridCoordTxt()+")");
						} else {
//							str.append("\nUnknown_"+clients[i]+" ("+CCanvasController.canvasdb.get(cuids[x]).getGridCoordTxt()+")");
						}
						
					}
				}
			}
		}
		
		PText text = new PText(str.toString());
		text.setConstrainWidthToTextWidth(true);
		text.setFont(new Font(CalicoOptions.messagebox.font.name, Font.BOLD, CalicoOptions.messagebox.font.size));
		text.setConstrainHeightToTextHeight(true);
		text.setConstrainWidthToTextWidth(true);
		text.setTextPaint(CalicoOptions.messagebox.color.text);
		
		text.recomputeLayout();
		Rectangle ntextbounds = text.getBounds().getBounds();
		

		int padding = CalicoOptions.messagebox.padding;
		int padadd = padding*2;
		
		
		int lastVertPos = 30;
		
		
		
		
		//int vertpos = CCanvasController.canvasdb.get(canvas_uid).messageBoxOffset;
		

		Rectangle bounds = new Rectangle(16,lastVertPos,ntextbounds.width+padadd,ntextbounds.height+padadd);
		
		bounds.setLocation(boundingBox.x, boundingBox.y-ntextbounds.height-30);

		Rectangle textbounds = new Rectangle(bounds.x+padding,bounds.y+padding,ntextbounds.width,ntextbounds.height);

		PNode bgnode = new PNode();
		bgnode.setPaint(CalicoOptions.messagebox.color.notice);
		
		bgnode.setBounds(bounds);
		
		
		text.setBounds(textbounds);

		this.clientListPopup.addChild(0,bgnode);
		this.clientListPopup.addChild(1,text);
		this.clientListPopup.setBounds(bounds);
		

		((PCanvas)CalicoDataStore.calicoObj.getContentPane().getComponent(0)).getCamera().addChild(0, this.clientListPopup);
		CalicoDataStore.calicoObj.getContentPane().getComponent(0).repaint();
		((PCanvas)CalicoDataStore.calicoObj.getContentPane().getComponent(0)).getCamera().repaint();
		
		Rectangle newBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height+padding);
	}



	public void updateCell(long canvas) {
		if (cells.containsKey(canvas))
			cells.get(canvas).refreshImage();
	}
	
	public void updateCellPresenceText(long canvas) {
		cells.get(canvas).updatePresenceText();
	}
	
	public static void loadGrid()
	{
		CalicoDataStore.gridObject = CGrid.getInstance();
		CalicoDataStore.gridObject.refreshCells();
		CalicoDataStore.calicoObj.getContentPane().removeAll();
		
		Component[] comps = CalicoDataStore.calicoObj.getContentPane().getComponents();
		
		CalicoDataStore.gridObject.drawBottomToolbar();
		CalicoDataStore.calicoObj.getContentPane().add( CalicoDataStore.gridObject.getComponent() );
		
		for (int i = 0; i < comps.length; i++)
			CalicoDataStore.calicoObj.getContentPane().remove(comps[i]);
		
		CalicoDataStore.calicoObj.pack();
		CalicoDataStore.calicoObj.setVisible(true);
		CalicoDataStore.calicoObj.repaint();
		GridPerspective.getInstance().activate();
	}

	private class ContainedCanvas extends PCanvas
	{
		@Override
		protected void removeInputSources()
		{
			super.removeInputSources();
		}
	}
}//CGrid
