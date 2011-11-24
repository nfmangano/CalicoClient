package calico.components;

import calico.CalicoDataStore;

public class CSession
{
	private String name = "";
	private String host = "";
	private int port = 27000;
	
	public CSession(String name, String host, int port) {
		this.name = name;
		this.host = host;
		this.port = port;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}
	
	public String toString() {
		String strRep = this.name+" ("+this.host+":"+this.port+")";
		if(this.host.equals(CalicoDataStore.ServerHost) && this.port == CalicoDataStore.ServerPort) {
			strRep += " [Current]";
		}
		return strRep;
	}
	
	
}
