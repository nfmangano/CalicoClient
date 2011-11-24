package calico.events;

import java.lang.reflect.*;

import java.util.ArrayList;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;

public class CalicoEventHandler {

	/**
	 * ACTION_PERFORMER_LISTENER are for listeners that "own" a particular event. The primary
	 * users of this listener are plugins do not have their method located in PacketHandler or ProcessQueue.
	 * 
	 * These listeners will be notified first
	 */
	public static final int ACTION_PERFORMER_LISTENER = 1;
	
	/**
	 * PASSIVE_LISTENER are for listeners that respond to some event, such as updating their local variables
	 * in response to a user entering a canvas.
	 */
	public static final int PASSIVE_LISTENER = 2;
	
	private static CalicoEventHandler instance = new CalicoEventHandler();
	
	private static Int2ReferenceOpenHashMap<ArrayList<CalicoEventListener>> eventListeners;
	
	public static CalicoEventHandler getInstance()
	{
		return instance;
	}
	
	public CalicoEventHandler()
	{
		eventListeners = new Int2ReferenceOpenHashMap<ArrayList<CalicoEventListener>>();
		registerEvents();
//		System.out.println("Instanciated the Calico Event Handler Class!");
	}
	
	
	
	private void registerEvents()
	{
		Class<?> rootClass = NetworkCommand.class;
		Field[] fields = rootClass.getFields();
		
		try
		{
			for (int i = 0; i < fields.length; i++)
			{
				if (fields[i].getType() == int.class)
				{
					fields[i].setAccessible(true);
					int value = fields[i].getInt(NetworkCommand.class);
					addEvent(value);
//					System.out.println("Registering event for: " + fields[i].getName() + ", value: " + fields[i].getInt(NetworkCommand.class));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	public boolean addEvent(int value)
	{
		if (eventListeners.containsKey(value))
		{
			return false;
		}
		if (value >= 2200 && value < 2300)
			System.out.println("found a palette one!");
		
		eventListeners.put(value, new ArrayList<CalicoEventListener>());
		return true;
	}
	
	public void addListener(int event, CalicoEventListener listener, int listenerType)
	{
		if (!eventListeners.containsKey(event) || listener == null)
			return;
		
		if (listenerType == CalicoEventHandler.ACTION_PERFORMER_LISTENER)
			eventListeners.get(event).add(0, listener);
		else
			eventListeners.get(event).add(listener);
		
//		System.out.println("Added listener " + listener.getClass().getName() + " for value " + event);
	}
	
	public void addListenerForType(String type, CalicoEventListener listener, int listenerType)
	{
		Class<?> rootClass = NetworkCommand.class;
		Field[] fields = rootClass.getFields();
		
		try
		{
			for (int i = 0; i < fields.length; i++)
			{
				if (fields[i].getType() == int.class)
				{
					if (fields[i].getName().length() >= type.length() && fields[i].getName().substring(0, type.length()).compareTo(type) == 0)
					{
						fields[i].setAccessible(true);
						int command = fields[i].getInt(NetworkCommand.class);
						if (listenerType == CalicoEventHandler.ACTION_PERFORMER_LISTENER)
							eventListeners.get(command).add(0, listener);
						else
							eventListeners.get(command).add(listener);
//						eventListeners.get(command).add(listener);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void fireEvent(int event, CalicoPacket p)
	{
		if (!eventListeners.containsKey(event))
			return;
		
		ArrayList<CalicoEventListener> listeners = eventListeners.get(event);
		
		for (CalicoEventListener listener : listeners)
			listener.handleCalicoEvent(event, p);
	}
	
}
