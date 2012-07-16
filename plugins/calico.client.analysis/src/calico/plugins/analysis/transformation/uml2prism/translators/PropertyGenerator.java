package calico.plugins.analysis.transformation.uml2prism.translators;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.Node;

import calico.plugins.analysis.transformation.uml2prism.Logger;
import calico.plugins.analysis.transformation.uml2prism.prism.PrismState;
import calico.plugins.analysis.transformation.uml2prism.uml.ActivityDecorator;

/** After the Prism model has been built generates
 * the properties of interest
 * @author motta
 *
 */
public class PropertyGenerator {

	/** The translator that has generated the prism model */
	private AtopTranslator translator;
	
	public PropertyGenerator(AtopTranslator translator){
		this.translator=translator;
	}
	
	public Map<ActivityNode, String> getPropertyList(ActivityNode node, double distance){
		PrismState s=translator.getPrismState(node);
		Map<ActivityNode,String> props=new HashMap<ActivityNode,String>();
		
		ActivityDecorator actdec=new ActivityDecorator(translator.getActivity());
		for(ActivityNode act_node: actdec.getNodes()){
			if(!act_node.equals(node)){
				PrismState s_target=translator.getPrismState(act_node);
				String s_target_str=s_target.getStateVarName()+"="+s_target.getStateNumber();
				if(s_target.getParentForkNode()!=null) s_target_str=s_target_str+"&"+ s_target.getParentForkNode().getStateVarName()+"="+s_target.getParentForkNode().getStateNumber();
				
				String curr_state=s.getStateVarName()+"="+s.getStateNumber();
				if(s.getParentForkNode()!=null) curr_state=curr_state+"&"+ s.getParentForkNode().getStateVarName()+"="+s.getParentForkNode().getStateNumber();
				
				String prop="filter(avg, P=? [ F<="+Double.toString(distance)+" "+s_target_str+" ], "+curr_state+")";
				props.put(act_node, prop);
			}
		}
		return props;
	}
	
}
