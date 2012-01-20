package calico.iconsets;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import calico.CalicoDataStore;
import calico.modules.ErrorMessage;

public class CalicoIconManager
{
	private static Properties iconTheme = new Properties();

	private static String iconNotFound = "";
	private static String iconThemeName = "";

	public static int defaultIconSize = 16;

	public static Logger logger = Logger.getLogger(CalicoIconManager.class.getName());

	private static final Map<String, BufferedImage> ICON_CACHE = new HashMap<String, BufferedImage>();

	public static void setIconTheme(String name)
	{
		iconThemeName = name;
		try
		{
			iconTheme.load(CalicoDataStore.calicoObj.getClass().getResourceAsStream("iconsets/" + iconThemeName + "/icontheme.properties"));
			// iconTheme.list(System.out);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ErrorMessage.fatal("Unable to load icon theme " + iconThemeName + ".");
		}

		// Load Up the default parts
		iconNotFound = iconTheme.getProperty("notfound", "cross.png");

		// Show some blip about it
		logger.info("Loading Icon Theme: " + iconTheme.getProperty("iconset.name") + " by " + iconTheme.getProperty("author.name") + " ("
				+ iconTheme.getProperty("author.email") + ")");

	}

	public static String getIcon(String icon)
	{
		String iconPath = iconTheme.getProperty(icon, iconNotFound);
		if (iconPath.startsWith("@"))
		{
			return getIcon(iconPath.replace("@", ""));
		}

		URL url = CalicoDataStore.calicoObj.getClass().getResource("iconsets/" + iconThemeName + "/" + iconPath);
		return url.toString();
	}

	public static BufferedImage getIconImage(String iconPath)
	{
		BufferedImage image = ICON_CACHE.get(iconPath);
		if (image == null)
		{
			try
			{
				image = ImageIO.read(new URL(CalicoIconManager.getIcon(iconPath)));
				ICON_CACHE.put(iconPath, image);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		return image;
	}

	public static BufferedImage getImagePart(Image img, int x, int y, int w, int h)
	{
		BufferedImage bgBuf = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bgBuf.getGraphics();
		// g.setBackground(new Color(83,83,83));
		g.drawImage(img, null, null);
		g.dispose();

		return bgBuf.getSubimage(x, y, w, h);
	}

}
