package calico.components.composable.connectors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import calico.Geometry;
import calico.components.CConnector;
import calico.components.composable.Composable;
import calico.components.composable.ComposableElement;
import calico.controllers.CConnectorController;
import calico.networking.netstuff.ByteUtils;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * Element to add custom arrowheads to the ends of connectors
 * Takes in a polygon that will rotate around (0,0) depending on the polar angle in radians 
 * of the points p0 and p1, where p0 is the end point from whichever end the arrowhead is for, 
 * and p1 is pointOffset number of points from that end.
 * 
 * See the default arrow and default circle below for examples
 * @author Wayne
 *
 */
public class ArrowheadElement extends ComposableElement {
	
	public static final int pointOffset = 10;
	
	/**
	 * Indicates head or tail. Constant from CConnector
	 */
	private int anchorType;
	private float strokeSize;
	private Color outlineColor;
	private Color fillColor;
	private Polygon polygon;
	
	public ArrowheadElement(long uuid, long cuuid, int anchorType, float strokeSize, Color outlineColor, Color fillColor, Polygon polygon)
	{
		super(uuid, cuuid);
		this.anchorType = anchorType;
		this.strokeSize = strokeSize;
		this.outlineColor = outlineColor;
		this.fillColor = fillColor;
		this.polygon = polygon;
	}
	
	public void applyElement() 
	{
		
	}
	
	public void removeElement()
	{
		
	}
	
	public Composable getComposable()
	{
		return CConnectorController.connectors.get(cuuid);
	}
	
	public boolean isDrawable()
	{
		return true;
	}
	
	public PNode getNode()
	{
		if (!CConnectorController.exists(cuuid))
			return null;
		
		CConnector connector = CConnectorController.connectors.get(cuuid);
		Polygon linePoints = connector.getRawPolygon();
		
		Point p0, p1;
		int pointForArrow = Math.min(pointOffset, linePoints.npoints);
		
		if (this.anchorType == CConnector.TYPE_HEAD)
		{
			p0 = new Point(linePoints.xpoints[linePoints.npoints-1], linePoints.ypoints[linePoints.npoints-1]);
			p1 = new Point(linePoints.xpoints[linePoints.npoints-pointForArrow], linePoints.ypoints[linePoints.npoints-pointForArrow]);
		}
		else if (this.anchorType == CConnector.TYPE_TAIL)
		{
			p0 = new Point(linePoints.xpoints[0], linePoints.ypoints[0]);
			p1 = new Point(linePoints.xpoints[pointForArrow-1], linePoints.ypoints[pointForArrow-1]);
		}
		else
		{
			return null;
		}


		PPath arrowHead = new PPath();
		
		AffineTransform rotateAboutPivot = AffineTransform.getRotateInstance(Geometry.computeAngleOfLineBetweenTwoPoints(p0.x, p0.y, p1.x, p1.y), 0, 0);
		
		for (int i = 0; i < polygon.npoints; i++)
		{
			Point ptSrc = new Point(polygon.xpoints[i], polygon.ypoints[i]);
			Point ptDst = new Point();
			rotateAboutPivot.transform(ptSrc, ptDst);
			ptDst.translate(p0.x, p0.y);
			
			if (i == 0)
			{
				arrowHead.moveTo(ptDst.x, ptDst.y);
			}
			else
			{
				arrowHead.lineTo(ptDst.x, ptDst.y);
			}
		}
		
		arrowHead.setStroke(new BasicStroke(strokeSize));
		arrowHead.setStrokePaint(this.outlineColor);
		arrowHead.setPaint(this.fillColor);
		
		
		return arrowHead;
	}
	
	public CalicoPacket getPacket(long uuid, long cuuid)
	{
		int packetSize = ByteUtils.SIZE_OF_INT 				//Command
				+ ByteUtils.SIZE_OF_INT 				//Element Type
				+ (2 * ByteUtils.SIZE_OF_LONG) 			//UUID & CUUID
				+ ByteUtils.SIZE_OF_INT					//Anchor Type
				+ (3 * ByteUtils.SIZE_OF_INT)			//Stroke size, outline color, fill color
				+ ByteUtils.SIZE_OF_INT					//NPoints
				+ (polygon.npoints * 2 * polygon.npoints);		//polyon points
	
		CalicoPacket packet = new CalicoPacket(packetSize);
		
		packet.putInt(NetworkCommand.ELEMENT_ADD);
		packet.putInt(ComposableElement.TYPE_ARROWHEAD);
		packet.putLong(uuid);
		packet.putLong(cuuid);
		packet.putInt(anchorType);
		packet.putFloat(strokeSize);
		packet.putColor(outlineColor);
		packet.putColor(fillColor);
		
		packet.putInt(polygon.npoints);
		for (int i = 0; i < polygon.npoints; i++)
		{
			packet.putInt(polygon.xpoints[i]);
			packet.putInt(polygon.ypoints[i]);
		}
		
		
		return packet;
	}
	
	public CalicoPacket getPacket()
	{
		return getPacket(this.uuid, this.cuuid);
	}
	
	public static Polygon getDefaultArrow()
	{
		Polygon poly = new Polygon();
		
		poly.addPoint(0, 0);
		poly.addPoint(25, 8);
		poly.addPoint(20, 0);
		poly.addPoint(25 ,-8);
		poly.addPoint(0, 0);
		
		return poly;
	}
	
	public static Polygon getDefaultCircle()
	{
		Polygon poly = new Polygon();
		int[] points = Geometry.createCircle(0, 0, 3);
		
		for (int i = 0; i < points.length; i = i + 2)
		{
			poly.addPoint(points[i], points[i+1]);
		}
		
		return poly;
	}

	@Override
	public ComposableElement getInstanceFromPacket(CalicoPacket packet) {
		packet.rewind();
		if (packet.getInt() != NetworkCommand.ELEMENT_ADD)
			return null;
		
		int elementType = packet.getInt();
		long uuid = packet.getLong();
		long cuuid = packet.getLong();
		
		int type = packet.getInt();
		float strokeSize = packet.getFloat();
		Color outlineColor = packet.getColor();
		Color fillColor = packet.getColor();
		
		Polygon polygon = new Polygon();
		int npoints = packet.getInt();
		for (int i = 0; i < npoints; i++)
		{
			polygon.addPoint(packet.getInt(), packet.getInt());
		}
		
		return new ArrowheadElement(uuid, cuuid, type, strokeSize, outlineColor, fillColor, polygon);
	}
}
