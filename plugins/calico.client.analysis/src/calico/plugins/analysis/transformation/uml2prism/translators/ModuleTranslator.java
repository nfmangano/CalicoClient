package calico.plugins.analysis.transformation.uml2prism.translators;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.FinalNode;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.JoinNode;
import org.eclipse.uml2.uml.MergeNode;

import calico.plugins.analysis.transformation.uml2prism.Logger;
import calico.plugins.analysis.transformation.uml2prism.uml.ActivityEdgeDecorator;
import calico.plugins.analysis.transformation.uml2prism.uml.ActivityNodeDecorator;
import calico.plugins.analysis.transformation.uml2prism.uml.DecisionNodeDecorator;

/**
 * A translator which creates PRISM modules.
 * @author Alfredo Motta
 *
 */
class ModuleTranslator{
	
	/** The start nodes for this module */
	private Set<? extends ActivityNode> startNodes = null;
	
	/** A reference to the root translator */
	private AtopTranslator translator = null;
	
	/** The name of this module */
	private String moduleName = null;
	
	/** The name of the variable keeping the state of the module */
	private String stateVarName = null;
	
	/** A mapping between ActivityNode identifiers and state identifiers */
	private Map<ActivityNode,Integer> nodeIds = new HashMap<ActivityNode,Integer>();
	
	/** At the beginning I have only one state, the initial state of the fork path  */
	private int nodesCounter = 0;
	
	/** Writer used to create the header of the module */
	private Writer headerWriter = new StringWriter();
	
	/** Writer used to create the body of the module */
	private Writer bodyWriter = new StringWriter();
	
	/** A reference to the translator associated to the parent module */
	private ModuleTranslator parentTranslator = null;
	
	/** A boolean variable telling me if we already generated the entry transition for a submodule or not */
	private boolean firstTransition=true;
	
	/** A reference to the parent fork node */
	private ForkNode parentForkNode=null;

	/** The name of the action used to synchronize with the parent module */
	private String syncActionName = null;
	
	/** The set containing the nodes already visited during the translation of this module */
	private Set<ActivityNode> visitedNodes = new HashSet<ActivityNode>();
	
	/** The next node to go after this module */
	private ActivityNode nextnode;
	
	/** fake transition **/
	private double fake=100;
	
	/**
	 * A constructor
	 * @param translator: the root transaltor
	 * @param startNodes: the start nodes of the activity diagram
	 * @param moduleId: the id given to the root module
	 */
	ModuleTranslator(AtopTranslator translator, Set<? extends ActivityNode> startNodes, String moduleId) {
		this.translator = translator;
		this.startNodes = startNodes;
		
		this.moduleName = "ad" + this.translator.getActivity().getName() + "_fork" + moduleId;
		this.stateVarName = "ad" + this.translator.getActivity().getName() + "_fork" + moduleId;
	}
	
	/**
	 * A constructor.
	 * @param translator the root translator.
	 * @param startNodes the starting nodes of this fork path (should be one)
	 * @param moduleId the identifier of this fork path
	 * @param parentTranslator the translator associated to the parent module.
	 * @param parentForkNode the node in the parent module spawning the process.
	 * @param syncActionName the action used to synchronize with the parent module.
	 */
	ModuleTranslator(AtopTranslator translator, Set<? extends ActivityNode> startNodes, 
			String moduleId, ModuleTranslator parentTranslator, ForkNode parentForkNode, String syncActionName) {
		this.translator = translator;
		this.startNodes = startNodes;
		
		this.moduleName = "ad" + this.translator.getActivity().getName() + "_fork" + moduleId;
		this.stateVarName = "ad" + this.translator.getActivity().getName() + "_fork" + moduleId;
		
		this.parentTranslator = parentTranslator;
		this.parentForkNode = parentForkNode;
		this.syncActionName = syncActionName;
	}
	
	/**
	 * Gets a String representation of this module.
	 * @return the representation of this module.
	 */
	String getModule() {
		String moduleRep = headerWriter.toString();
		moduleRep += "\n\n" + bodyWriter.toString();
		moduleRep += "\n\n" + "endmodule" + "\n\n";
		
		return moduleRep;
	}
	
	/**
	 * Gets the set of the identifiers of the nodes translated by this module translator. 
	 * @return the set of identifiers.
	 */
	Set<ActivityNode> getTranslatedNodes() {
		return new HashSet<ActivityNode>(this.visitedNodes);
	}
	
	/**
	 * Gets the state value used to represent a specific node.
	 * @param node the node.
	 * @return the state.
	 */
	Integer getStateNumber(ActivityNode node) {
		return nodeIds.get(node);
	}
	
	/**
	 * Gets the name of the variable used to keep the state of this module.
	 * @return the variable name.
	 */
	String getStateVariable() {
		return stateVarName;
	}
	
	/**
	 * Assign a PRISM state number to the Activity node
	 * and returns the number
	 * @param node the node.
	 * @return the node number.
	 */
	private Integer assignStateNumber(ActivityNode node) {
		Integer stateNumber = nodeIds.get(node);
		if (stateNumber == null) {
			stateNumber = nodesCounter++;
			nodeIds.put(node, stateNumber);
		}
		
		return stateNumber;
	}
	
	/**
	 * Gets the module translator associated to the parent process of this process.
	 * @return the parent module translator if there is one, otherwise null.
	 */
	ModuleTranslator getParentTranslator() {
		return parentTranslator;
	}

	/**
	 * Generates the PRISM model for the Activity.
	 * @throws Exception 
	 * @see ITranslator
	 */
	public void translate() throws Exception{
		// for now only one start node is admitted
		if (startNodes.size() == 0) throw new Exception("No initial node defined for activity");
		if (startNodes.size() > 1) throw new Exception("Multiple initial nodes are not supported");
		
		ActivityNode node = startNodes.iterator().next();
		
		//First I assign the state number to each activity node
		this.assignStateNumberVisit(node);
		//clear the visited set after you are done
		visitedNodes.clear();

		//Then I generate the semantics
		//restart the search
		this.dispatchNodeVisit(node);
		this.createHeader();
	}
	
	/**
	 * Creates the header of the module.
	 * @throws Exception 
	 * @throws TranslationException if the generation of the header fails.
	 */
	private void createHeader() throws Exception {
		try {
			headerWriter.write("module " + moduleName);
			
			// write module main variable
			headerWriter.write("\n\n" + stateVarName + " : [0.." + Integer.toString((nodesCounter - 1)) + "] init 0;");
		} catch (IOException e) {
			throw new Exception("Unable to generate header for PRISM module", e);
		}
	}
	
	private void assignStateNumberVisit(ActivityNode node) throws Exception{
		if (visitedNodes.contains(node)) return;
		else visitedNodes.add(node);
		
		//Assign the state number to this node
		this.assignStateNumber(node);
		
		//one outgoing transition nodes
		if(node instanceof InitialNode || node instanceof Action || node instanceof MergeNode){
			ActivityEdge edge = node.getOutgoing(null);
			ActivityNode target = edge.getTarget();
			assignStateNumberVisit(target);
		}
		//decision node
		if(node instanceof DecisionNode){
			DecisionNode temp=(DecisionNode)node;
			for (ActivityEdge edge : temp.getOutgoings()) {
				assignStateNumberVisit(edge.getTarget());
			}	
		}	
		//fork
		if(node instanceof ForkNode){
			ForkNode temp=(ForkNode)node;
			//Pick up one of the next nodes, there should be a path to the join
			JoinNode join=assignStateNumberVisit_opaque(temp.getOutgoings().iterator().next().getTarget());
			assignStateNumberVisit(join.getOutgoing(null).getTarget());
		}
	}
	
	/**
	 * Visits the nodes after a fork searching for the corresponding join
	 * @param target
	 */
	private JoinNode assignStateNumberVisit_opaque(ActivityNode node) {
		if (visitedNodes.contains(node)) return null;
		else visitedNodes.add(node);
		
		//one outgoing transition nodes
		if(node instanceof InitialNode || node instanceof Action || node instanceof MergeNode){
			ActivityEdge edge = node.getOutgoing(null);
			ActivityNode target = edge.getTarget();
			return assignStateNumberVisit_opaque(target);
		}
		//decision node
		if(node instanceof DecisionNode){
			DecisionNode temp=(DecisionNode)node;
			for (ActivityEdge edge : temp.getOutgoings()) {
				JoinNode join=assignStateNumberVisit_opaque(edge.getTarget());
				if(join!=null) return join;
			}	
		}	
		if(node instanceof JoinNode){
			return (JoinNode)node;
		}
		//Right now let's assume only one depth level of fork
		//if(node instanceof ForkNode)	
		
		return null;
	}

	/**
	 * Dispatches the visit of a node to the specific translation method.
	 * @param node the node to visit.
	 * @throws Exception 
	 * @throws TranslationException if the translation fails.
	 */
	private void dispatchNodeVisit(ActivityNode node) throws Exception{
		if (visitedNodes.contains(node)) return;
		else visitedNodes.add(node);
		
		if (node instanceof InitialNode) visitNode((InitialNode)node);
		if (node instanceof Action) visitNode((Action)node);
		if (node instanceof FinalNode) visitNode((FinalNode)node);
		if (node instanceof DecisionNode) visitNode((DecisionNode)node);
		if (node instanceof MergeNode) visitNode((MergeNode)node);
		if (node instanceof ForkNode) visitNode((ForkNode)node);
		if (node instanceof JoinNode) visitNode((JoinNode)node);
	}
	
	/**
	 * Checks if a node has only a single outgoing transition.
	 * @param node the node to check.
	 * @throws Exception 
	 * @throws TranslationException if the node has multiple outgoing transitions.
	 */
	private void checkSingleOutgoingTransition(ActivityNode node) throws Exception{
		if (node.getOutgoings().size() == 0) throw new Exception( "Action \"" + node.getQualifiedName() + "\" has no outgoing edge.");
		else if (node.getOutgoings().size() > 1) new Exception("Action \"" + node.getQualifiedName() + "\" has multiple outgoing edges.");
	}
	
	
	/**
	 * Visits an initial node.
	 * @param node the node.
	 * @throws Exception 
	 * @throws TranslationException if the translation fails.
	 */
	private void visitNode(InitialNode node) throws Exception{
		// InitNodes should have just one out transition
		checkSingleOutgoingTransition(node);
		
		ActivityEdge edge = node.getOutgoing(null);
		ActivityNode target = edge.getTarget();
		
		this.createGuard(node);
		this.createTransition(target, this.fake, true);
		
		//by construction the assigned state should be zero
		//the id is assigned when the transition is created
		if(this.getStateNumber(node)!=0) throw new Exception("The initial node has a wrong state number");
		
		this.dispatchNodeVisit(target);
	}
	
	/**
	 * Visits an Action node. 
	 * @param node the node 
	 * @throws Exception 
	 * @throws TranslationException if the translation fails.
	 */
	private void visitNode(Action node) throws Exception{
		// ActionNodes should have just one out transition
		checkSingleOutgoingTransition(node);
		
		ActivityEdge edge = node.getOutgoing(null);
		ActivityNode target = edge.getTarget();
		
		ActivityNodeDecorator act_dec=new ActivityNodeDecorator(node);
		Double outProb=act_dec.getSpeed();
		
		this.createGuard(node);
		this.createTransition(target, 1d/outProb, true);
		
		this.dispatchNodeVisit(target);
	}
	
	/**
	 * Visits a decision node.
	 * @param node the node.
	 * @throws Exception 
	 * @throws TranslationException if the translation fails.
	 */
	private void visitNode(DecisionNode node) throws Exception{
		DecisionNodeDecorator dndec=new DecisionNodeDecorator(node);
		
		// create the command
		createGuard(node);
		
		int counter = node.getOutgoings().size();
		int i = 0;
		for (ActivityEdge edge : dndec.getProbabilities().keySet()) {				
			ActivityNode target = edge.getTarget();
			
			if (++i == counter) this.createTransition(target, dndec.getProbabilities().get(edge), true);
			else createTransition(target, dndec.getProbabilities().get(edge), false);
		}
		
		for (ActivityEdge edge : dndec.getProbabilities().keySet()) {
			dispatchNodeVisit(edge.getTarget());
		}
	}
	
	/**
	 * Visits a merge node.
	 * @param node the node.
	 * @throws Exception 
	 * @throws TranslationException if the translation fails.
	 */
	private void visitNode(MergeNode node) throws Exception{
		// MergeNodes should have just one out transition
		checkSingleOutgoingTransition(node);
		
		// reserve state for merge node and get target node
		ActivityEdge outTransition = node.getOutgoing(null); 
		ActivityNode target = outTransition.getTarget();
		
		// create the command
		createGuard(node);
		createTransition(target, this.fake, true);
		
		dispatchNodeVisit(target);
	}
	
	/**
	 * Visits a fork node.
	 * @param node the node.
	 * @throws Exception 
	 * @throws TranslationException if the translation fails.
	 */
	private void visitNode(ForkNode node) throws Exception {
		// spawn new module translators
		String actionName = translator.registerNewAction();
		List<ModuleTranslator> moduleTranslators = new ArrayList<ModuleTranslator>();
		for (ActivityEdge edge : node.getOutgoings()) {
			Set<ActivityNode> startNodes = new HashSet<ActivityNode>();
			startNodes.add(edge.getTarget());
			moduleTranslators.add(this.translator.translateModule(startNodes, this, node, actionName));
		}

		// next node to go
		ActivityNode afterjoin=moduleTranslators.get(0).nextnode;
		
		// create success synchronization transition
		this.openGuard(actionName);
		createGuardCondition("true");
		closeGuard();
		createTransition(afterjoin, 1.0, true);
		
		this.dispatchNodeVisit(afterjoin);
	}
	
	/**
	 * Visits a join node.
	 * @param node the node
	 * @throws IOException 
	 * @throws TranslationException if the translation fails.
	 */
	private void visitNode(JoinNode node) throws IOException {
		if (parentTranslator != null) { // this is a sub process, we should stop the module and sync with parent
			openGuard(this.syncActionName);
			String guard= this.stateVarName + " = " + getStateNumber(node);
			createGuardCondition(guard);
			closeGuard();
			createTransition("0", this.fake, true);
			nextnode=node.getOutgoings().get(0).getTarget();
		}
	}
	
	/**
	 * Visits a final node.
	 * @param node the node.
	 * @throws IOException 
	 * @throws TranslationException if the translation fails
	 */
	private void visitNode(FinalNode node) throws IOException {
		//Nothing to do
	}
	
	/**
	 * Opens a new guard.
	 * @throws IOException 
	 */
	private void openGuard() throws IOException{
		openGuard("");
	}
	
	/**
	 * Opens a new guard synchronized by the specified action.
	 * @param action the name of the synchronization action.
	 * @throws IOException 
	 * @throws TranslationException if the generation fails
	 */
	private void openGuard(String action) throws IOException{
		bodyWriter.write("[" + action + "]");
		Logger.log("[" + action + "]");
	}
	
	/**
	 * Creates a new guard for a transition that may be invoked while the module is the state
	 * corresponding to a specified node. 
	 */
	private void createGuard(ActivityNode node) throws IOException{
		//Check if we need to add the first transition
		boolean addFirstTransitionSemantics=false;
		if (parentTranslator != null && this.firstTransition==true) { 
			firstTransition=false;
			addFirstTransitionSemantics=true;
		}
		
		boolean open=true;
		if(addFirstTransitionSemantics){
			openGuard();
			//The guard has already been opened because of the initial module transition
			open=false;
			createGuardCondition(this.parentTranslator.stateVarName+ " = " + getParentTranslator().getStateNumber(parentForkNode));
			bodyWriter.write(" &");
		}
		
		if (open == true) openGuard();
		
		String guard = " " + stateVarName;
		guard += " = " + getStateNumber(node);
		
		bodyWriter.write(guard);
		Logger.log(guard);
		
		closeGuard();
	}
	
	/**
	 * Creates the first condition of the currently opened guard.
	 * @param condition the condition,
	 * @throws IOException 
	 * @throws TranslationException if the generation fails
	 */
	private void createGuardCondition(String condition) throws IOException{
		bodyWriter.write(" " + condition);
		Logger.log(" " + condition);
	}
	
	/**
	 * Closes a guard.
	 * @throws IOException 
	 * @throws TranslationException if the generation fails
	 */
	private void closeGuard() throws IOException {
		this.bodyWriter.write(" -> ");
		Logger.log(" -> ");
	}
	
	/**
	 * Creates a transition.
	 * @param node the target node.
	 * @param probability the probability of reaching the node.
	 * @param close true if the transition construct should be closed.
	 * @throws IOException 
	 * @throws TranslationException 
	 * @throws TranslationException if the generation fails.
	 */
	private void createTransition(ActivityNode node, Double probability, boolean close) throws IOException{
		createTransition(getStateNumber(node).toString(), probability, close);
	}
	
	/**
	 * Crates a transition.
	 * @param state the target state.
	 * @param probability the probability of reaching the state.
	 * @param close true if the transition construct should be closed.
	 * @throws IOException 
	 * @throws TranslationException 
	 * @throws TranslationException if the generation fails.
	 */
	private void createTransition(String state, Double probability, boolean close) throws IOException {
		createTransition(this.stateVarName, state, probability, close);
	}
	
	/**
	 * Creates a transition.
	 * @param stateVar the PRISM variable holding the state to update. 
	 * @param state the target state.
	 * @param probability the probability of reaching the state.
	 * @param close true if the transition construct should be closed.
	 * @throws IOException 
	 * @throws TranslationException if the generation fails.
	 */
	private void createTransition(String stateVar, String state, Double probability, boolean close) throws IOException{
		String transition = probability.toString() + " : ";
		transition += "(" + stateVar + "'";
		transition += " = " + state + ")";
		if (close == true) transition += ";\n";
		else transition += " + ";
		
		this.bodyWriter.write(transition);
		Logger.log(transition);
	}
	
	public ForkNode getParentForkNode() {
		return parentForkNode;
	}
}