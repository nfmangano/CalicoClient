package calico.networking.netstuff;

/**
 * This holds all the byte values for all the network commands
 *
 * @author Mitch Dempsey
 */
public class NetCommandFormat
{
	private String name = "";
	private String fmt = "";
	
	public NetCommandFormat(String n, String f)
	{
		name = n;
		fmt = f;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getFormat()
	{
		return fmt;
	}



}
