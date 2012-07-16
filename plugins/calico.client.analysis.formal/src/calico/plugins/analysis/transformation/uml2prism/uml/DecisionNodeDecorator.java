package calico.plugins.analysis.transformation.uml2prism.uml;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.DecisionNode;

public class DecisionNodeDecorator {
	
	/** the decorated node */
	private DecisionNode node;
	
	/** the probabilities associated to this decision */
	private Map<ActivityEdge, Double> probabilities=new HashMap<ActivityEdge,Double>();

	public DecisionNodeDecorator(DecisionNode node){
		this.node=node;
	}
	
	public Map<ActivityEdge, Double> getProbabilities(){
		if(!probabilities.isEmpty()) return probabilities;
		
		// compute transition probabilities
		Map<ActivityEdge, Double> probabilities = new HashMap<ActivityEdge, Double>();
		
		// compute the normalization factor for probabilities not summing up to one
		double normalizationFactor = 0d;
		for (ActivityEdge edge : node.getOutgoings()) {
			ActivityEdgeDecorator edgeDec = new ActivityEdgeDecorator(edge);
			normalizationFactor += edgeDec.getProbability();
			probabilities.put(edge, edgeDec.getProbability());
		}
		
		// normalize probabilities
		for (ActivityEdge edge : probabilities.keySet()) {
			probabilities.put(edge, probabilities.get(edge) / normalizationFactor);
		}
		
		return probabilities;
	}

}
