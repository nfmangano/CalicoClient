package calico.analysis.namemanagers;

public class ActionsNameManager {

	static int i=0;
	
	public static String generateName() {
		String s = Integer.toString(i++);
		return s;
	}

}
