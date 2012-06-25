package calico.plugins.iip.components.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PClip;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CIntentionTopology
{
	private static final Color RING_COLOR = new Color(0xEEEEEE);
	private static final Color BOUNDING_BOX_COLOR = new Color(0x0, 0x0, 0x0, 0x0);

	public class Cluster extends PComposite
	{
		private final long rootCanvasId;
		private final List<PPath> rings = new ArrayList<PPath>();

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
			PClip box = new PClip();
			box.setPathToRectangle(xBox, yBox, wBox, hBox);
			box.setStrokePaint(BOUNDING_BOX_COLOR);
			addChild(box);

			while (tokens.hasMoreTokens())
			{
				int radius = Integer.parseInt(tokens.nextToken());
				PPath ring = PPath.createEllipse((float) (getX() - radius), (float) (getY() - radius), radius * 2, radius * 2);
				ring.setStrokePaint(RING_COLOR);
				rings.add(ring);
			}

			for (int i = (rings.size() - 1); i >= 0; i--)
			{
				box.addChild(rings.get(i));
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
}
