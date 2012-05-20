package calico.plugins.iip.components.graph;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CIntentionTopology
{
	private static final Color RING_COLOR = new Color(0xEEEEEE);
	
	public class Cluster extends PComposite
	{
		private final List<PPath> rings = new ArrayList<PPath>();

		Cluster(String serialized)
		{
			StringTokenizer tokens = new StringTokenizer(serialized, "[],:");
			int x = Integer.parseInt(tokens.nextToken());
			int y = Integer.parseInt(tokens.nextToken());

			setX(x);
			setY(y);

			while (tokens.hasMoreTokens())
			{
				int radius = Integer.parseInt(tokens.nextToken());
				PPath ring = PPath.createEllipse((float) (getX() - radius), (float) (getY() - radius), radius * 2, radius * 2);
				ring.setStrokePaint(RING_COLOR);
				rings.add(ring);
			}

			for (int i = (rings.size() - 1); i >= 0; i--)
			{
				addChild(rings.get(i));
			}
		}
	}

	private final List<Cluster> clusters = new ArrayList<Cluster>();

	public CIntentionTopology(String serialized)
	{
		StringTokenizer tokens = new StringTokenizer(serialized, "C");
		while (tokens.hasMoreTokens())
		{
			clusters.add(new Cluster(tokens.nextToken()));
		}
	}

	public void clear()
	{
		clusters.clear();
	}

	public List<Cluster> getClusters()
	{
		return clusters;
	}
}
