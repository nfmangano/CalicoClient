package calico.plugins.iip.components;

import java.awt.Color;

public class CIntentionType
{
	public static final Color[] AVAILABLE_COLORS = new Color[] { new Color(0xC4FF5E), new Color(0xFFF024), new Color(0x29FFE2), new Color(0x52DCFF),
			new Color(0xF896FF), new Color(0xFFBDC1), new Color(0xFFE1C9), new Color(0xC2E4FF), new Color(0xEED9FF) };

	private final long uuid;
	private String name;
	private int colorIndex;

	public CIntentionType(long uuid, String name, int colorIndex)
	{
		this.uuid = uuid;
		this.name = name;
		this.colorIndex = colorIndex;
	}

	public long getId()
	{
		return uuid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public int getColorIndex()
	{
		return colorIndex;
	}

	public Color getColor()
	{
		return AVAILABLE_COLORS[colorIndex];
	}

	public void setColorIndex(int colorIndex)
	{
		this.colorIndex = colorIndex;
	}
}
