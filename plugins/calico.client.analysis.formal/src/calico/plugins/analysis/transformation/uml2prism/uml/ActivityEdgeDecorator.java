package calico.plugins.analysis.transformation.uml2prism.uml;
import org.eclipse.uml2.uml.ActivityEdge;

public class ActivityEdgeDecorator {

	/** The decorated edge */
	private ActivityEdge edge = null;
	
	/**
	 * Default constructor.
	 * @see AbstractDecorator::AbstractDecorator
	 */
	public ActivityEdgeDecorator(ActivityEdge edge) {
		this.edge = edge;
	}
	
	/**
	 * Gets the probability of taking this edge.
	 * @return the probability.
	 */
	public double getProbability() {
		return this.parseProbabilityComment();
	}

	private Double parseProbabilityComment(){
		String s=edge.getOwnedComments().get(0).getBody();
		Double probability= Double.parseDouble(s.substring(s.indexOf(":")+1));
		return probability;
	}
}
