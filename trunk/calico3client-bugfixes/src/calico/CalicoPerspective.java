package calico;

public class CalicoPerspective
{
	private static final State STATE = new State();
	
	public boolean isActive()
	{
		return STATE.activePerspective == this;
	}
	
	public void activate()
	{
		STATE.activePerspective = this;
	}
	
	private static class State
	{
		private CalicoPerspective activePerspective = null;
	}
}
