package calico.inputhandlers;

import java.awt.*;

import calico.*;
import calico.components.*;
import calico.components.arrow.CArrow;
import calico.components.piemenu.*;
import calico.components.piemenu.arrows.ArrowDeleteButton;
import calico.components.piemenu.arrows.ChangeArrowColorButton;
import calico.controllers.CArrowController;
import calico.modules.*;
import calico.networking.*;

import java.awt.Color;
import java.awt.geom.*;
import java.util.*;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolox.nodes.PLine;

@Deprecated
public class CArrowInputHandler extends CalicoAbstractInputHandler
{
	public static Logger logger = Logger.getLogger(CArrowInputHandler.class.getName());
	
	
	private long uuid = 0L;

	
	public CArrowInputHandler(long u)
	{
		uuid = u;
	}
	
	private double getPercentage(InputEventInfo e)
	{
		return CArrowController.arrows.get(uuid).getPointPercentage(e.getPoint());
	}
	private int getRegion(InputEventInfo e)
	{
		return CArrow.getPercentRegion(getPercentage(e));
	}
	
	public void actionPressed(InputEventInfo e)
	{
		Calico.logger.debug("ARROW_PRESSED("+uuid+")");
		//Calico.log_debug(getPercentage(e)+"%|"+e.toString());
	}


	public void actionDragged(InputEventInfo e)
	{
		Calico.logger.debug("ARROW_DRAGGED("+uuid+")");
		//Calico.log_debug(getPercentage(e)+"%|"+e.toString());
	}


	public void actionReleased(InputEventInfo e)
	{
		int region = getRegion(e);
		if(region==CArrow.REGION_HEAD)
		{
			Calico.logger.debug("REGION: HEAD");
			
			
			
		}
		else if(region==CArrow.REGION_TAIL)
		{
			Calico.logger.debug("REGION: TAIL");
		}
		else if(region==CArrow.REGION_MIDDLE)
		{
			Calico.logger.debug("REGION: MIDDLE");
		}
		
		PieMenu.displayPieMenu(e.getGlobalPoint(), 
				new ArrowDeleteButton(uuid),
				new ChangeArrowColorButton(uuid)
		);
		
		Calico.logger.debug("ARROW_RELEASED("+uuid+")");
		//Calico.log_debug(getPercentage(e)+"%|"+e.toString());
		
	}

}
