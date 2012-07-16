package calico.plugins.analysis.transformation.uml2prism.prism;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Model;

/** 
 * This class is in charge of invoking prism and collect the 
 * results from the verification
 * @author motta
 *
 */
public class Solver {
	
	public String MODEL_FILE="out_prism_model.sm";
	public String PROPERTY_FILE="out_prism_properties.props";
	public String RESULT_FILE="out_prism_results.txt";
	
	ArrayList<Result> prism_results=new ArrayList<Result>();
	
	//the one that I actually use
	private BufferedReader results_reader=null;
	//the smoke in the eye
	private File results=null;
	private FileReader file_results_reader=null;
	

	/**
	 * Takes the model and the properties and verify them using prism
	 * Finally annotates the UML model with the results
	 * @param prism_model
	 * @param prism_properties
	 */
	public void solve(ByteArrayOutputStream prism_model, Map<ActivityNode, String> prism_properties){
		writeToOutputFile(MODEL_FILE, prism_model);
		writeToOutputFile(PROPERTY_FILE, convertPropertyMapToStream(prism_properties));
	
		//ISSUE THE COMMAND
		String command="prism "+MODEL_FILE+" "+PROPERTY_FILE+" -exportresults "+ RESULT_FILE + " -fixdl";
		try {
			//Process ls_proc = Runtime.getRuntime().exec(command);
			SysCommandExecutor cmdExecutor = new SysCommandExecutor(); 		
			cmdExecutor.runCommand(command);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		openResultsFile();
		//collect the results in the internal array list
		collectResults(prism_properties);
		//create the comments
		createComments(prism_properties);
		closeResultsFile();
	}
	
	private void createComments(Map<ActivityNode,String> prism_properties){
		for(ActivityNode act_node: prism_properties.keySet()){
			for(Result r: this.prism_results){
				if(prism_properties.get(act_node).equals(r.getProperty())){
					Comment c=act_node.createOwnedComment();
					c.setBody("Result: "+Double.toString(r.getResult()));
				}
			}
		}
	}
	
	private void closeResultsFile() {
		try {			
			results_reader.close();
			file_results_reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		results=null;		
	}

	private void collectResults(Map<ActivityNode, String> prism_properties){
		for(int i=0; i< prism_properties.size(); i++){
			getNextResult();
		}
	}
	
	private void getNextResult(){
		Result curr_prism_result=new Result();
		try {
			
			
			//Possible white lines before the property
			String w=results_reader.readLine();
			if(w.equals("")) while(w.equals("")) w=results_reader.readLine();
			//Toglio i ":" finali
			curr_prism_result.setProperty(w.substring(0, w.length()-1));
			
			//"Result"
			results_reader.readLine();
			//The number
			curr_prism_result.setResult(Double.parseDouble(results_reader.readLine()));
			this.prism_results.add(curr_prism_result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void openResultsFile(){
		//If it is the first time, just open the file
		if(results==null){
			results=new File(RESULT_FILE);
			try {
				file_results_reader=new FileReader(results);
				results_reader=new BufferedReader(file_results_reader);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private ByteArrayOutputStream convertPropertyMapToStream(Map<ActivityNode,String> in){
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		PrintWriter writer=new PrintWriter(out);
		for(ActivityNode key: in.keySet()) writer.write(in.get(key)+"\n");		
		writer.close();
		return out;
	}
	
	private void writeToOutputFile(String file, ByteArrayOutputStream out){
		try {
			FileWriter writer=new FileWriter(file);
			String to_write="";
			to_write=out.toString("UTF-8");
			writer.write(to_write+"\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
