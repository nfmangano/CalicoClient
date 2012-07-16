package calico.plugins.analysis.transformation.uml2prism.prism;

/**
 * Every Activity diagram node is mapped to a prism state
 * A prism state is the couple <module_state_var_name, state_number>
 * For example in the following prism module the initial node of the activity diagram
 * has been mapped to the couple <adActivity1_fork0, 0>
 * 
 * module adActivity1_fork0

	adActivity1_fork0 : [0..5] init 0;
	
	[] adActivity1_fork0 = 0 -> 1.0 : (adActivity1_fork0' = 1);
	[] adActivity1_fork0 = 1 -> 1.0 : (adActivity1_fork0' = 2);
	[] adActivity1_fork0 = 2 -> 1.0 : (adActivity1_fork0' = 3);
	[] adActivity1_fork0 = 3 -> 0.5 : (adActivity1_fork0' = 5) + 0.5 : (adActivity1_fork0' = 4);
	[a_0] true -> 1.0 : (adActivity1_fork0' = 4);
	[] adActivity1_fork0 = 4 -> 1.0 : (adActivity1_fork0' = 1);
	
	
	endmodule
 * @author motta
 *
 */
public class PrismState {
	
	private String stateVarName;
	private int stateNumber;
	
	//If this state is inside an inner fork
	//I need to retrieve its father
	PrismState parentForkNode;
	
	public PrismState getParentForkNode() {
		return parentForkNode;
	}

	public void setParentForkNode(PrismState parent) {
		this.parentForkNode = parent;
	}

	public PrismState(String stateVarName, int stateNumber){
		this.stateVarName=stateVarName;
		this.stateNumber=stateNumber;
	}
	
	public String getStateVarName() {
		return stateVarName;
	}
	
	public void setStateVarName(String stateVarName) {
		this.stateVarName = stateVarName;
	}
	
	public int getStateNumber() {
		return stateNumber;
	}
	
	public void setStateNumber(int stateNumber) {
		this.stateNumber = stateNumber;
	}
	
	

}
