package calico.plugins.analysis.transformation.uml2prism.uml;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.Artifact;
import org.eclipse.uml2.uml.Device;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Node;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

public class ModelDecorator {

	/** The UML element for which statistics will be computed */
	private Model model = null;
	
	/** The set of activities defined in the model */
	private Set<Activity> activities = null;
	
	/**
	 * Default constructor.
	 * @see AbstractDecorator::AbstractDecorator
	 */
	public ModelDecorator(Model model) {
		this.model = model;
	}
	
	/**
	 * Gets all the activities defined in the model. 
	 * @return a copy of the set of all activities.
	 */
	public Set<Activity> getAllActivities() {
		if (activities == null) {
			activities = new HashSet<Activity>();
			for(Element descendant : model.allOwnedElements()) {
				if (descendant instanceof Activity) activities.add((Activity) descendant);
			}
		}
		return new HashSet<Activity>(activities);
	}
	
	public void saveModel(String file){
        ResourceSet resourceSet = new ResourceSetImpl();
        UML300ResourcesUtil.init(resourceSet);
        Resource resource = resourceSet.createResource(URI.createFileURI(file));
        resource.getContents().add(model);
        try {
			resource.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Given an XMI file returns the UML model associated 
	 * with it
	 * @param file
	 * @return
	 */
	public static Model getModel(String file){
		URI typesUri = null;
		typesUri = URI.createFileURI(file);
		ResourceSet set = new ResourceSetImpl();
	
		set.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		set.createResource(typesUri);
		Resource r = set.getResource(typesUri, true);
		
		List<EObject> contents = r.getContents();
		for (EObject obj : contents) {
			if (obj instanceof Model) {
				return (Model) obj;
			}
		}
		return null;
	}
	
}
