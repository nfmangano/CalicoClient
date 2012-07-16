package calico.plugins.analysis.transformation.uml2prism.uml;

import org.eclipse.uml2.uml.ActivityNode;

public class ActivityNodeDecorator {

	private ActivityNode node;
	
	public ActivityNodeDecorator(ActivityNode node){
		this.node=node;
	}
	
	public Double getSpeed(){
		return this.parseSpeedComment();
	}

	private Double parseSpeedComment() {
		String s=node.getOwnedComments().iterator().next().getBody();
		Double speed= Double.parseDouble(s.substring(s.indexOf(":")+1));
		return speed;
	}
	
}
