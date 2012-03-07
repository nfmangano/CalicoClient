package calico.components.piemenu.arrows;

import calico.*;
import calico.iconsets.CalicoIconManager;
import calico.inputhandlers.CalicoInputManager;
import calico.inputhandlers.InputEventInfo;
import calico.modules.StatusMessage;
import calico.components.*;
import calico.components.arrow.CArrow;
import calico.components.piemenu.PieMenuButton;
import calico.controllers.CArrowController;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;

import java.awt.*;

@Deprecated
public class ArrowSetAnchorButton extends PieMenuButton
{
	private Point point = null;
	public ArrowSetAnchorButton(Point p)
	{
		super("arrow.create");
		point = p;
	}
	
	
	public void onClick(InputEventInfo ev)
	{
		if(CArrowController.getOutstandingAnchorPoint()==null)
		{
			CArrowController.setOutstandingAnchorPoint(point);
			
			//CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).menuBar.redrawArrowIndicator();
			
			if(CalicoOptions.arrow.show_creation_popup)
			{
				StatusMessage.popup("Please click to place the end point for the arrow.");
			}
		}
		else
		{
			// It is not null, so we want to finish it
			Point start = CArrowController.getOutstandingAnchorPoint();
			Point end = point;
			long startGroupUID = CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), start);
			long endGroupUID = CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), end);
			if(endGroupUID==startGroupUID && endGroupUID!=0L)
			{
				return;
			}

			long curCanvasUID = CCanvasController.getCurrentUUID();
			int startType = CArrow.TYPE_GROUP;
			int endType = CArrow.TYPE_GROUP;
			
			if(startGroupUID==0L)
			{
				startGroupUID = curCanvasUID;
				startType = CArrow.TYPE_CANVAS;
			}
			if(endGroupUID==0L)
			{
				endGroupUID = curCanvasUID;
				endType = CArrow.TYPE_CANVAS;
			}

			// ARROW!
			/*CArrowController.start(Calico.uuid(), curCanvasUID, 
					startType, startGroupUID, start,
					endType, endGroupUID, end
				);
				*/
		
			CArrowController.setOutstandingAnchorPoint(null);
			
			//CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).menuBar.redrawArrowIndicator();
			
		}
		ev.stop();
		
	}
	

	
}
