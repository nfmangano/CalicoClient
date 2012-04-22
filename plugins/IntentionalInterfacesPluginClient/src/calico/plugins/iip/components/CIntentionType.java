package calico.plugins.iip.components;

import java.awt.Color;

public class CIntentionType
{
	public static final Color[] AVAILABLE_COLORS = new Color[] { new Color(0xC4FF5E), new Color(0xFFF024), new Color(0x29FFE2), new Color(0x52DCFF),
			new Color(0xF896FF), new Color(0xFFBDC1), new Color(0xFFFDBA), new Color(0xC2E4FF), new Color(0xEED9FF) };

	private String name;
	private Color color;
	
	public CIntentionType(String name, Color color)
	{
		this.name = name;
		this.color = color;
	}

	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public void setColor(Color color)
	{
		this.color = color;
	}
}
