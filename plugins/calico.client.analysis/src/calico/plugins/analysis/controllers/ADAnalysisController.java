package calico.plugins.analysis.controllers;

import java.text.DecimalFormat;
import java.util.Iterator;

import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OpaqueAction;

import calico.CalicoDraw;
import calico.analysis.uml.ModelDecorator;
import calico.components.CGroup;
import calico.controllers.CCanvasController;
import calico.controllers.CConnectorController;
import calico.controllers.CGroupController;
import calico.plugins.analysis.components.AnalysisComponent;
import calico.plugins.analysis.components.activitydiagram.ActivityNode;
import calico.plugins.analysis.uml.translators.calico2uml.Calico2UML;

/**
 * The Analysis Menu Controller is in managing the analysis actions
 * 
 * @author motta
 * 
 */
public class ADAnalysisController {

	public static double CURRDISTANCE=500.0;
	
	public static void runAnalysis(long target_uuid, double distance) {
		ADAnalysisController.CURRDISTANCE=distance;
		Calico2UML translator=new Calico2UML();
		Model model=translator.translate();
		
		//save for debugging
		ModelDecorator modeldec=new ModelDecorator(model);
		modeldec.saveModel("out_model.uml");
		
		//solve it!
		org.eclipse.uml2.uml.ActivityNode node=translator.getTraceabilityMap().get(target_uuid);
        new calico.analysis.Main().solve(model, node, distance);
        
        //Annotate the results in calico
        annotateResults(translator, target_uuid);
        
		//save for debugging
		modeldec=new ModelDecorator(model);
		modeldec.saveModel("out_model.uml");
	}
	
	private static void annotateResults(Calico2UML translator, long target_uuid){
		
		//annotate the others
		for(long uuid: translator.getTraceabilityMap().keySet()){
			CGroup c=CGroupController.groupdb.get(uuid);
			if(getResult(translator, uuid)!=null)
				c.setTransparency(Math.min(getResult(translator, uuid).floatValue()+(float)0.05, 1.0f));
			CalicoDraw.invalidatePaint(c);
        }
	}
	
	private static Double getResult(Calico2UML translator, long uuid){
    	Double r=null;
		org.eclipse.uml2.uml.ActivityNode temp=translator.getTraceabilityMap().get(uuid);
    	
    	if(!(temp instanceof OpaqueAction)) return null;
    	
    	Iterator<Comment> it=temp.getOwnedComments().iterator();
    	while(it.hasNext()){
    		String body=it.next().getBody();
    		if(body.indexOf("Result")!=-1){
    			DecimalFormat twoDForm = new DecimalFormat("#.##");
    			r=Double.parseDouble(twoDForm.format(Double.parseDouble(body.substring(body.indexOf(":")+1))));
    		}
    	}
    	return r;
	}
	
	

}
