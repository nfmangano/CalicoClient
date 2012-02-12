package calico.plugins.iip.components.graph;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import calico.Calico;
import calico.CalicoDataStore;
import calico.components.menus.CanvasMenuBar;
import calico.input.CalicoMouseListener;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
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
			new IntentionGraph();
		}
		INSTANCE.repaint();
		return INSTANCE;
	}

	private static IntentionGraph INSTANCE;

	private final ContainedCanvas canvas = new ContainedCanvas();
	private final ContainedCanvas contentCanvas = new ContainedCanvas();
	private IntentionGraphMenuBar menuBar;

	private final long uuid;

	private IntentionGraph()
	{
		INSTANCE = this;

		uuid = Calico.uuid();

		// IntentionGraph.exitButtonBounds = new Rectangle(CalicoDataStore.ScreenWidth-32,5,24,24);

		canvas.setPreferredSize(new Dimension(CalicoDataStore.ScreenWidth, CalicoDataStore.ScreenHeight));
		setBounds(0, 0, CalicoDataStore.ScreenWidth, CalicoDataStore.ScreenHeight);

		CalicoInputManager.addCustomInputHandler(uuid, new IntentionGraphInputHandler());

		canvas.addMouseListener(new CalicoMouseListener());
		canvas.addMouseMotionListener(new CalicoMouseListener());

		canvas.removeInputEventListener(canvas.getPanEventHandler());
		canvas.removeInputEventListener(canvas.getZoomEventHandler());

		repaint();

		canvas.getCamera().addChild(contentCanvas.getLayer());
		
		drawMenuBar();
	}
	
	public long getId()
	{
		return uuid;
	}

	public PLayer getLayer()
	{
		return contentCanvas.getLayer();
	}

	public JComponent getComponent()
	{
		return canvas;
	}

	public void repaint()
	{
		canvas.repaint();
		contentCanvas.repaint();
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
			canvas.getCamera().removeChild(menuBar);
		}

		menuBar = new IntentionGraphMenuBar(CanvasMenuBar.POSITION_BOTTOM);
		canvas.getCamera().addChild(menuBar);

		contentCanvas.setBounds(0, 0, CalicoDataStore.ScreenWidth, (int) (CalicoDataStore.ScreenHeight - menuBar.getBounds().height));
	}

	public boolean processToolEvent(InputEventInfo event)
	{
		if (menuBar.isPointInside(event.getGlobalPoint()))
		{
			menuBar.processEvent(event);
			return true;
		}
		return false;
	}

	public void addMouseListener(MouseListener listener)
	{
		canvas.addMouseListener(listener);
	}

	public void addMouseMotionListener(MouseMotionListener listener)
	{
		canvas.addMouseMotionListener(listener);
	}

	public void removeMouseListener(MouseListener listener)
	{
		canvas.removeMouseListener(listener);
	}

	public void removeMouseMotionListener(MouseMotionListener listener)
	{
		canvas.removeMouseMotionListener(listener);
	}

	private class ContainedCanvas extends PCanvas
	{
		public ContainedCanvas()
		{
			super.removeInputSources();
		}
	}
}
