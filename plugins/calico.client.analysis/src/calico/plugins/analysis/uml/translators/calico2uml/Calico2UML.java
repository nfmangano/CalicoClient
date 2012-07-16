package calico.plugins.analysis.uml.translators.calico2uml;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityFinalNode;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.ControlFlow;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.JoinNode;
import org.eclipse.uml2.uml.MergeNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.InitialNode;

import calico.analysis.Main;
import calico.analysis.translators.AtopTranslator;
import calico.components.CGroup;
import calico.controllers.CCanvasController;
import calico.controllers.CGroupController;
import calico.plugins.analysis.components.AnalysisComponent;
import calico.plugins.analysis.components.activitydiagram.ActivityDiagram;
import calico.plugins.analysis.components.activitydiagram.FinalNode;

public class Calico2UML {

	/** records the mapping between the id of the calico groups and the uml entities */
	private Long2ReferenceOpenHashMap<ActivityNode> traceabilitymap = new Long2ReferenceOpenHashMap<ActivityNode>();
	
	/** the Eclipse UML2 model that I am going to build */
	private Model model;
	
	/** the Eclipse UML2 activity that I am going to build */
	private Activity activity;
	
	/** Used for the exploration of the calico uml activity */
    private HashSet<Long> visited=new HashSet<Long>();
	
	public Model translate(){
		//Create the model
        model = UMLFactory.eINSTANCE.createModel();
        model.setName("Calico model");

        //Create the activity
        activity=UMLFactory.eINSTANCE.createActivity();
        model.getPackagedElements().add(activity);
        
        //Get the initial node
        calico.plugins.analysis.components.activitydiagram.InitialNode initialNode=ActivityDiagram.getInitialNode();
        //Go visit it, this should add all the nodes starting from it
        visit(initialNode);
        
        //Add the connectors
        createControlFlows();

        return model;
	}
	
	public Long2ReferenceOpenHashMap<ActivityNode> getTraceabilityMap(){
		return traceabilitymap;
	}
	
	public Model getModel(){
		return model;
	}
	
	private void createControlFlows() {
		for(calico.plugins.analysis.components.activitydiagram.ControlFlow cf: ActivityDiagram.getControlFlows()){
			this.addControlFlow(cf);
		}
	}

	private void visit(AnalysisComponent node) {
		//If you are not know I am done with this path
		if(visited.contains(node.getUUID())) return;
		
		//Otherwise let-s check who you are
		if(node instanceof calico.plugins.analysis.components.activitydiagram.InitialNode){
			calico.plugins.analysis.components.activitydiagram.InitialNode temp=(calico.plugins.analysis.components.activitydiagram.InitialNode) node;
			visited.add(temp.getUUID());
			this.addInitialNode(temp);
			this.visit(temp.getOutgoingConnector().getOutgoingNode());
		}
		if(node instanceof calico.plugins.analysis.components.activitydiagram.ActivityNode){
			calico.plugins.analysis.components.activitydiagram.ActivityNode temp= (calico.plugins.analysis.components.activitydiagram.ActivityNode) node;
			visited.add(temp.getUUID());
			this.addActivityNode(temp);
			this.visit(temp.getOutgoingConnector().getOutgoingNode());
		}
		if(node instanceof calico.plugins.analysis.components.activitydiagram.FinalNode){
			calico.plugins.analysis.components.activitydiagram.FinalNode temp= (calico.plugins.analysis.components.activitydiagram.FinalNode) node;
			visited.add(temp.getUUID());
			this.addFinalNode(temp);
			//no further visit is needed
		}
		if(node instanceof calico.plugins.analysis.components.activitydiagram.DecisionNode){
			calico.plugins.analysis.components.activitydiagram.DecisionNode temp= (calico.plugins.analysis.components.activitydiagram.DecisionNode) node;
			visited.add(temp.getUUID());
			this.addDecisionNode(temp);
			for(calico.plugins.analysis.components.activitydiagram.ControlFlow cf: temp.getOutgoingPaths()) this.visit(cf.getOutgoingNode());
		}
		if(node instanceof calico.plugins.analysis.components.activitydiagram.ForkNode){
			calico.plugins.analysis.components.activitydiagram.ForkNode temp= (calico.plugins.analysis.components.activitydiagram.ForkNode) node;
			if(temp.getIncomingPaths().size()==1 && temp.getOutgoingPaths().size()>1){
				//this is actually a fork
				visited.add(temp.getUUID());
				this.addForkNode(temp);
				for(calico.plugins.analysis.components.activitydiagram.ControlFlow cf: temp.getOutgoingPaths()) this.visit(cf.getOutgoingNode());
			}
			else if(temp.getIncomingPaths().size()>1 && temp.getOutgoingPaths().size()==1){
				//this is a join
				visited.add(temp.getUUID());
				this.addJoinNode(temp);
				this.visit(temp.getOutgoingPaths().iterator().next().getOutgoingNode());
			}
			else{
				//this is an error
				try {
					throw new Exception("Malformed fork/join in the calico model");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void addForkNode(calico.plugins.analysis.components.activitydiagram.ForkNode forkNode) {
		ForkNode newdec=UMLFactory.eINSTANCE.createForkNode();
		activity.getNodes().add(newdec);
		traceabilitymap.put(forkNode.getUUID(), newdec);
	}
	
	private void addJoinNode(calico.plugins.analysis.components.activitydiagram.ForkNode joinNode) {
		JoinNode newdec=UMLFactory.eINSTANCE.createJoinNode();
		activity.getNodes().add(newdec);
		traceabilitymap.put(joinNode.getUUID(), newdec);
	}

	private void addFinalNode(calico.plugins.analysis.components.activitydiagram.FinalNode finalNode) {
		ActivityFinalNode newfinal=UMLFactory.eINSTANCE.createActivityFinalNode();
		activity.getNodes().add(newfinal);
		traceabilitymap.put(finalNode.getUUID(), newfinal);
	}

	private void addInitialNode(calico.plugins.analysis.components.activitydiagram.InitialNode initialNode){
		InitialNode initialNode_=UMLFactory.eINSTANCE.createInitialNode();
		activity.getNodes().add(initialNode_);
		traceabilitymap.put(initialNode.getUUID(), initialNode_);
	}
	
	private void addActivityNode(calico.plugins.analysis.components.activitydiagram.ActivityNode activityNode){
		Action newaction=UMLFactory.eINSTANCE.createOpaqueAction();
		
		Comment c=newaction.createOwnedComment();
		c.setBody("Speed: "+Double.toString(activityNode.getResponseTime()));
		
		activity.getNodes().add(newaction);
		traceabilitymap.put(activityNode.getUUID(), newaction);
	}
	
	private void addDecisionNode(calico.plugins.analysis.components.activitydiagram.DecisionNode decisionNode){
		if(decisionNode.getOutgoingPaths().size()>1){
			DecisionNode newdec=UMLFactory.eINSTANCE.createDecisionNode();
			activity.getNodes().add(newdec);
			traceabilitymap.put(decisionNode.getUUID(), newdec);
		}else{
			MergeNode newmerge=UMLFactory.eINSTANCE.createMergeNode();
			activity.getNodes().add(newmerge);
			traceabilitymap.put(decisionNode.getUUID(), newmerge);
		}
	}	
	
	private void addControlFlow(calico.plugins.analysis.components.activitydiagram.ControlFlow controlFlow){
		ControlFlow newcf=UMLFactory.eINSTANCE.createControlFlow();
		
		if(controlFlow.getIncomingNode() instanceof calico.plugins.analysis.components.activitydiagram.DecisionNode){
			calico.plugins.analysis.components.activitydiagram.DecisionNode  d=(calico.plugins.analysis.components.activitydiagram.DecisionNode) controlFlow.getIncomingNode();
			//non si tratta di un merge
			if(d.getOutgoingPaths().size()>1){
				Comment c=newcf.createOwnedComment();
				c.setBody("Probability: "+Double.toString(controlFlow.getProbability()));
			}
		}
		
		newcf.setSource(traceabilitymap.get(controlFlow.getIncomingNode().getUUID()));
		newcf.setTarget(traceabilitymap.get(controlFlow.getOutgoingNode().getUUID()));
		activity.getEdges().add(newcf);
	}
	
	public static void main(String[] args) throws IOException{
		new Calico2UML().translate();
	}
	
}
