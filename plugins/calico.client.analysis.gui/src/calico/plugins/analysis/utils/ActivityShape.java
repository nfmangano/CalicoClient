package calico.plugins.analysis.utils;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import sl.shapes.RegularPolygon;
import sl.shapes.RoundPolygon;
import calico.utils.Geometry;

public enum ActivityShape {

	DECISION, ACTIVITY, INITIALNODE, FINALNODE, FORK, JOIN;
	
	//Return a shape centered in x,y
	public Polygon getShape(int x, int y){
		switch(this){
			case DECISION: {
				GeneralPath myPolygon = new GeneralPath(new RegularPolygon(x, y, 20, 4, 0));
				Polygon p = Geometry.getPolyFromPath(myPolygon.getPathIterator(null));	
				return p;
			}
			case ACTIVITY: {
				GeneralPath myPolygon = new GeneralPath(new Rectangle(x,y, 120, 60));
				Polygon p = Geometry.getPolyFromPath(myPolygon.getPathIterator(null));	
				return p;
			}
			case FORK: {
				GeneralPath myPolygon = new GeneralPath(new Rectangle(x,y, 120, 20));
				Polygon p = Geometry.getPolyFromPath(myPolygon.getPathIterator(null));	
				return p;
			}
			case INITIALNODE: {
				GeneralPath myPolygon = new GeneralPath(new RegularPolygon(x, y, 20, 15, 0));
				Polygon p = Geometry.getPolyFromPath(myPolygon.getPathIterator(null));	
				return p;
			}
			case FINALNODE: {
				GeneralPath myPolygon = new GeneralPath(new RegularPolygon(x, y, 20, 15, 0));
				Polygon p = Geometry.getPolyFromPath(myPolygon.getPathIterator(null));	
				return p;
			}
		}
		
		return null;
	}
	
}
