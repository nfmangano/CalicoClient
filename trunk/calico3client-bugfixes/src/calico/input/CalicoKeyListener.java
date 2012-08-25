package calico.input;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoOptions;
import calico.components.bubblemenu.BubbleMenu;
import calico.components.decorators.CListDecorator;
import calico.components.grid.CGrid;
import calico.components.menus.buttons.CanvasNavButton;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.networking.Networking;
import calico.networking.netstuff.CalicoPacket;
import calico.networking.netstuff.NetworkCommand;
import calico.perspectives.CanvasPerspective;
import calico.perspectives.GridPerspective;

public class CalicoKeyListener extends KeyAdapter {

    public void keyPressed(KeyEvent evt) {
    	
    	int buttonType = -2;
        // Check for key characters.
        if (evt.getKeyCode() == KeyEvent.VK_LEFT || evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            buttonType = CanvasNavButton.TYPE_LEFT;
        }

        if (evt.getKeyCode() == KeyEvent.VK_RIGHT || evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
        	buttonType = CanvasNavButton.TYPE_RIGHT;
        }
        
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
        	buttonType = CanvasNavButton.TYPE_UP;
        }
        
        if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
        	buttonType = CanvasNavButton.TYPE_DOWN;
        }
        
        if (buttonType > -1)
        	moveToCell(buttonType);
        
        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
 
        	if (!GridPerspective.getInstance().isActive())
        		CGrid.loadGrid();
        }
        
        if (CanvasPerspective.getInstance().isActive() && evt.getKeyCode() == KeyEvent.VK_ENTER) {
        	createTextScrap();
        }
    }
    
    private void moveToCell(int button_type)
    {
		// Grid Size
		int gridx = CalicoDataStore.GridCols-1;
		int gridy = CalicoDataStore.GridRows-1;
				
		// Canvas Coords
		long cuuid = CCanvasController.getLastActiveUUID();
		int xpos = CCanvasController.canvasdb.get(cuuid).getGridCol();
		int ypos = CCanvasController.canvasdb.get(cuuid).getGridRow();
    	
		switch(button_type)
		{
			case CanvasNavButton.TYPE_DOWN:
				if((ypos+1)<=gridy)
				{
					
					
					loadCanvas(xpos,ypos+1);
					
				}
				else
				{
					loadCanvas(xpos,0);
				}
				break;
				
			case CanvasNavButton.TYPE_UP:
				if((ypos-1)>=0)
				{
					loadCanvas(xpos,ypos-1);
				}
				else
				{
					loadCanvas(xpos,gridy);
				}
				break;
				
			case CanvasNavButton.TYPE_LEFT:
				if((xpos-1)>=0)
				{
					loadCanvas(xpos-1,ypos);
				}
				else
				{
					loadCanvas(gridx,ypos);
				}
				break;
				
			case CanvasNavButton.TYPE_RIGHT:
				if((xpos+1)<=gridx)
				{
					loadCanvas(xpos+1,ypos);
				}
				else
				{
					loadCanvas(0,ypos);
				}
				break;
		}
	
    }
    
	private void loadCanvas(int x, int y)
	{
		long cuid = CCanvasController.getCanvasAtPos(x, y);
		
		if(cuid==0L)
		{
			// Error
			return;
		}
		CCanvasController.unloadCanvasImages(CCanvasController.getCurrentUUID());
		CCanvasController.loadCanvas(cuid);
	}
	
	private void createTextScrap()
	{
		int xPos = CalicoDataStore.ScreenWidth/3, yPos = CalicoDataStore.ScreenHeight/3;
		boolean updateBubbleIcons = false;
		if (BubbleMenu.activeUUID != 0l && CGroupController.groupdb.get(BubbleMenu.activeUUID) instanceof CListDecorator)
		{
			xPos = CGroupController.groupdb.get(BubbleMenu.activeUUID).getPathReference().getBounds().x + 50;
			yPos = CGroupController.groupdb.get(BubbleMenu.activeUUID).getPathReference().getBounds().y
					+ CGroupController.groupdb.get(BubbleMenu.activeUUID).getPathReference().getBounds().height - 10;
			updateBubbleIcons = true;
		}
		else
		{
			while (CGroupController.get_smallest_containing_group_for_point(CCanvasController.getCurrentUUID(), new Point(xPos, yPos)) != 0)
				yPos += CalicoOptions.group.padding * 4;
		}
		
		
		String response = JOptionPane.showInputDialog(CCanvasController.canvasdb.get(CCanvasController.getCurrentUUID()).getComponent(),
				  "Create Scrap with Text",
				  "Please enter text",
				  JOptionPane.QUESTION_MESSAGE);
		
		long new_uuid = 0l;
		if (response != null && response.length() > 0)
		{
			if (isImageURL(response))
			{
				new_uuid = Calico.uuid();
				Networking.send(CalicoPacket.getPacket(NetworkCommand.GROUP_IMAGE_DOWNLOAD, new_uuid, CCanvasController.getCurrentUUID(), response, 50, 50));
			}
			else
			{
				new_uuid = Calico.uuid();
				CGroupController.create_text_scrap(new_uuid, CCanvasController.getCurrentUUID(), response, xPos, yPos);
				
				if (updateBubbleIcons)
					BubbleMenu.moveIconPositions(CGroupController.groupdb.get(BubbleMenu.activeUUID).getBounds());
			}
		}
//		CGroupController.move_start(new_uuid);
//		CGroupController.move_end(new_uuid, xPos, yPos);
	}
	
	private boolean isImageURL(String text)
	{
		String regex = "((https?|ftp|gopher|telnet|file|notes|ms-help):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)+\\.(?:gif|jpg|jpeg|png|bmp|GIF|JPEG|JPG|PNG|BMP|Gif|Jpg|Jpeg|Png|Bmp)$";
		Pattern pattern = Pattern.compile(regex); 
		Matcher matcher = pattern.matcher(text); 
		return matcher.matches();
	}
	
}

