
/**
 * An instance of Client is a simple datasack that has a unique netID, a name,
 * and sometimes a status stored locally (otherwise handled by the team that
 * manages this Client object).
 * 
 * @author Alan Rosenberg
 */
public class Client {
	
	private int netID;
	private String name;
	private int status;		// only used in case client and status need to be moved together
	
	public Client() {
		netID = 0;
		name = "Nikolas Alan Koelzer";
	}
	
	public Client(int netID, String name, int status) {
		this.netID = netID;
		this.name = name;
		this.status = status;
	}

	public int getNetID() {
		return netID;
	}

	public void setNetID(int netID) {
		this.netID = netID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
