package calico.plugins.iip.components;

import java.awt.Color;

/**
 * Represents a canvas tag in this plugin's internal model. Rendering associated with this tag is maintained by other
 * classes such as the <code>CanvasTagPanel</code>.
 * 
 * @author Byron Hawkins
 */
public class CIntentionType
{
	/**
	 * Static set of tag colors, identified by array index, which is known both to the client and server.
	 */
	public static final Color[] AVAILABLE_COLORS = new Color[] { new Color(0xFFFFFF), new Color(0xC4FF5E), new Color(0xFFF024), new Color(0x29FFE2), new Color(0x52DCFF),
			new Color(0xFFBDC1), new Color(0xFFE1C9), new Color(0xC2E4FF), new Color(0xEED9FF) };

	/**
	 * Identifies the tag.
	 */
	private final long uuid;
	/**
	 * Display name of the tag.
	 */
	private String name;
	/**
	 * Index of the tag's color. This index is the color reference value on both the client and server, and is only
	 * correlated with a visual color in this class.
	 */
	private int colorIndex;
	/**
	 * Description of tag.
	 */
	private String description;

	public CIntentionType(long uuid, String name, int colorIndex, String description)
	{
		this.uuid = uuid;
		this.name = name;
		this.colorIndex = colorIndex;
		this.description = description;
	}

	public long getId()
	{
		return uuid;
	}

	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * A tag'S color is known only by the color index in all other classes on the client and server.
	 */
	public int getColorIndex()
	{
		return colorIndex;
	}

	public Color getColor()
	{
		return AVAILABLE_COLORS[colorIndex];
	}

	/**
	 * A color is assigned by setting the index using this method.
	 */
	public void setColorIndex(int colorIndex)
	{
		this.colorIndex = colorIndex;
	}
}
