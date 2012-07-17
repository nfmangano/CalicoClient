package calico.plugins.analysis.components.activitydiagram;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import calico.CalicoDraw;
import calico.CalicoOptions;
import calico.components.CGroup;
import calico.controllers.CCanvasController;
import calico.plugins.analysis.components.AnalysisComponent;
import calico.utils.Geometry;
import edu.umd.cs.piccolo.util.PPaintContext;

public class FinalNode extends CGroup implements AnalysisComponent{

	private static final long serialVersionUID = 1L;

	public FinalNode(long uuid, long cuid, long puid) {
		super(uuid, cuid, puid);
		// TODO Auto-generated constructor stub
		color = new Color(245,245,245);
	}

	protected void paint(final PPaintContext paintContext) {
		super.paint(paintContext);
		Point2D pivotPoint=Geometry.getMidPoint2D(points);
		
		//Get the shape of the scrap and make a copy 
		//The copy will be the black filling
		Shape border = (Shape)getPathReference().clone();
		((GeneralPath)border).closePath();
		Polygon oldPolygon = Geometry.getPolyFromPath(border.getPathIterator(null));
		Polygon newPolygon = new Polygon(oldPolygon.xpoints, oldPolygon.ypoints, oldPolygon.npoints);

		//Move the points to the origin
		newPolygon.translate((int)(-1 * pivotPoint.getX()), (int)(-1 * pivotPoint.getY()));
		//Scale the points centered in the origin
		for(int i=0; i<newPolygon.npoints; i++){
			newPolygon.xpoints[i]=(int)(newPolygon.xpoints[i]*0.6);
			newPolygon.ypoints[i]=(int)(newPolygon.ypoints[i]*0.6);
		}
		//Move the points back to where they were before
		newPolygon.translate((int)(1 * pivotPoint.getX()), (int)(1 * pivotPoint.getY()));
		
		//Paint the newPolygon (scaled with respect to the previous one)
		//and fill it black
		final Graphics2D g2 = paintContext.getGraphics();
		g2.setPaint(Color.BLACK);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
		g2.fill(newPolygon);

	}
	
	public String toString(){
		return "Final Node: " + this.uuid + "\n";
	}

}
