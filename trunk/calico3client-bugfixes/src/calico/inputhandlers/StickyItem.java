package calico.inputhandlers;

import java.awt.Point;

public interface StickyItem {
	
	public boolean containsPoint(Point p);
	
	public long getUUID();

}
