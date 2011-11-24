package calico;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

import calico.controllers.CGroupController;

public class CalicoUtils
{
	
	public static Polygon pathIterator2Polygon(PathIterator path)
	{
		Polygon poly = new Polygon();
		
		double[] coordinates = new double[6];
		while (path.isDone() == false)
		{
			//int type = path.currentSegment(coordinates);
			
			poly.addPoint((int)coordinates[0], (int)coordinates[1]);
			
			path.next();
		}
		
		return poly;
	}
	
	public static Polygon clonePolygon(Polygon poly)
	{
		Polygon newpoly = new Polygon();
		for(int i=0;i<poly.npoints;i++)
		{
			newpoly.addPoint(poly.xpoints[i], poly.ypoints[i]);
		}
		return newpoly;
	}
	

	
}