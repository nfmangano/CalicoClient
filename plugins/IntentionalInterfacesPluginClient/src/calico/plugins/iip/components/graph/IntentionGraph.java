package calico.plugins.iip.components.graph;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JComponent;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.menus.CanvasMenuBar;
import calico.input.CalicoMouseListener;
import calico.inputhandlers.CalicoInputManager;
import calico.plugins.iip.components.menus.IntentionGraphMenuBar;
import calico.plugins.iip.inputhandlers.IntentionGraphInputHandler;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;

public class IntentionGraph
{
	public static IntentionGraph getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new IntentionGraph();
		}
		INSTANCE.repaint(); 
		return INSTANCE;
	}

	private static IntentionGraph INSTANCE;

	private final ContainedCanvas canvas = new ContainedCanvas();
	private IntentionGraphMenuBar menuBar;

	private final long uuid;

	private IntentionGraph()
	{
		uuid = Calico.uuid();

		// IntentionGraph.exitButtonBounds = new Rectangle(CalicoDataStore.ScreenWidth-32,5,24,24);

		canvas.setPreferredSize(new Dimension(CalicoDataStore.ScreenWidth, CalicoDataStore.ScreenHeight));
		setBounds(0, 0, CalicoDataStore.ScreenWidth, CalicoDataStore.ScreenHeight);

		CalicoInputManager.addCustomInputHandler(uuid, new IntentionGraphInputHandler());

		canvas.removeInputSources();

		canvas.addMouseListener(new CalicoMouseListener());
		canvas.addMouseMotionListener(new CalicoMouseListener());

		canvas.removeInputEventListener(canvas.getPanEventHandler());
		canvas.removeInputEventListener(canvas.getZoomEventHandler());

		repaint();

		drawMenuBar();
	}

	public PCamera getCamera()
	{
		return canvas.getCamera();
	}

	public PLayer getLayer()
	{
		return canvas.getLayer();
	}

	public JComponent getComponent()
	{
		return canvas;
	}

	public void repaint()
	{
		canvas.repaint();
	}
	
	public Rectangle getBounds()
	{
		return canvas.getBounds();
	}

	public void setBounds(int x, int y, int w, int h)
	{
		canvas.setBounds(x, y, w, h);
	}

	private void drawMenuBar()
	{
		if (menuBar != null)
		{
			getCamera().removeChild(menuBar);
		}

		menuBar = new IntentionGraphMenuBar(CanvasMenuBar.POSITION_BOTTOM);
		getCamera().addChild(menuBar);
	}

	private class ContainedCanvas extends PCanvas
	{
		@Override
		protected void removeInputSources()
		{
			super.removeInputSources();
		}
	}
}
