package calico;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import calico.components.CSession;
import calico.components.grid.CGrid;
import calico.controllers.CCanvasController;
import calico.input.CInputMode;



public class CalicoDataStore
{
	// Object Storage
	public static CGrid gridObject = null;

	/**
	 * This is a reference back to the main Calico object
	 */
	public static Calico calicoObj	= null;
	public static StatusMessageHandler messageHandlerObj = null;

	public static int ScreenWidth = 0;
	public static int ScreenHeight = 0;
	public static boolean isFullScreen = false;
	public static final Dimension CanvasSnapshotSize = new Dimension();

	public static String SessionName = "";
	public static String Username = "SDCL-";
	public static String Password = "";

	public static boolean SkipConnectionScreen = false;
	
	public static String ServerHost = null;
	public static int ServerPort = 0;
	
	
	public static int GridRows = 0;
	public static int GridCols = 0;
	
	public static boolean RunStressTest = false;
	public static long StressTestInterval = Integer.MAX_VALUE;
	public static long timeLastStressPacketSent = 0;
	
	/**
	 * This is the current operating mode we are in
	 */
	public static CInputMode Mode = CInputMode.EXPERT;
	
	/**
	 * This is the current pen color
	 */
	public static Color PenColor = Color.BLACK;
	public static float PenThickness = CalicoOptions.pen.stroke_size;
	
	public static Color LastDrawingColor = Color.BLACK;
	public static Color PointingColor = Color.ORANGE;
	
	public static float LastDrawingThickness = CalicoOptions.pen.stroke_size;
	
	
	// This is used to see when the last mouse/keyboard/finger/whatever was inputted
	// We will probably use this thing for running tasks after a certain amount of time.
	public static long LastUserActionTime = 0L;

	public static boolean initialScreenDisplayed = false;
	
	public static ArrayList<CSession> sessiondb = new ArrayList<CSession>();
	
	
	public static Int2ObjectOpenHashMap<String> clientInfo = new Int2ObjectOpenHashMap<String>();
	
	
	/**
	 * Sets the default options
	 */
	public static void setup()
	{
		Mode = CalicoOptions.canvas.input.default_mode;
		PenColor = CalicoOptions.stroke.default_color;
		CalicoDataStore.messageHandlerObj = StatusMessageHandler.getInstance();
		
		LastUserActionTime = System.currentTimeMillis();
	}
	
	public static void set_Mode(CInputMode mode)
	{

//		if (Mode == Calico.MODE_EXPERT)
//			LastDrawingColor = PenColor;
//		if (Mode == Calico.MODE_POINTER)
//			PointingColor = PenColor;

		if (mode == CInputMode.EXPERT)
		{
			PenColor = LastDrawingColor;
			PenThickness = LastDrawingThickness;
		}
		if (mode == CInputMode.POINTER)
		{
			PenColor = PointingColor;
			PenThickness = 4.0f;
		}
		if (mode == CInputMode.ARROW)
		{
			PenColor = LastDrawingColor;
		}
		
		Mode = mode;
		CCanvasController.canvasModeChanged();
		
		//Calico.logger.debug("Switching to mode "+Mode+" ("+Mode_Reverse+")");
	}
	
	
	public static void touch_input()
	{
		LastUserActionTime = System.currentTimeMillis();
	}
	
	
}
