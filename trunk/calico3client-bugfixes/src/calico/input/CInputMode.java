package calico.input;

import java.awt.image.BufferedImage;

import calico.iconsets.CalicoIconManager;

public enum CInputMode
{
	// public static final int MODE_EXPERT = 1 << 0;
	// public static final int MODE_SCRAP = 1 << 1;
	// public static final int MODE_STROKE = 1 << 2;
	// public static final int MODE_ARROW = 1 << 3;
	// public static final int MODE_DELETE = 1 << 4;
	// public static final int MODE_POINTER = 1 << 5;

	EXPERT("mode.expert"),
	SCRAP("mode.scrap"),
	STROKE("mode.stroke"),
	ARROW("mode.arrow"),
	DELETE("mode.delete"),
	POINTER("mode.pointer");

	private final int id;
	private final String imageId;
	private BufferedImage image = null;

	private CInputMode(String imageId)
	{
		this.id = 1 << ordinal();
		this.imageId = imageId;
	}

	public BufferedImage getImage()
	{
		return image;
	}

	public int getId()
	{
		return id;
	}

	public String getImageId()
	{
		return imageId;
	}

	public static CInputMode forId(int id)
	{
		for (CInputMode mode : CInputMode.values())
		{
			if (mode.id == id)
			{
				return mode;
			}
		}
		return null;
	}

	public static void setup()
	{
		for (CInputMode mode : CInputMode.values())
		{
			mode.image = CalicoIconManager.getIconImage(mode.imageId);
		}
	}
}
