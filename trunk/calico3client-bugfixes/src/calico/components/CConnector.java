package calico.components;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import org.apache.log4j.Logger;

import calico.Calico;
import calico.CalicoDraw;
import calico.CalicoOptions;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.controllers.CStrokeController;
import calico.networking.netstuff.ByteUtils;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.plugins.analysis.components.activitydiagram.ControlFlow;
import calico.Geometry;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CConnector extends PComposite{
	
	private static Logger logger = Logger.getLogger(CConnector.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private long uuid = 0L;
	private long canvasUID = 0L;
	
	private Color color = null;
	
	Stroke stroke;
	Color strokePaint;
	float thickness;
	
	final public static int TYPE_HEAD = 1;
	final public static int TYPE_TAIL = 2;
	private long anchorHeadUUID = 0l;
	private long anchorTailUUID = 0l;
	
	//The components that are drawn by Piccolo
	private PPath connectorHead = null;
	private PPath connectorTail = null;
	private PPath connectorLine = null;
	
	//The data model for the connector
	private Point pointHead = null;
	private Point pointTail = null;
	//Orthogonal distance from the direct head to tail line
	private double[] orthogonalDistance;
	//Percent along the direct head to tail line (Percentage in decimal format; Can be negative)
	private double[] travelDistance;
	
	private boolean isHighlighted = false;
	
	// This will hold the bubble menu buttons (Class<?>)
	private static ObjectArrayList<Class<?>> bubbleMenuButtons = new ObjectArrayList<Class<?>>(); 
	
	public CConnector(long uuid, long cuid, Color color, float thickness, Polygon points)
	{
		this.uuid = uuid;
		this.canvasUID = cuid;
		this.color = color;
		this.thickness = thickness;
		
		pointHead = new Point(points.xpoints[points.npoints-1], points.ypoints[points.npoints-1]);
		pointTail = new Point(points.xpoints[0], points.ypoints[0]);
		this.anchorHeadUUID = CGroupController.get_smallest_containing_group_for_point(cuid, pointHead);
		this.anchorTailUUID = CGroupController.get_smallest_containing_group_for_point(cuid, pointTail);
		
		//pointHead and pointTail must already be assigned
		createDataModelFromPolygon(points);

		stroke = new BasicStroke( thickness );
		strokePaint = this.color;

		resetBounds();
		redraw();
	}
	
	
	public CConnector(long uuid, long cuid, Color color, float thickness, Polygon polygon, long anchorHead, long anchorTail)
	{
		this.uuid = uuid;
		this.canvasUID = cuid;
		this.color = color;
		this.thickness = thickness;
		this.anchorHeadUUID = anchorHead;
		this.anchorTailUUID = anchorTail;
		
		pointHead = new Point(polygon.xpoints[polygon.npoints-1], polygon.ypoints[polygon.npoints-1]);
		pointTail = new Point(polygon.xpoints[0], polygon.ypoints[0]);
		
		//pointHead and pointTail must already be assigned
		createDataModelFromPolygon(polygon);
		
		stroke = new BasicStroke( thickness );
		strokePaint = this.color;

		resetBounds();
		redraw();
	}
	
	public CConnector(long uuid, long cuid, Color color, float thickness, Point head, Point tail, double[] orthogonalDistance, double[] travelDistance,
			 long anchorHead, long anchorTail)
	{
		this.uuid = uuid;
		this.canvasUID = cuid;
		this.color = color;
		this.thickness = thickness;
		this.anchorHeadUUID = anchorHead;
		this.anchorTailUUID = anchorTail;
		
		pointHead = head;
		pointTail = tail;
		
		this.orthogonalDistance = orthogonalDistance;
		this.travelDistance = travelDistance;
		
		stroke = new BasicStroke( thickness );
		strokePaint = this.color;

		resetBounds();
		redraw();
	}
	
	/**
	 * Builds the connector data model from a polygon
	 * @param points
	 */
	public void createDataModelFromPolygon(Polygon points)
	{
		orthogonalDistance = new double[points.npoints];
		travelDistance = new double[points.npoints];

		for (int i = 0; i < points.npoints; i++)
		{
			//Set the distance from the point to the the direct line from the tail to the head
			orthogonalDistance[i] = Geometry.distance(pointTail.x, pointTail.y, pointHead.x, pointHead.y, points.xpoints[i], points.ypoints[i]);
			
			double[] vectorTail = {pointTail.getX(), pointTail.getY()};
			double[] vectorHead = {pointHead.getX(), pointHead.getY()};
			double[] vectorCurrent = {points.xpoints[i], points.ypoints[i]};
			
			//Get which side of the tail to head line the point is on so we can see if we need to flip the distance
			double side = Geometry.getSide(vectorTail, vectorHead, vectorCurrent);
			
			//Set the point's distance to negative depending on what side of the direct line it is on
			if (side < 0)
			{
				orthogonalDistance[i] = -orthogonalDistance[i];
			}
			
			
			
			double[] intersectingPoint = Geometry.computeIntersectingPoint(pointTail.x, pointTail.y, pointHead.x, pointHead.y, points.xpoints[i], points.ypoints[i]);
			
			//Calculate the lengths from the tail and head to the current point, and also the length from the tail to the head
			double lengthTailToPoint = Geometry.length(pointTail.getX(), pointTail.getY(), intersectingPoint[0], intersectingPoint[1]);
			double lengthTailToHead = Geometry.length(pointTail.getX(), pointTail.getY(), pointHead.getX(), pointHead.getY());
			double lengthPointToHead = Geometry.length(intersectingPoint[0], intersectingPoint[1], pointHead.getX(), pointHead.getY());
			
			//Store the percent along the tail to head line that the point is perpendicular to
			travelDistance[i] = lengthTailToPoint / lengthTailToHead;
			
			//Set percent to negative if it is behind the tail point
			if (lengthPointToHead > lengthTailToHead && lengthTailToPoint < lengthPointToHead)
			{
				travelDistance[i] = -travelDistance[i];
			}
			
			
		}
	}
	
	public long getCanvasUUID()
	{
		return this.canvasUID;
	}
	
	//Calculate the raw polygon from the internal data model
	public Polygon getRawPolygon()
	{
		Polygon points = new Polygon();
		double[] tail = {pointTail.getX(), pointTail.getY()};
		double[] head = {pointHead.getX(), pointHead.getY()};
		double dx = pointHead.getX() - pointTail.getX();
		double dy = pointHead.getY() - pointTail.getY();
		double idx = -dy;
		double idy = dx;
		double magnitude = Math.sqrt((Math.pow(idx, 2) + Math.pow(idy, 2)));
		for (int i = 0; i < travelDistance.length; i++)
		{
			double[] pointOnTailHead = Geometry.computePointOnLine(tail[0],tail[1], head[0], head[1], travelDistance[i]);
			double x = pointOnTailHead[0] + (orthogonalDistance[i] * (idx / magnitude));
			double y = pointOnTailHead[1] + (orthogonalDistance[i] * (idy / magnitude));
			points.addPoint((int)x, (int)y);
		}
		
		return points;
	}
	
	public Polygon getPolygon()
	{
		return calico.utils.Geometry.getPolyFromPath(connectorLine.getPathReference().getPathIterator(null));
	}
	
	public double[] getOrthogonalDistance()
	{
		return orthogonalDistance;
	}
	
	public double[] getTravelDistance()
	{
		return travelDistance;
	}
	
	public Point getHead()
	{
		return pointHead;
	}
	
	public Point getTail()
	{
		return pointTail;
	}
	
	public void delete()
	{		
		// remove from canvas
		CCanvasController.no_notify_delete_child_connector(this.canvasUID, this.uuid);
		
		//Remove from groups
		CGroupController.no_notify_delete_child_connector(this.getAnchorUUID(TYPE_HEAD), uuid);
		CGroupController.no_notify_delete_child_connector(this.getAnchorUUID(TYPE_TAIL), uuid);
		
		if(CCanvasController.canvas_has_child_connector_node(this.canvasUID, uuid))
		{
			//This line is not thread safe so must invokeLater to prevent eraser artifacts.
			/*SwingUtilities.invokeLater(
					new Runnable() { public void run() { removeFromParent(); } }
			);*/
			CalicoDraw.removeNodeFromParent(this);
			//removeFromParent();
		}
	}
	
	public void linearize()
	{
		orthogonalDistance = new double[]{0.0, 0.0};
		travelDistance = new double[]{0.0, 1.0};
		
		redraw();
	}
	
	public void setAnchorUUID(long uuid, int anchorType)
	{
		switch(anchorType)
		{
		case TYPE_HEAD: anchorHeadUUID = uuid;
			break;
		case TYPE_TAIL: anchorTailUUID = uuid;
			break;
		}
	}
	
	public long getAnchorUUID(int anchorType)
	{
		switch(anchorType)
		{
		case TYPE_HEAD: return anchorHeadUUID;

		case TYPE_TAIL: return anchorTailUUID;

		default: return 0l;
		}
	}
	
	public Point getAnchorPoint(long guuid)
	{
		if (anchorHeadUUID == guuid)
		{
			return pointHead;
		}
		else if (anchorTailUUID == guuid)
		{
			return pointTail;
		}
		return null;
	}
	
	public long getUUID()
	{
		return this.uuid;
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public float getThickness()
	{
		return thickness;
	}
	
	@Override
	public PBounds getBounds()
	{
		Rectangle bounds = connectorLine.getBounds().getBounds();
		double buffer = 30;
		PBounds bufferBounds = new PBounds(bounds.getX() - buffer, bounds.getY() - buffer, bounds.getWidth() + buffer * 2, bounds.getHeight() + buffer * 2);
		return bufferBounds;
	}
	
	public void redraw()
	{
		addRenderingElements();
		CalicoDraw.removeAllChildrenFromNode(this);

		CalicoDraw.addChildToNode(this, connectorHead, 0);
		CalicoDraw.addChildToNode(this, connectorTail, 0);
		CalicoDraw.addChildToNode(this, connectorLine, 0);
		//this.repaint();
		CalicoDraw.repaintNode(this);
	}
	
	protected void addRenderingElements()
	{
		Polygon linePoints = getRawPolygon();
		
		double[] p0 = new double[]{linePoints.xpoints[linePoints.npoints-1], linePoints.ypoints[linePoints.npoints-1], 0};
		int pointForArrow = Math.min(10, linePoints.npoints);
		double[] p1 = new double[]{linePoints.xpoints[linePoints.npoints-pointForArrow], linePoints.ypoints[linePoints.npoints-pointForArrow], 0};
		Geometry.extendLine(p0, p1, 50);

		connectorHead = null;
		//int[] apoints = Geometry.createArrow(linePoints.xpoints[0], linePoints.ypoints[0], linePoints.xpoints[linePoints.npoints-1], linePoints.ypoints[linePoints.npoints-1],
		//		CalicoOptions.arrow.length, CalicoOptions.arrow.angle, CalicoOptions.arrow.inset);
		int[] apoints = Geometry.createArrow((int)p1[0], (int)p1[1], (int)p0[0], (int)p0[1],
				CalicoOptions.arrow.length, CalicoOptions.arrow.angle, CalicoOptions.arrow.inset);

		connectorHead = new PPath();
		connectorHead.moveTo((float) apoints[0], (float) apoints[1]);
		for (int i = 2; i < apoints.length; i = i + 2)
		{
			connectorHead.lineTo((float) apoints[i], (float) apoints[i + 1]);
		}
		connectorHead.setStroke(new BasicStroke(CalicoOptions.arrow.stroke_size));
		connectorHead.setStrokePaint(this.color);
		connectorHead.setPaint(this.color);

		//int[] bpoints = Geometry.createArrow(mousePoints.xpoints[mousePoints.npoints-1], mousePoints.ypoints[mousePoints.npoints-1], mousePoints.xpoints[0], mousePoints.ypoints[0],
		//		CalicoOptions.arrow.length, CalicoOptions.arrow.angle, CalicoOptions.arrow.inset);
		int[] bpoints = Geometry.createCircle(linePoints.xpoints[0], linePoints.ypoints[0], 3);

		connectorTail = new PPath();
		connectorTail.moveTo((float) bpoints[0], (float) bpoints[1]);
		for (int i = 2; i < bpoints.length; i = i + 2)
		{
			connectorTail.lineTo((float) bpoints[i], (float) bpoints[i + 1]);
		}
		connectorTail.setStroke(new BasicStroke(CalicoOptions.arrow.stroke_size));
		connectorTail.setStrokePaint(this.color);
		connectorTail.setPaint(this.color);


		connectorLine = new PPath();
		connectorLine.setStroke(stroke);
		connectorLine.setStrokePaint(this.color);
		//connectorLine.setPaint(strokePaint);
		applyAffineTransform(linePoints);

		//this.addChild(0, arrowLine);
		//CalicoDraw.addChildToNode(this, arrowLine, 0);
	}
	
	@Override
	protected void paint(final PPaintContext paintContext)
	{
		final Graphics2D g2 = paintContext.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Composite temp = g2.getComposite();
		if (isHighlighted)
		{
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, CalicoOptions.stroke.background_transparency));
			g2.setStroke(new BasicStroke(CalicoOptions.pen.stroke_size + 8));
			g2.setPaint(Color.blue);
			g2.draw(connectorLine.getPathReference());
		}	
		
		//g2.draw(connectorLine.getPathReference());
		
		g2.setComposite(temp);
		//super.paint(paintContext);
		
		
	}
	
	public void highlight_on() {
		isHighlighted = true;
		highlight_repaint();
	}
	
	public void highlight_off() {
		isHighlighted = false;
		highlight_repaint();
	}

	public void highlight_repaint()
	{
		CalicoDraw.repaintNode(CCanvasController.canvasdb.get(canvasUID).getLayer(), this.getBounds(), this);
	}
	
	public void moveAnchor(long guuid, int deltaX, int deltaY)
	{
		if (anchorHeadUUID == guuid)
		{
			pointHead.setLocation(pointHead.x + deltaX, pointHead.y + deltaY);
		}
		if (anchorTailUUID == guuid)
		{
			pointTail.setLocation(pointTail.x + deltaX, pointTail.y + deltaY);
		}
		redraw();
	}
	
	public void moveAnchor(int type, int deltaX, int deltaY)
	{
		if (type == TYPE_HEAD)
		{
			pointHead.setLocation(pointHead.x + deltaX, pointHead.y + deltaY);
		}
		else if (type == TYPE_TAIL)
		{
			pointTail.setLocation(pointTail.x + deltaX, pointTail.y + deltaY);
		}
		redraw();
	}

	
	
	protected void applyAffineTransform(Polygon points)
	{
		Rectangle oldBounds = getBounds().getBounds();
		
		PAffineTransform piccoloTextTransform = getPTransform(points);
		GeneralPath p = (GeneralPath) getBezieredPoly(points).createTransformedShape(piccoloTextTransform);
		connectorLine.setPathTo(p);
//		if (p.getBounds().width == 0 || p.getBounds().height == 0)
//		{
//			this.setBounds(new java.awt.geom.Rectangle2D.Double(p.getBounds2D().getX(), p.getBounds2D().getY(), 1d, 1d));
//		}
//		else
//		{
//			this.setBounds(p.getBounds());
//		}
//		this.repaintFrom(this.getBounds(), this);
//		invalidatePaint();
		
//		CCanvasController.canvasdb.get(canvasUID).getCamera().validateFullPaint();

		//CCanvasController.canvasdb.get(canvasUID).getCamera().repaintFrom(new PBounds(Geometry.getCombinedBounds(new Rectangle[] {oldBounds, this.getBounds().getBounds()})), this);
		CalicoDraw.repaintNode(CCanvasController.canvasdb.get(canvasUID).getCamera(), new PBounds(calico.utils.Geometry.getCombinedBounds(new Rectangle[] {oldBounds, this.getBounds().getBounds()})), this);
	}
	
	public PAffineTransform getPTransform(Polygon points) {
		PAffineTransform piccoloTextTransform = new PAffineTransform();
		return piccoloTextTransform;
	}
	
	public GeneralPath getBezieredPoly(Polygon pts)
	{
		GeneralPath p = new GeneralPath();
		if (pts.npoints > 0)
		{
			p.moveTo(pts.xpoints[0], pts.ypoints[0]);
			if (pts.npoints >= 4)
			{
				int counter = 1;
				for (int i = 1; i+2 < pts.npoints; i += 3)
				{
					p.curveTo(pts.xpoints[i], pts.ypoints[i], 
							pts.xpoints[i+1], pts.ypoints[i+1], 
							pts.xpoints[i+2], pts.ypoints[i+2]);
					counter += 3;
				}
				while (counter < pts.npoints)
				{
					p.lineTo(pts.xpoints[counter], pts.ypoints[counter]);
					counter++;
				}
			}
			else
			{
				for (int i = 1; i < pts.npoints; i++)
				{
					p.lineTo(pts.xpoints[i], pts.ypoints[i]);
				}
			}
		}
		return p;
	}
	
	public CalicoPacket[] getUpdatePackets(long uuid, long cuid)
	{			
		int packetSize = ByteUtils.SIZE_OF_INT + (2 * ByteUtils.SIZE_OF_LONG) + ByteUtils.SIZE_OF_INT + ByteUtils.SIZE_OF_INT
				+ (ByteUtils.SIZE_OF_INT * 4) + ByteUtils.SIZE_OF_INT + (2 * this.orthogonalDistance.length * ByteUtils.SIZE_OF_LONG) + (2 * ByteUtils.SIZE_OF_LONG);
		
		CalicoPacket packet = new CalicoPacket(packetSize);

		packet.putInt(NetworkCommand.CONNECTOR_LOAD);
		packet.putLong(uuid);
		packet.putLong(cuid);
		packet.putColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue()));
		packet.putFloat(this.thickness);
		
		packet.putInt(pointHead.x);
		packet.putInt(pointHead.y);
		packet.putInt(pointTail.x);
		packet.putInt(pointTail.y);
		
		packet.putInt(this.orthogonalDistance.length);
		for(int j=0;j<this.orthogonalDistance.length;j++)
		{
			packet.putDouble(this.orthogonalDistance[j]);
			packet.putDouble(this.travelDistance[j]);
		}
		
		packet.putLong(anchorHeadUUID);
		packet.putLong(anchorTailUUID);
		
		return new CalicoPacket[]{packet};

	}
	
	public CalicoPacket[] getUpdatePackets()
	{
		return getUpdatePackets(this.uuid, this.canvasUID);
	}
	
	public CalicoPacket[] getStrokePackets()
	{			
		Polygon mousePoints = getRawPolygon();
		int packetSize = ByteUtils.SIZE_OF_INT + (3 * ByteUtils.SIZE_OF_LONG) + ByteUtils.SIZE_OF_INT + ByteUtils.SIZE_OF_SHORT + (2 * mousePoints.npoints * ByteUtils.SIZE_OF_SHORT);
		
		CalicoPacket packet = new CalicoPacket(packetSize);
		//UUID CUID PUID <COLOR> <NUMCOORDS> x1 y1
		packet.putInt(NetworkCommand.STROKE_LOAD);
		packet.putLong(Calico.uuid());
		packet.putLong(canvasUID);
		packet.putLong(0l);
		packet.putColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue()));
		packet.putFloat(this.thickness);
		packet.putCharInt(mousePoints.npoints);
		for(int j=0;j<mousePoints.npoints;j++)
		{
			packet.putInt(mousePoints.xpoints[j]);
			packet.putInt(mousePoints.ypoints[j]);
		}
		packet.putDouble(0.0);
		packet.putDouble(1.0);
		packet.putDouble(1.0);
		
		return new CalicoPacket[]{packet};

	}
	
	public ObjectArrayList<Class<?>> getBubbleMenuButtons()
	{
		ObjectArrayList<Class<?>> bubbleMenuButtons = new ObjectArrayList<Class<?>>();
		bubbleMenuButtons.addAll(internal_getBubbleMenuButtons());
		//bubbleMenuButtons.addAll(CConnector.bubbleMenuButtons);
		return bubbleMenuButtons;
	}
	
	protected ObjectArrayList<Class<?>> internal_getBubbleMenuButtons()
	{
		ObjectArrayList<Class<?>> bubbleMenuButtons = new ObjectArrayList<Class<?>>(); 
		bubbleMenuButtons.add(calico.components.bubblemenu.connectors.ConnectorLinearizeButton.class);
		bubbleMenuButtons.add(calico.components.bubblemenu.connectors.ConnectorMakeStrokeButton.class);
		bubbleMenuButtons.add(calico.components.bubblemenu.connectors.ConnectorMoveHeadButton.class);
		bubbleMenuButtons.add(calico.components.bubblemenu.connectors.ConnectorMoveTailButton.class);
		//motta.lrd: the probability distribution button for the analysis
		if(this instanceof ControlFlow){
			bubbleMenuButtons.add(calico.plugins.analysis.components.buttons.ProbabilityDistributionBubbleButton.class);
		}
		return bubbleMenuButtons;
	}
	
	public int get_signature() {

		int sig = (int) (this.orthogonalDistance.length + pointHead.x + pointHead.y + anchorTailUUID);

//		System.out.println("Debug sig for group " + uuid + ": " + sig + ", 1) " + this.points.npoints + ", 2) " + isPermanent() + ", 3) " + this.points.xpoints[0] + ", 4) " + this.points.xpoints[0] + ", 5) " + this.points.ypoints[0] + ", 6) " + (int)(this.rotation*10) + ", 7) " + (int)(this.scaleX*10) + ", 8) " + (int)(this.scaleY*10));
		return sig;
	}
}
