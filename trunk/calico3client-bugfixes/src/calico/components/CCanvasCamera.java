package calico.components;

import java.util.List;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PPaintContext;

public class CCanvasCamera extends PCamera
{
	//private PCamera theRealCamera = null;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient PComponent component;
    private transient List layers;
   
    private final PAffineTransform viewTransform;

    private int viewConstraint;

	
	public CCanvasCamera(PCamera camera)
	{
		super();
		layers = camera.getLayersReference();
		component = camera.getComponent();
		viewTransform = camera.getViewTransform();
		viewConstraint = camera.getViewConstraint();
		//theRealCamera = camera;
	}
	
	public void fullPaint(final PPaintContext paintContext)
	{
		System.out.println("CALLING THE REAL CAMERA PAINT");
		super.fullPaint(paintContext);
	}
	
	
}