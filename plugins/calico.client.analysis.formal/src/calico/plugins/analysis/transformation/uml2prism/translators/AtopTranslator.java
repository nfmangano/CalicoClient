package calico.plugins.analysis.transformation.uml2prism.translators;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Element;

import calico.plugins.analysis.transformation.uml2prism.namemanagers.ActionsNameManager;
import calico.plugins.analysis.transformation.uml2prism.namemanagers.ModuleNameManager;
import calico.plugins.analysis.transformation.uml2prism.prism.PrismState;
import calico.plugins.analysis.transformation.uml2prism.uml.ActivityDecorator;
import calico.plugins.analysis.transformation.uml2prism.uml.ModelDecorator;


/**
 * A translator from activity graphs to PRISM models.
 * @author Alfredo Motta
 */
public class AtopTranslator{
		
	/** The UML model */
	private Model model = null;
	
	/** The decorator of the UML model */
	private ModelDecorator modelDec = null;
	
	/** The activity to translate */
	private Activity activity = null;
	
	/** The decorator of the activity */
	private ActivityDecorator activityDec = null;
	
	/** The stream to output the prism model */
	private OutputStream prismModelStream = null;
	
	/** The writer used to write the model */
	private PrintWriter modelWriter = null;
	
	/** The list of module translators used to generate PRISM modules */
	private List<ModuleTranslator> moduleTranslators = new ArrayList<ModuleTranslator>();	

	
	public AtopTranslator(Model m, OutputStream prismModelStream) {
		this.model=m;
		this.prismModelStream = prismModelStream;
	}
	
	/**
	 * Initializes the translator.
	 * @throws TranslationException if the initialization fails
	 */
	private void initialize() {		
		// create the decorator for the UML model
		this.modelDec = new ModelDecorator(this.model);
		
		// find the activity and create the decorator
		for (Activity act : modelDec.getAllActivities()) {
			activity = act;
			activityDec=new ActivityDecorator(activity);
		}
	}	
	
	/**
	 * Gets the activity to translate.
	 * @return the activity.
	 */
	Activity getActivity() {
		return activity;
	}
	
	/**
	 * Generates both the PRISM model representing the activity and the properties that should be checked
	 * against the model.
	 * @throws Exception 
	 * @see ITranslator
	 */
	public void translate() throws Exception{
		initialize();
		
		translateModel(prismModelStream);
	}
	
	/**
	 * Translates the graph into a PRISM model.
	 * @param stream the output stream that will be used for the model.
	 * @throws Exception 
	 * @throws TranslationException if the translation fails.
	 */
	private void translateModel(OutputStream stream) throws Exception{
		modelWriter = new PrintWriter(stream);
		
		//This is a ctmc prism model
		modelWriter.write("ctmc\n\n");
			
		//translate the main module e recursively accumulate
		//the results that I will find in moduleTranslators
		translateModule(activityDec.getInitialNodes()); 
		
		//collect the translation of all the modules
		for (ModuleTranslator t : moduleTranslators) {
			modelWriter.write(t.getModule());
		}
		
		modelWriter.close();
	}
	
	/**
	 * Translates a main module (i.e., a module whose parent is the root PRISM model).
	 * @param startNodes the nodes from which the module will start its execution.
	 * @return the translator used to generate the module.
	 * @throws Exception 
	 * @throws TranslationException if the translation fails.
	 */
	ModuleTranslator translateModule(Set<InitialNode> startNodes) throws Exception {
		//The first module has id equal to zero
		ModuleTranslator translator = new ModuleTranslator(this, startNodes, ModuleNameManager.generateName());
		this.moduleTranslators.add(translator);
		translator.translate();
		return translator;
	}
	
	/**
	 * Translates a submodule (i.e., a module whose parent is another module).
	 * @param startNodes the nodes from which the module will start its execution.
	 * @param parentTranslator the parent module.
	 * @param parentForkNode the node in the parent module spawning the process.
	 * @param actionId the name of the action used to synchronize the join between the parent module and this module.
	 * @return the translator used to generate the module.
	 * @throws Exception 
	 * @throws TranslationException if the translation fails. 
	 */
	ModuleTranslator translateModule(Set<? extends ActivityNode> startNodes, ModuleTranslator parentTranslator, 
			ForkNode parentForkNode, String actionId) throws Exception {
		
		ModuleTranslator translator = new ModuleTranslator(this, startNodes, ModuleNameManager.generateName(), parentTranslator, parentForkNode, actionId);
		this.moduleTranslators.add(translator);
		translator.translate();
		
		return translator;
	}
	
	/**
	 * Register a new global action.
	 * @return the name of the registered action.
	 */
	String registerNewAction() {
		return "a_" + ActionsNameManager.generateName();
	}
	
	/**
	 * After the translation has been done this metod gets the 
	 * Prism state associated to a certain activity node
	 * @param node
	 * @return
	 */
	public PrismState getPrismState(ActivityNode node){
		PrismState r=null;
		for(ModuleTranslator m: moduleTranslators){
			if(m.getStateNumber(node)!=null){
				r=new PrismState(m.getStateVariable(), m.getStateNumber(node));
				if(m.getParentForkNode()!= null) r.setParentForkNode(this.getPrismState(m.getParentForkNode()));
			}
		}
		if(r==null){
			try {
				throw new Exception("Prism state not found for this activity node");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return r;
	}
	
}
