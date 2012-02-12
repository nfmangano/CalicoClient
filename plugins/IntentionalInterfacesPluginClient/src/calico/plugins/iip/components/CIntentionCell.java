package calico.plugins.iip.components;

import java.awt.Point;

import calico.plugins.iip.components.graph.IntentionGraph;
import calico.plugins.iip.iconsets.CalicoIconManager;
import calico.plugins.iip.util.IntentionalInterfacesGraphics;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;

public class CIntentionCell
{
	long uuid;
	long canvas_uuid;
	Point location;

	private final PComposite shell = new PComposite();
	private final PImage obscuredContent;

	public CIntentionCell(long uuid, long canvas_uuid, int x, int y)
	{
		this.uuid = uuid;
		this.canvas_uuid = canvas_uuid;
		this.location = new Point(x, y);

		obscuredContent = new PImage(IntentionalInterfacesGraphics.superimposeCellAddress(
				CalicoIconManager.getIconImage("intention-graph.obscured-intention-cell"), canvas_uuid));
		shell.addChild(obscuredContent);
		shell.setBounds(0.0, 0.0, obscuredContent.getWidth(), obscuredContent.getHeight());
	}

	public long getId()
	{
		return uuid;
	}

	public long getCanvasId()
	{
		return canvas_uuid;
	}

	public boolean contains(Point point)
	{
		PBounds bounds = shell.getGlobalBounds();
		return ((point.x > bounds.x) && (point.y > bounds.y) && ((point.x - bounds.x) < bounds.width) && (point.y - bounds.y) < bounds.height);
	}

	public void setLocation(int x, int y)
	{
		location.x = x;
		location.y = y;

		shell.setBounds(x, y, shell.getBounds().getWidth(), shell.getBounds().getHeight());
	}

	public void display(boolean b)
	{
		if (b)
		{
			IntentionGraph.getInstance().getLayer().addChild(shell);
		}
		else
		{
			IntentionGraph.getInstance().getLayer().removeChild(shell);
		}
	}
}
