package calico.modules;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.awt.Font;
import java.awt.Rectangle;

import calico.Calico;
import calico.CalicoDataStore;
import calico.CalicoDraw;
import calico.CalicoOptions;
import calico.controllers.CCanvasController;
import calico.perspectives.GridPerspective;
import calico.utils.Ticker;
import calico.utils.TickerTask;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.nodes.PComposite;

// TODO: If the current canvas is 0L, then we need to abort this.


/**
 * This class handles all processing of error messages
 * 
 * @author Mitch Dempsey
 */
public class MessageObject extends PComposite
{
	private static final long serialVersionUID = 1L;

	public static ObjectArrayList<Rectangle> rect_list = new ObjectArrayList<Rectangle>();
	
	
	public static final int TYPE_ERROR = 1 << 0;
	public static final int TYPE_NOTICE = 1 << 1;
	public static final int TYPE_SUCCESS = 1 << 2;
	
	private long canvas_uid = 0L;
	
	//private Timer removeMessageTimer = null;
	
	private class RemoveMessageTimer extends TickerTask//TimerTask
	{
		private long canvasuid = 0L;
		private MessageObject smobj = null;
		private Rectangle objbounds = null;
		
		public RemoveMessageTimer(long cuid, MessageObject obj, Rectangle rect)
		{
			canvasuid = cuid;
			smobj = obj;
			objbounds = rect;
			MessageObject.rect_list.add(rect);
		}
		
		public boolean runtask()
		{
			if(CCanvasController.getCurrentUUID()==canvasuid|| GridPerspective.getInstance().isActive())
			{
				try
				{
					for(int i=10;i>1;i--)
					{
						//Calico.logger.debug("fading");
						//smobj.setTransparency((float) (i/10.0));
						CalicoDraw.setNodeTransparency(smobj, (float) (i/10.0));
						//smobj.invalidatePaint();
						//smobj.setPaintInvalid(true);
						CalicoDraw.setNodePaintInvalid(smobj, true);
						//smobj.repaint();
						CalicoDraw.repaint(smobj);
						/*
						if(CalicoDataStore.isViewingGrid)
						{
							CalicoDataStore.gridObject.getCamera().repaint();
						}
						else
						{
							CCanvasController.canvasdb.get(canvasuid).getCamera().repaint();
						}*/
						Thread.sleep(CalicoOptions.messagebox.fade_time_pause);
					}
				}
				catch(Exception e)
				{
					// the fade effect failed... who cares
				}
			}
			else
			{
				//CCanvasController.canvasdb.get(canvasuid).getCamera().removeChild(smobj);
			}
			MessageObject.rect_list.rem(objbounds);
			//smobj.removeFromParent();
			CalicoDraw.removeNodeFromParent(smobj);
			CalicoDataStore.calicoObj.getContentPane().getComponent(0).repaint();
			return false;
		}
		
	}
	
	public MessageObject(String msg)
	{
		this(CCanvasController.getCurrentUUID(), msg, MessageObject.TYPE_NOTICE);
	}
	
	public MessageObject(String msg, int type)
	{
		this(CCanvasController.getCurrentUUID(), msg, type);
	}
	
	public MessageObject(long cuid, String msg, int type)
	{
		canvas_uid = cuid;
		
		
		
		
		if(type==MessageObject.TYPE_ERROR)
		{
			Calico.logger.warn(msg);
		}
		else if(type==MessageObject.TYPE_NOTICE)
		{
			Calico.logger.info(msg);
		}
		else if(type==MessageObject.TYPE_SUCCESS)
		{
			Calico.logger.info(msg);
		}

		PText text = new PText(msg);
		text.setConstrainWidthToTextWidth(true);
		text.setFont(new Font(CalicoOptions.messagebox.font.name, Font.BOLD, CalicoOptions.messagebox.font.size));
		text.setConstrainHeightToTextHeight(true);
		text.setConstrainWidthToTextWidth(true);
		text.setTextPaint(CalicoOptions.messagebox.color.text);
		
		text.recomputeLayout();
		Rectangle ntextbounds = text.getBounds().getBounds();
		

		int padding = CalicoOptions.messagebox.padding;
		int padadd = padding*2;
		
		
		int lastVertPos = 30;
		
		if(MessageObject.rect_list.size()>0)
		{
			Rectangle temp = MessageObject.rect_list.get(MessageObject.rect_list.size()-1);
			lastVertPos = temp.height + temp.y;
		}
		
		
		
		//int vertpos = CCanvasController.canvasdb.get(canvas_uid).messageBoxOffset;
		

		Rectangle bounds = new Rectangle(16,lastVertPos,ntextbounds.width+padadd,ntextbounds.height+padadd);
		Rectangle textbounds = new Rectangle(bounds.x+padding,bounds.y+padding,ntextbounds.width,ntextbounds.height);
		

		PNode bgnode = new PNode();
		
		// What color should we have
		if(type==MessageObject.TYPE_ERROR)
		{
			bgnode.setPaint(CalicoOptions.messagebox.color.error);
		}
		else if(type==MessageObject.TYPE_NOTICE)
		{
			bgnode.setPaint(CalicoOptions.messagebox.color.notice);
		}
		else if(type==MessageObject.TYPE_SUCCESS)
		{
			bgnode.setPaint(CalicoOptions.messagebox.color.success);
		}
		
		bgnode.setBounds(bounds);
		
		
		text.setBounds(textbounds);

		/*this.addChild(0,bgnode);
		this.addChild(1,text);
		this.setBounds(bounds);*/
		CalicoDraw.addChildToNode(this, bgnode, 0);
		CalicoDraw.addChildToNode(this, text, 1);
		CalicoDraw.setNodeBounds(this, bounds);

		//((PCanvas)CalicoDataStore.calicoObj.getContentPane().getComponent(0)).getCamera().addChild(0, this);
		CalicoDraw.addChildToNode(((PCanvas)CalicoDataStore.calicoObj.getContentPane().getComponent(0)).getCamera(), this, 0);
		CalicoDataStore.calicoObj.getContentPane().getComponent(0).repaint();
		//((PCanvas)CalicoDataStore.calicoObj.getContentPane().getComponent(0)).getCamera().repaint();
		CalicoDraw.repaint(((PCanvas)CalicoDataStore.calicoObj.getContentPane().getComponent(0)).getCamera());
		
		Rectangle newBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height+padding);
		
		//removeMessageTimer = new Timer("MessageFadeTimer",true);
		//removeMessageTimer.schedule(new RemoveMessageTimer(canvas_uid,this,newBounds), CalicoOptions.messagebox.fade_time);
		
		Ticker.scheduleIn(CalicoOptions.messagebox.fade_time, new RemoveMessageTimer(canvas_uid,this,newBounds));
		
	}
	
	
	/**
	 * These messages are being castrated for now until things are fixed.
	 * 
	 * 	-Nick
	 * 
	 * @param msg
	 */
	public static void showNotice(String msg)
	{
		//new MessageObject(msg, MessageObject.TYPE_NOTICE);
	}
	
	public static void showError(String msg)
	{
		//new MessageObject(msg, MessageObject.TYPE_ERROR);
	}
	
	public static void showSuccess(String msg)
	{
		//new MessageObject(msg, MessageObject.TYPE_SUCCESS);
	}
	
}