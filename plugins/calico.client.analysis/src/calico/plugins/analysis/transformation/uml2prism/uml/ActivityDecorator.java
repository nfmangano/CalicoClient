package calico.plugins.analysis.transformation.uml2prism.uml;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.Stereotype;

/**
 * A decorator for an UML activity.
 * @author Mauro Luigi Drago
 *
 */
public class ActivityDecorator{
	
	/** The decorated activity */
	private Activity activity = null;
	
	/** The set of actions defined inside this activity */
	private Set<Action> actions = null;
	
	/** All the nodes defined inside this activity */
	private Set<ActivityNode> nodes = null;
	
	/**
	 * Default constructor.
	 * @see AbstractDecorator::AbstractDecorator
	 */
	public ActivityDecorator(Activity activity) {
		this.activity = activity;
	}
	
	/**
	 * Gets all the actions defined inside this activity.
	 * @return a copy of the set of all actions.
	 */
	public synchronized Set<Action> getActions() {
		if (actions == null) {
			actions = new HashSet<Action>();
			for (Element descendant : activity.allOwnedElements()) {
				if (descendant instanceof Action) actions.add((Action) descendant);
			}
		}
		return new HashSet<Action>(actions);
	}
	
	/**
	 * Gets all the activity nodes defined inside this activity.
	 * @return a copy of the set of all nodes.
	 */
	public synchronized Set<ActivityNode> getNodes() {
		if (nodes == null) {
			nodes = new HashSet<ActivityNode>();
			for (Element descendant : activity.allOwnedElements()) {
				if (descendant instanceof ActivityNode) nodes.add((ActivityNode) descendant);
			}
		}
		return new HashSet<ActivityNode>(nodes);
	}
	
	
	/**
	 * Gets the set of initial nodes defined for this activity.
	 * @return a copy of the set initial nodes.
	 */
	public Set<InitialNode> getInitialNodes() {
		Set<InitialNode> nodes = new HashSet<InitialNode>();
		for (Element child : activity.getOwnedElements()) {
			if (child instanceof InitialNode) nodes.add((InitialNode) child);
		}
		return nodes;
	}
	
}