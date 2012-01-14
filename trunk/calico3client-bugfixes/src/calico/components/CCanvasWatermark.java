package calico.components;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;

public class CCanvasWatermark extends PLayer
{
	public enum Span
	{
		X,
		Y;
	}

	private static final int DEFAULT_TILE_FREQUENCY = 200;

	private final Image image;
	private final List<List<PImage>> tiles = new ArrayList<List<PImage>>(); // grid of tiles: <x<y>>

	private int tileFrequency = DEFAULT_TILE_FREQUENCY;

	public CCanvasWatermark(Image image)
	{
		this.image = image;
	}

	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();

		// make sure this flag is reliable for this usage
		if (getBoundsChanged())
		{
			PBounds bounds = getBoundsReference();

			int xTileCount = (int) (bounds.width / tileFrequency);
			int yTileCount = (int) (bounds.height / tileFrequency);

			if (xTileCount > getTileCount(Span.X))
			{
				for (int i = getTileCount(Span.X); i < xTileCount; i++)
				{
					List<PImage> row = new ArrayList<PImage>();
					for (int j = 0; j < yTileCount; j++)
					{
						row.add(new PImage(image));
					}
					tiles.add(row);
				}
			}

			if (yTileCount > getTileCount(Span.Y))
			{
				for (List<PImage> row : tiles)
				{
					for (int i = getTileCount(Span.Y); i < yTileCount; i++)
					{
						row.add(new PImage(image));
					}
				}
			}

			removeAllChildren();
			int y = tileFrequency / 2;
			for (int xIndex = 0; xIndex < xTileCount; xIndex++)
			{
				int x = tileFrequency / 2;
				List<PImage> row = tiles.get(xIndex);
				
				addChildren(row);
				
				for (PImage tile : row)
				{
					tile.setX(x);
					tile.setY(y);
					
					x += tileFrequency;
				}
				
				y += tileFrequency;
			}
		}
	}

	public int getTileFrequency()
	{
		return tileFrequency;
	}

	public void setTileFrequency(int tileFrequency)
	{
		this.tileFrequency = tileFrequency;
	}

	public int getTileCount(Span span)
	{
		switch (span)
		{
			case X:
				return tiles.size();
			case Y:
				if (tiles.isEmpty())
				{
					return 0;
				}
				return tiles.get(0).size();
			default:
				throw new IllegalArgumentException("Span " + span + " is unrecognized");
		}
	}
}
