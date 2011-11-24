package calico.input;

import calico.*;

import it.unimi.dsi.fastutil.ints.*;


public class CInputDevice
{
	public static final int TYPE_MOUSE = 1;
	public static final int TYPE_PEN = 2;

	public int deviceID = 0;
	public int type = CInputDevice.TYPE_PEN;

	public int xpos = 0;
	public int ypos = 0;

	public boolean button_left = false;
	public boolean button_right = false;
	public boolean button_center = false;

	public CInputDevice()
	{

	}

}//
