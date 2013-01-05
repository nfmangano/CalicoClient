package calico.plugins.iip.components.graph;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import calico.CalicoDraw;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PClip;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * Represents the intention topology of the Intention View. It draws each <code>CIntentionRing</code> from the server's
 * intention layout, along with a bounding box around each cluster.
 * 
 * @author Byron Hawkins
 */
public class CIntentionTopology
{
	private static final Color RING_COLOR = new Color(0x8b, 0x89, 0x89);
	private static final Color BOUNDING_BOX_COLOR = Color.black;//new Color(0x8b, 0x89, 0x89);
	

	/**
	 * Represents one cluster in the Piccolo component hierarchy of the IntentionView. It is constructed from the
	 * topology that was serialized on the server, taking one cluster out of the serialized topology data and inflating
	 * it into a set of <code>PPath</code>s for the rings and a last <code>PPath</code> for the bounding box.
	 * 
	 * @author Byron Hawkins
	 */
	public class Cluster extends PComposite
	{
		private final long rootCanvasId;
		private final List<PPath> rings = new ArrayList<PPath>();
		private final PClip box;
		private final PClip outerBox;

//		buffer.append(rootCanvasId);
//		buffer.append("[");
//		buffer.append(center.x);
//		buffer.append(",");
//		buffer.append(center.y);
//		buffer.append(",");
//		buffer.append(boundingBox.x);
//		buffer.append(",");
//		buffer.append(boundingBox.y);
//		buffer.append(",");
//		buffer.append(boundingBox.width);
//		buffer.append(",");
//		buffer.append(boundingBox.height);
//		buffer.append(",");			
//		buffer.append(outerBox.x);
//		buffer.append(",");
//		buffer.append(outerBox.y);
//		buffer.append(",");
//		buffer.append(outerBox.width);
//		buffer.append(",");
//		buffer.append(outerBox.height);			
//		buffer.append(":");
		
		Cluster(String serialized)
		{
			StringTokenizer tokens = new StringTokenizer(serialized, "[],:");
			rootCanvasId = Long.parseLong(tokens.nextToken());
			int x = Integer.parseInt(tokens.nextToken());
			int y = Integer.parseInt(tokens.nextToken());

			setX(x);
			setY(y);

			int xBox = Integer.parseInt(tokens.nextToken());
			int yBox = Integer.parseInt(tokens.nextToken());
			int wBox = Integer.parseInt(tokens.nextToken());
			int hBox = Integer.parseInt(tokens.nextToken());
			box = new PClip();
			box.setPathToRectangle(xBox, yBox, wBox, hBox);
			box.setStrokePaint(BOUNDING_BOX_COLOR);
			
			
			int xOuterBox = Integer.parseInt(tokens.nextToken());
			int yOuterBox = Integer.parseInt(tokens.nextToken());
			int wOuterBox = Integer.parseInt(tokens.nextToken());
			int hOuterBox = Integer.parseInt(tokens.nextToken());		
			
			outerBox = new PClip();
			outerBox.setPathToRectangle(xOuterBox, yOuterBox, wOuterBox, hOuterBox);
			outerBox.setStrokePaint(BOUNDING_BOX_COLOR);		
			outerBox.setPaint(Color.white);
			outerBox.setBounds(xOuterBox, yOuterBox, wOuterBox, hOuterBox);
			
//			addChild(box);
//			CalicoDraw.addChildToNode(this, box);
			CalicoDraw.addChildToNode(this, outerBox);
			CalicoDraw.setNodeBounds(this, xOuterBox, yOuterBox, wOuterBox, hOuterBox);

			while (tokens.hasMoreTokens())
			{
				int radius = Integer.parseInt(tokens.nextToken());
				PPath ring = PPath.createEllipse((float) (outerBox.getBounds().getCenterX() - radius), 
						(float) (outerBox.getBounds().getCenterY() - radius), radius * 2, radius * 2);
				ring.setStrokePaint(RING_COLOR);
				rings.add(ring);
			}

			for (int i = (rings.size() - 1); i >= 0; i--)
			{
				CalicoDraw.addChildToNode(outerBox, rings.get(i));
//				box.addChild(rings.get(i));
			}
		}

		public PBounds getMaxRingBounds()
		{
			if (rings.isEmpty())
			{
				return null;
			}

			double span = rings.get(rings.size() - 1).getWidth();
			return new PBounds(getX() - (span / 2.0), getY() - (span / 2.0), span, span);
		}
		
		public PBounds getVisualBoxBounds()
		{
			
			return outerBox.getBounds();
		}
		
		public long getRootCanvasId()
		{
			return rootCanvasId;
		}
		
		public boolean hasChildren()
		{
			return rings.size() > 0;
		}
	}

	private final Map<Long, Cluster> clusters = new HashMap<Long, Cluster>();

	public CIntentionTopology(String serialized)
	{
		StringTokenizer tokens = new StringTokenizer(serialized, "C");
		while (tokens.hasMoreTokens())
		{
			Cluster cluster = new Cluster(tokens.nextToken());
			clusters.put(cluster.rootCanvasId, cluster);
		}
	}

	public void clear()
	{
		clusters.clear();
	}

	public Collection<Cluster> getClusters()
	{
		return clusters.values();
	}

	public Cluster getCluster(long rootCanvasId)
	{
		return clusters.get(rootCanvasId);
	}
	
	public Cluster getClusterAt(Point2D p)
	{
		for (Cluster c : clusters.values())
		{
			if (c.getBounds().contains(p))
				return c;
		}
		return null;
	}
}
