package calico.plugins.iip.components.menus.buttons;

import calico.plugins.iip.components.canvas.CanvasIntentionToolBarItem;

public class NewIdeaButton extends CanvasIntentionToolBarItem
{
	public NewIdeaButton()
	{
		super("intention.new-idea");
	}

	@Override
	protected void onClick()
	{
		System.out.println("Let's create a new idea in a new canvas and link it from canvas #" + canvas_uuid);
	}
}
