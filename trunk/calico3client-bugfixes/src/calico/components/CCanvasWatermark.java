package calico.components;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import calico.CalicoDraw;
import calico.input.CInputMode;
import calico.utils.ImageUtils;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;

public class CCanvasWatermark extends PLayer
{
	public static class InputModeWatermarks
	{
		private static final Map<CInputMode, CCanvasWatermark> watermarks = new EnumMap<CInputMode, CCanvasWatermark>(CInputMode.class);
		
		public static void setup()
		{
			for (CInputMode mode : CInputMode.values())
			{
				watermarks.put(mode, new CCanvasWatermark(mode.getImage()));
			}
			watermarks.remove(CInputMode.EXPERT); // no watermark for pen mode
			watermarks.remove(CInputMode.POINTER); // no watermark for highlighter mode
		}
		
		public static CCanvasWatermark get(CInputMode mode)
		{
			return watermarks.get(mode);
		}
	}
	
	private enum Span
	{
		X,
		Y;
	}

	private static final int DEFAULT_TILE_FREQUENCY = 200;

	private final Image image;
	private final List<List<PImage>> tiles = new ArrayList<List<PImage>>(); // grid of tiles: <y<x>>

	private int tileFrequency = DEFAULT_TILE_FREQUENCY;

	public CCanvasWatermark(BufferedImage image)
	{
		this.image = ImageUtils.adjustIntensity(image, -0.6f);
	}

	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();

		// make sure this flag is reliable for this usage
		if (getBoundsChanged())
		{
			PBounds bounds = getBoundsReference();

			int xTileCount = ((int) (bounds.width / tileFrequency)) + 1;
			int yTileCount = ((int) (bounds.height / tileFrequency)) + 1;

			int xCurrentTileCount = getTileCount(Span.X);
			if (xTileCount > xCurrentTileCount)
			{
				for (List<PImage> row : tiles)
				{
					for (int i = xCurrentTileCount; i < xTileCount; i++)
					{
						row.add(new PImage(image));
					}
				}
			}

			if (yTileCount > getTileCount(Span.Y))
			{
				for (int i = getTileCount(Span.Y); i < yTileCount; i++)
				{
					List<PImage> row = new ArrayList<PImage>();
					for (int j = 0; j < xTileCount; j++)
					{
						row.add(new PImage(image));
					}
					tiles.add(row);
				}
			}

			//removeAllChildren();
			CalicoDraw.removeAllChildrenFromNode(this);
			
			int y = tileFrequency / 2;
			for (int yIndex = 0; yIndex < yTileCount; yIndex++)
			{
				int x = tileFrequency / 2;
				List<PImage> row = tiles.get(yIndex);
				
				//addChildren(row);
				CalicoDraw.addChildrenToNode(this, row);
				
				for (PImage tile : row)
				{
					//tile.setX(x);
					//tile.setY(y);
					CalicoDraw.setNodeX(tile, x);
					CalicoDraw.setNodeY(tile, y);
					
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
				if (tiles.isEmpty())
				{
					return 0;
				}
				return tiles.get(0).size();
			case Y:
				return tiles.size();
			default:
				throw new IllegalArgumentException("Span " + span + " is unrecognized");
		}
	}
}
