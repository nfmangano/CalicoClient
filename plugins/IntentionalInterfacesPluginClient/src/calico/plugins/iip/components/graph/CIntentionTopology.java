package calico.plugins.iip.components.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import calico.CalicoDraw;
import calico.events.CalicoEventListener;
import calico.networking.netstuff.CalicoPacket;
import calico.plugins.iip.IntentionalInterfacesNetworkCommands;
import calico.plugins.iip.controllers.CIntentionCellController;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
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
		private final PText wallTitle;
		private final PText clusterTitle;

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
			
			wallTitle = new PText("Wall > ");
			wallTitle.setOffset(outerBox.getX() + 20, outerBox.getY() + 15);
			Font font = new Font ("Helvetica", Font.PLAIN , 30);
			wallTitle.setFont(font);
			wallTitle.recomputeLayout();
			
			clusterTitle = new PText("Unnamed cluster");
			clusterTitle.setOffset(outerBox.getX() + 20 + wallTitle.getGlobalBounds().getWidth(), outerBox.getY() + 15);
			clusterTitle.setWidth(outerBox.getGlobalBounds().getWidth() - wallTitle.getGlobalBounds().getWidth());
			clusterTitle.setConstrainWidthToTextWidth(false);
//			Font font = new Font ("Helvetica", Font.PLAIN , 30);
			clusterTitle.setFont(font);
			clusterTitle.recomputeLayout();
			
			
			
//			addChild(box);
//			CalicoDraw.addChildToNode(this, box);
			CalicoDraw.addChildToNode(this, outerBox);
			CalicoDraw.setNodeBounds(this, xOuterBox, yOuterBox, wOuterBox, hOuterBox);
			CalicoDraw.addChildToNode(this, clusterTitle);
			CalicoDraw.addChildToNode(this, wallTitle);

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
		
		public boolean clusterTitleTextContainsPoint(Point p)
		{
			return clusterTitle.getGlobalFullBounds().contains(p);
		}
		
		public boolean clusterWallTextContainsPoint(Point p)
		{
			return wallTitle.getGlobalFullBounds().contains(p);
		}
		
		public void updateTitleText()
		{

			SwingUtilities.invokeLater(
					new Runnable() { public void run() { 
						Font font;
						if (IntentionGraph.getInstance().getFocus() == IntentionGraph.Focus.CLUSTER)
						{
							font = new Font ("Helvetica", Font.PLAIN , (int)(40)); 
						}
						else
						{
							font = new Font ("Helvetica", Font.PLAIN , (int)(70));
						}
						wallTitle.setFont(font);
						wallTitle.recomputeLayout();
						
						clusterTitle.setFont(font);
						clusterTitle.setText(CIntentionCellController.getInstance().getCellByCanvasId(rootCanvasId).getTitle());
						clusterTitle.setOffset(outerBox.getX() + 20 + wallTitle.getBounds().getWidth(), outerBox.getY() + 15);
						clusterTitle.setWidth(outerBox.getBounds().getWidth() - wallTitle.getBounds().getWidth());
						clusterTitle.recomputeLayout();
					}});

			
			
//			clusterTitle.repaint();
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
		
		/**
		 * Returns true if the ring contains {@link Point} p. Returns false if the point is not contained or the ring level doesn't exist.
		 * @param p The point passed (is not changed) in global coordinates.
		 * @param ringLevel The ring level. The first ring level is zero.
		 * @return
		 */
		public boolean ringContainsPoint(Point p, int ringLevel)
		{
			if (rings.size() <= ringLevel)
				return false;
			
			Point2D local = outerBox.globalToLocal(new Point(p));
			
			return ((ArrayList<PPath>)rings).get(ringLevel).getPathReference().contains(local);
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
