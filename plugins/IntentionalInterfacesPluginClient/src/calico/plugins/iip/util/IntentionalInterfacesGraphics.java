package calico.plugins.iip.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import calico.components.CCanvas;
import calico.controllers.CCanvasController;

public class IntentionalInterfacesGraphics
{
	public static Image superimposeCellAddress(Image baseImage, long canvas_uuid)
	{
		CCanvas canvas = CCanvasController.canvasdb.get(canvas_uuid);
		String coordinates = canvas.getGridCoordTxt();
		Rectangle baseBounds = new Rectangle(baseImage.getWidth(null), baseImage.getHeight(null));
		BufferedImage compound = new BufferedImage(baseBounds.width, baseBounds.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) compound.getGraphics();

		Color c = g.getColor();
		Font f = g.getFont();

		g.drawImage(baseImage, 0, 0, null);
		g.setFont(new Font("Verdana", Font.BOLD, 32));
		g.setColor(COORDINATES_COLOR);

		Rectangle2D coordinatesBoundsMess = g.getFontMetrics().getStringBounds(coordinates, g);
		Rectangle coordinatesBounds = new Rectangle((int) coordinatesBoundsMess.getWidth(), (int) coordinatesBoundsMess.getHeight());
		int x = (baseBounds.width - coordinatesBounds.width) / 2;
		int y = (baseBounds.height - ((baseBounds.height - coordinatesBounds.height) / 2)) - g.getFontMetrics().getDescent();
		g.drawString(coordinates, x, y);

		g.setColor(c);
		g.setFont(f);

		return compound;
	}

	private static final Color COORDINATES_COLOR = new Color(0x77777766);
}
