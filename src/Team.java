import java.util.LinkedList;

/**
 * A Team instance will maintain details regarding a team; namely the status vector of the team.
 * Teams also have a unique ID, and a list of TeamClient objects (along with their statuses).
 * 
 * @author Alan Rosenberg
 */
public class Team {
	
	private int teamID;
	private LinkedList<Client> clients;
	private LinkedList<Integer> stati;			//statuses? status? stati?
	
	/*
	 * Default Constructor
	 */
	public Team() {
		teamID = 0;
		clients = new LinkedList<>();
		stati = new LinkedList<>();
	}
	
	public Team(int teamID) {
		this.teamID = teamID;
		clients = new LinkedList<>();
		stati = new LinkedList<>();
		
	}
	
	/*
	 * Parameterized
	 */
	public Team(int ID, LinkedList<Client> clients, LinkedList<Integer> stati) {
		this.teamID = ID;
		this.clients = clients;
		this.stati = stati;
	}
	
	/**
	 * addClient will perform the task of adding new clients to this Team
	 * 
	 * @param cID TeamClient object to be added
	 */
	public void addClient(Client c) {
		
		clients.add(c);
		stati.add(c.getStatus());
	}
	
	/**
	 * removeClient will perform the task of removing clients from this Team
	 * 
	 * @param netID TeamClient object to be removed
	 */
	public Client removeClient(int netID) {
		
		Client c = null;
		int s = 0;
		
		// looping through all clients
		for(int i = 0 ; i < clients.size() ; i++) {
			
			// when we find a matching client...
			if(clients.get(i).getNetID() == netID) {
				c = clients.remove(i);		// remove it from client vector
				s = stati.remove(i);		// remove it from status vector
			}
		}
		
		c.setStatus(s);
		return c;
	}
	
	/**
	 * This method will determine whether a client exists within this team
	 * @param netID
	 * @return
	 */
	public boolean containsClient(int netID) {
		
		for(int i = 0 ; i < clients.size() ; i++) {
			
			if(clients.get(i).getNetID() == netID) {
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * This method will modify the status vector. The status value in the same index as the
	 * given client will be updated with the given status.
	 * @param c
	 * @param s
	 */
	public void setClientStatus(int c, int s) {
		
		// looping through all clients
		for(int i = 0 ; i < clients.size() ; i++) {
			
			// when we find a match...
			if(clients.get(i).getNetID() == c) {
				
				// set the corresponding status
				stati.set(i, s);
			}
		}
	}

	public int getTeamID() {
		return teamID;
	}

	public void setTeamID(int teamID) {
		this.teamID = teamID;
	}

	public LinkedList<Client> getClients() {
		return clients;
	}

	public void setClients(LinkedList<Client> clients) {
		this.clients = clients;
	}

	/**
	 * Effectively a toString method for a Team
	 * @return formatted status vector
	 */
	public String getStati() {
		
		StringBuffer s = new StringBuffer();
		
		s.append("Status of Team ");
		s.append(teamID);
		s.append(": ");
		
		for(int i = 0 ; i < clients.size() ; i++) {
			
			s.append(clients.get(i).getName());
			s.append(" : ");
			s.append(stati.get(i));
		}
		
		return s.toString();
	}

	public void setStati(LinkedList<Integer> stati) {
		this.stati = stati;
	}
}
