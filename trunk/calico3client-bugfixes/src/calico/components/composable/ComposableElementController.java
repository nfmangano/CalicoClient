package calico.components.composable;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Map;

import calico.components.CConnector;
import calico.components.composable.connectors.ArrowheadElement;
import calico.components.composable.connectors.CardinalityElement;
import calico.components.composable.connectors.ColorElement;
import calico.components.composable.connectors.HighlightElement;
import calico.components.composable.connectors.LabelElement;
import calico.components.composable.connectors.LineStyleElement;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

public class ComposableElementController {

	/**
	 * This is the database of all the components with elements. The map returns an arraylist containing all the individual elements for that component.
	 */
	public static Long2ReferenceAVLTreeMap<Long2ReferenceAVLTreeMap<ComposableElement>> elementList = new Long2ReferenceAVLTreeMap<Long2ReferenceAVLTreeMap<ComposableElement>>();
	
	private static Int2ReferenceOpenHashMap<Class<? extends ComposableElement>> composableElementList = new Int2ReferenceOpenHashMap<Class<? extends ComposableElement>>();
	
	static {
		registerComposableElement(0, ArrowheadElement.class);
		registerComposableElement(1, CardinalityElement.class);
		registerComposableElement(2, ColorElement.class);
		registerComposableElement(3, HighlightElement.class);
		registerComposableElement(4, LabelElement.class);
		registerComposableElement(5, LineStyleElement.class);
	}

	public static void addElement(ComposableElement e)
	{
		no_notify_addElement(e);
		
		Networking.send(e.getPacket());
	}
	
	public static void registerComposableElement(int index, Class<? extends ComposableElement> e)
	{
		if (!composableElementList.containsKey(index))
			composableElementList.put(index, e);
		else
		{
			System.out.println("WARNING: Index " + index + " for " + e.getName() + " is already in use by " + composableElementList.get(index).getName());
			(new Exception()).printStackTrace();
		}
	}
	
	/**
	 * Add an element to a composable component
	 * @param e: The composable element
	 */
	public static void no_notify_addElement(ComposableElement e)
	{
		long cuuid = e.getComponentUUID();
		long euuid = e.getElementUUID();
		if (!elementList.containsKey(cuuid))
		{
			elementList.put(cuuid, new Long2ReferenceAVLTreeMap<ComposableElement>());
		}
		
		Long2ReferenceAVLTreeMap<ComposableElement> componentElements = elementList.get(cuuid);
		componentElements.put(euuid, e);
		e.applyElement();
		
		Composable composable = e.getComposable();
		if (composable != null)
		{
			composable.redraw();
		}
	}
	
	public static void removeElement(long euuid, long cuuid)
	{
		no_notify_removeElement(euuid, cuuid);
		
		Networking.send(NetworkCommand.ELEMENT_REMOVE, euuid, cuuid);
	}
	
	/**
	 * Remove an element from a composable component
	 * @param euuid: The UUID of the element
	 * @param cuuid: The UUID of the component (ie. connector)
	 */
	public static void no_notify_removeElement(long euuid, long cuuid)
	{
		if (!elementList.containsKey(cuuid))
		{
			return;
		}
		
		Long2ReferenceAVLTreeMap<ComposableElement> componentElements = elementList.get(cuuid);
		if (componentElements.containsKey(euuid))
		{
			Composable composable = componentElements.get(euuid).getComposable();
			componentElements.get(euuid).removeElement();
			
			componentElements.remove(euuid);
			if (componentElements.size() == 0)
			{
				elementList.remove(cuuid);
			}
			composable.redraw();
		}
	}
	
	public static void no_notify_removeAllElements(long cuuid)
	{
		if (!elementList.containsKey(cuuid))
		{
			return;
		}
		
		Long2ReferenceAVLTreeMap<ComposableElement> componentElements = elementList.get(cuuid);
		for (Map.Entry<Long, ComposableElement> entry : componentElements.entrySet())
		{
			entry.getValue().removeElement();
			
			componentElements.remove(entry.getValue().getElementUUID());
		}
		
		elementList.remove(cuuid);
	}
	
	/**
	 * Takes in a packet containing an element and returns the ComposableElement object
	 * @param packet
	 * @return
	 */
	public static ComposableElement getElementFromPacket(CalicoPacket packet)
	{
		packet.rewind();
		if (packet.getInt() != NetworkCommand.ELEMENT_ADD)
			return null;
		
		int elementType = packet.getInt();
//		long uuid = packet.getLong();
//		long cuuid = packet.getLong();
		
		return ComposableElementController.getInstanceFromPacket(composableElementList.get(elementType), packet);
		
//		switch(elementType)
//		{
//		case ComposableElement.TYPE_ARROWHEAD :
//			element = 
//			int type = packet.getInt();
//			float strokeSize = packet.getFloat();
//			Color outlineColor = packet.getColor();
//			Color fillColor = packet.getColor();
//			
//			Polygon polygon = new Polygon();
//			int npoints = packet.getInt();
//			for (int i = 0; i < npoints; i++)
//			{
//				polygon.addPoint(packet.getInt(), packet.getInt());
//			}
//			
//			element = new ArrowheadElement(uuid, cuuid, type, strokeSize, outlineColor, fillColor, polygon);
//			break;
//			
//		case ComposableElement.TYPE_CARDINALITY :
//			type = packet.getInt();
//			String text = packet.getString();
//			String fontName = packet.getString();
//			int fontStyle = packet.getInt();
//			int fontSize = packet.getInt();
//			Font font = new Font(fontName, fontStyle, fontSize);
//			
//			element = new CardinalityElement(uuid, cuuid, type, text, font);
//			break;
//			
//		case ComposableElement.TYPE_COLOR :
//			Color newColor = packet.getColor();
//			Color originalColor = packet.getColor();
//			
//			element = new ColorElement(uuid, cuuid, newColor, originalColor);
//			break;
//			
//		case ComposableElement.TYPE_HIGHLIGHT :
//			float transparency = packet.getFloat();
//			
//			float lineWidth = packet.getFloat();
//			int endCap = packet.getInt();
//			int lineJoin = packet.getInt();
//			float miterLimit = packet.getFloat();
//			int dashLength = packet.getInt();
//			float[] dash = (dashLength == 0) ? null : new float[dashLength];
//			for (int i = 0; i < dashLength; i++)
//			{
//				dash[i] = packet.getFloat();
//			}
//			float dashPhase = packet.getFloat();
//			BasicStroke stroke = new BasicStroke(lineWidth, endCap, lineJoin, miterLimit, dash, dashPhase);
//			
//			Color color = packet.getColor();
//			
//			element = new HighlightElement(uuid, cuuid, transparency, stroke, color);
//			break;
//			
//		case ComposableElement.TYPE_LABEL :
//			text = packet.getString();
//			fontName = packet.getString();
//			fontStyle = packet.getInt();
//			fontSize = packet.getInt();
//			
//			font = new Font(fontName, fontStyle, fontSize);
//			
//			element = new LabelElement(uuid, cuuid, text, font);
//			break;
//			
//		case ComposableElement.TYPE_LINESTYLE :
//			lineWidth = packet.getFloat();
//			endCap = packet.getInt();
//			lineJoin = packet.getInt();
//			miterLimit = packet.getFloat();
//			dashLength = packet.getInt();
//			dash = (dashLength == 0) ? null : new float[dashLength];
//			for (int i = 0; i < dashLength; i++)
//			{
//				dash[i] = packet.getFloat();
//			}
//			dashPhase = packet.getFloat();
//			BasicStroke newStroke = new BasicStroke(lineWidth, endCap, lineJoin, miterLimit, dash, dashPhase);
//			
//			lineWidth = packet.getFloat();
//			endCap = packet.getInt();
//			lineJoin = packet.getInt();
//			miterLimit = packet.getFloat();
//			dashLength = packet.getInt();
//			dash = (dashLength == 0) ? null : new float[dashLength];
//			for (int i = 0; i < dashLength; i++)
//			{
//				dash[i] = packet.getFloat();
//			}
//			dashPhase = packet.getFloat();
//			BasicStroke originalStroke = new BasicStroke(lineWidth, endCap, lineJoin, miterLimit, dash, dashPhase);
//			
//			element = new LineStyleElement(uuid, cuuid, newStroke, originalStroke);
//			break;
//		
//		default: return null;
//		}
//		
//		
//		return element;
	}
	
	public static ComposableElement getInstanceFromPacket(Class<? extends ComposableElement> ce, CalicoPacket p)
	{
		try {
			return ce.newInstance().getInstanceFromPacket(p);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
