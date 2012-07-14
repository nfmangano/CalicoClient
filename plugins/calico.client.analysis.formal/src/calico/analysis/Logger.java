package calico.analysis;

public class Logger {
	
	static boolean ENABLED=false;
	
	public static void log(String str){
		if(ENABLED){
			System.out.print(str);
		}
	}

}
