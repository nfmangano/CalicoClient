package calico.components.menus;

import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JOptionPane;

import calico.Calico;
import calico.controllers.CCanvasController;
import calico.networking.Networking;
import calico.networking.netstuff.NetworkCommand;

public class CanvasStatusBar extends CanvasGenericMenuBar
{
	private static final long serialVersionUID = 1L;

	private long cuid = 0L;
	
	public CanvasStatusBar(long c)
	{
		super(CanvasGenericMenuBar.POSITION_BOTTOM, CCanvasController.canvasdb.get(c).getBounds());		
		
		cuid = c;
		
		//Begin align right
		addTextEndAligned(
				"  Exit  ", 
				new Font("Verdana", Font.BOLD, 12),
				new CanvasTextButton(cuid) {
					public void actionMouseClicked(Rectangle boundingBox) {
						Calico.exit();
					}
				}
		);
		
		if (Networking.connectionState == Networking.ConnectionState.Connecting)
		{
			addSpacer(ALIGN_END);
			addTextEndAligned(
					" reconnect...   ", 
					new Font("Verdana", Font.BOLD, 12),
					new CanvasTextButton(cuid) {
						public void actionMouseClicked(Rectangle boundingBox) {
							//Do nothing
						}
					}
			);
			
			addTextEndAligned(
					" Attempting to", 
					new Font("Verdana", Font.BOLD, 12),
					new CanvasTextButton(cuid) {
						public void actionMouseClicked(Rectangle boundingBox) {
							//Do nothing
						}
					}
			);
			
			addTextEndAligned(
					"   Disconnected!", 
					new Font("Verdana", Font.BOLD, 12),
					new CanvasTextButton(cuid) {
						public void actionMouseClicked(Rectangle boundingBox) {
							//Do nothing
						}
					}
			);

		}

		if (!Networking.synchroized)
		{
			addSpacer(ALIGN_END);
			addTextEndAligned(
					" LOST SYNC!!  ", 
					new Font("Verdana", Font.BOLD, 12),
					new CanvasTextButton(cuid) {
						public void actionMouseClicked(Rectangle boundingBox) {
							int result = JOptionPane.showOptionDialog(null, "The canvas is not synchronized with the server. Press OK to synchronize", "Out of Sync Alert!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
							if (result == 0)
							{
								Networking.send(NetworkCommand.CONSISTENCY_RESYNC_CANVAS, CCanvasController.getCurrentUUID());
							}
						}
					}
			);
		}
		
		this.invalidatePaint();
	}
	
}
