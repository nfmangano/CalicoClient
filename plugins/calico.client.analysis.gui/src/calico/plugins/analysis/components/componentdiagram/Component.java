package calico.plugins.analysis.components.componentdiagram;

import java.util.HashSet;

import calico.components.CGroup;
import calico.plugins.analysis.components.AnalysisComponent;
import calico.plugins.analysis.components.activitydiagram.ActivityNode;

public class Component extends CGroup implements AnalysisComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	HashSet<Long> activities_uuid=new HashSet<Long>();
	double responseTime;

	public Component(long uuid, long cuid, long puid) {
		super(uuid, cuid, puid);
	}
	
	public double getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(double responseTime) {
		this.responseTime = responseTime;
	}
	
	public void addActivity(long uuid){
		this.activities_uuid.add(uuid);
	}
	
	public HashSet<Long> getActivities(){
		return this.activities_uuid;
	}
	
	public String toString(){
		String s="";
		s=s+"Component: "+activities_uuid+"\n";
		return s;
	}

}
