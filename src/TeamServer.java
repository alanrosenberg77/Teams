import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Instances of TeamServer will be the coordinators of teams: it will define and
 * manage teams with status vectors. It will also define what statuses are
 * possible for any given team. Instances add clients to teams, and remove
 * clients from teams. They will receive status updates from clients and update
 * the team's vector to reflect.
 * 
 * @author Alan Rosenberg
 */
public class TeamServer {

	/**
	 * This method will handle the task of sending a packet
	 * 
	 * @param message
	 * @param destAddr destination address
	 * @param destPort destination port
	 * @return DatagramSocket socket
	 * @throws Exception
	 */
	private static DatagramPacket receiveRequest(DatagramSocket socket) throws Exception {

		// will store incoming packet
		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

		// pause until packet received
		socket.receive(packet);
		System.out.println("\nMessage received...");

		System.out.println(getMessage(packet));

		return packet;
	}

	/**
	 * This method will handle UPDATE requests from clients. Parameters in the
	 * request are used to determine the client ID and new status. The method finds
	 * the client's team, then tells that team to update it status vector with the
	 * given status. Server sends back an ACK once this operation is complete.
	 * 
	 * @param updatePkt
	 * @param socket
	 * @throws Exception
	 */
	private static void handleUpdate(DatagramPacket updatePkt) throws Exception {

		// getting client details
		InetAddress srcAddr = updatePkt.getAddress();
		int srcPort = updatePkt.getPort();

		// grabbing that sweet sweet data
		String updateMsg = getMessage(updatePkt);
		String[] parts = updateMsg.split(" ");
		int netID = Integer.parseInt(parts[1]);
		int status = Integer.parseInt(parts[2]);

		// preventing invalid netIDs
		if (netID < 1 || netID > clientIDs) {

			handleInvalidInput(updatePkt);
			return;
		}

		// looping through all teams
		for (int i = 0; i < teams.size(); i++) {

			if (teams.get(i).containsClient(netID)) {
				teams.get(i).setClientStatus(netID, status);
			}
		}

		// making message
		String msg = "ACK UPDATE";
		byte[] bytes = msg.getBytes();

		// acknowledging client
		DatagramPacket ack = new DatagramPacket(bytes, bytes.length, srcAddr, srcPort);
		socket.send(ack);
		System.out.println("Reply sent");
	}

	/**
	 * This method handles GET requests from clients. The parameter of the request
	 * will determine which client is asking for stati. The server will determine
	 * which team this client belongs to, then return the status vector of that
	 * team.
	 * 
	 * @param getPkt
	 * @param teams
	 * @param socket
	 * @throws Exception
	 */
	private static void handleGet(DatagramPacket getPkt) throws Exception {

		// getting client details
		InetAddress srcAddr = getPkt.getAddress();
		int srcPort = getPkt.getPort();

		// grabbing that sweet sweet data
		String updateMsg = getMessage(getPkt);
		String[] parts = updateMsg.split(" ");
		int netID = Integer.parseInt(parts[1]);

		// preventing invalid netIDs
		if (netID < 1 || netID > clientIDs) {

			handleInvalidInput(getPkt);
			return;
		}

		String status = null;

		for (int i = 0; i < teams.size(); i++) {

			Team t = teams.get(i);

			if (t.containsClient(netID)) {

				status = t.getStati();
			}
		}

		// making message
		byte[] bytes = status.getBytes();

		// acknowledging client
		DatagramPacket stati = new DatagramPacket(bytes, bytes.length, srcAddr, srcPort);
		socket.send(stati);
		System.out.println("Reply sent");
	}

	/**
	 * This method handles SWITCH requests from clients. The client will provide
	 * their netID as a parameter in the request, which the server will use to find
	 * which team the client belongs to. The server will remove the client and its
	 * status from that team and send back an ACK.
	 * 
	 * @param leavePkt
	 * @param teams
	 * @param socket
	 * @throws Exception
	 */
	private static void handleSwitch(DatagramPacket leavePkt) throws Exception {

		// getting client details
		InetAddress srcAddr = leavePkt.getAddress();
		int srcPort = leavePkt.getPort();

		// grabbing that sweet sweet data
		String updateMsg = getMessage(leavePkt);
		String[] parts = updateMsg.split(" ");
		int netID = Integer.parseInt(parts[1]);
		int teamID = Integer.parseInt(parts[2]);

		// preventing invalid netIDs
		if (netID < 1 || netID > clientIDs) {

			handleInvalidInput(leavePkt);
			return;
		}

		Client c = null;

		// looping through all teams
		for (int i = 0; i < teams.size(); i++) {

			Team t = teams.get(i);

			// when the client's team is found...
			if (t.containsClient(netID)) {

				c = t.removeClient(netID); // remove it and store it temporarily
			}
		}

		Team t = findTeamByID(teamID);

		if (t == null) {

			teamIDs++;
			t = new Team(teamIDs); // make one
			t.addClient(c); // add client to it
			teams.add(t); // and add to our list

		}

		// otherwise...
		else {

			t.addClient(c); // just add the client
		}

		// making message
		String msg = "ACK SWITCH";
		byte[] bytes = msg.getBytes();

		// acknowledging client
		DatagramPacket ack = new DatagramPacket(bytes, bytes.length, srcAddr, srcPort);
		socket.send(ack);
		System.out.println("Reply sent");
	}

	/**
	 * This method handles CONN requests from clients. The method creates a new
	 * client object to store their generate netID, responding to the client with
	 * that ID. It then receives the client's teamID. If a team by that ID exists,
	 * the client is added to that team. If no team exists, the server makes a new
	 * one and adds the client. The method also gets the clients initial status by
	 * means of the <b>handleUpdate</b> method.
	 * 
	 * @param connPkt
	 * @param clientIDs
	 * @param socket
	 * @throws Exception
	 */
	private static void handleNewClient(DatagramPacket connPkt) throws Exception {

		// getting client details
		InetAddress srcAddr = connPkt.getAddress();
		int srcPort = connPkt.getPort();

		// grabbing that sweet sweet data
		String connMsg = getMessage(connPkt);
		String[] parts = connMsg.split(" ");
		String name = parts[1];
		int teamID = Integer.parseInt(parts[2]);
		int status = Integer.parseInt(parts[3]);

		// assigning new client netID
		clientIDs++;
		int netID = clientIDs;

		Client c = new Client(netID, name, status);

		Team t = findTeamByID(teamID);

		// if no team by that ID exists...
		if (t == null) {

			teamIDs++;
			t = new Team(teamIDs); // make one
			t.addClient(c); // add client to it
			teams.add(t); // and add to our list
		}

		// otherwise...
		else {

			t.addClient(c); // just add the client
		}

		// making message
		String msg = "ACK CONN | YOUR ID : " + netID;
		byte[] bytes = msg.getBytes();

		// acknowledging new client with their unique netID
		DatagramPacket ack = new DatagramPacket(bytes, bytes.length, srcAddr, srcPort);
		socket.send(ack);
		System.out.println("Reply sent");

	}

	/**
	 * This method handles invalid requests from clients. It simply tells them they
	 * are stupid.
	 * 
	 * @param invalidPkt
	 * @throws IOException
	 */
	private static void handleInvalidInput(DatagramPacket invalidPkt) throws IOException {

		// getting client details
		InetAddress srcAddr = invalidPkt.getAddress();
		int srcPort = invalidPkt.getPort();

		// making message
		String msg = "INVALID INPUT";
		byte[] bytes = msg.getBytes();

		// acknowledging client
		DatagramPacket ack = new DatagramPacket(bytes, bytes.length, srcAddr, srcPort);
		socket.send(ack);
		System.out.println("Reply sent");
	}

	/**
	 * This helper method is capable of finding the team related to the given team
	 * ID
	 * 
	 * @param teamID
	 * @return matching team, null if not match
	 */
	private static Team findTeamByID(int teamID) {

		// looping through all teams
		for (int i = 0; i < teams.size(); i++) {

			if (teams.get(i).getTeamID() == teamID) {
				return teams.get(i); // returning matching team
			}

		}

		return null; // returning null if no match
	}

	/**
	 * I ripped this from the previous lab ngl
	 * 
	 * @param pkt
	 * @throws Exception
	 */
	private static String getMessage(DatagramPacket pkt) throws Exception {
		// Obtain references to the packet's array of bytes.
		byte[] buf = pkt.getData();

		// Wrap the bytes in a byte array input stream,
		// so that you can read the data as a stream of bytes.
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		// Wrap the byte array output stream in an input stream reader,
		// so you can read the data as a stream of characters.
		InputStreamReader isr = new InputStreamReader(bais);

		// Wrap the input stream reader in a buffered reader,
		// so you can read the character data a line at a time.
		// (A line is a sequence of chars terminated by any combination of \r and \n.)
		BufferedReader br = new BufferedReader(isr);

		// The message data is contained in a single line, so read this line.
		return br.readLine().trim();
	}

	static int clientIDs = 0;
	static int teamIDs = 0;
	static LinkedList<Team> teams = new LinkedList<>();
	static DatagramSocket socket;

	public static void main(String[] args) throws Exception {

		// some setup
		System.out.println("Initializing Teams Server...");
		Scanner scan = new Scanner(System.in);

		// getting server port
		System.out.println("\n\nPlease enter the server port:");
		String sPort = scan.nextLine();
		int port = Integer.parseInt(sPort);
		System.out.println("\n");

		socket = new DatagramSocket(port);

		// perpetual loop to continue receiving and sending
		while (true) {

			DatagramPacket request = receiveRequest(socket);

			// grabbing core command
			String msg = getMessage(request);
			String[] parts = msg.split(" ");

			// if client connecting...
			if (parts[0].equals("CONN")) {

				// if client does not give the right number of parameters..
				if (parts.length != 4) {

					handleInvalidInput(request);
				}

				// otherwise...
				else {

					handleNewClient(request);
				}
			}

			// if client updating status...
			else if (parts[0].equals("UPDATE")) {

				// if client does not give the right number of parameters..
				if (parts.length != 3) {

					handleInvalidInput(request);
				}

				// otherwise...
				else {

					handleUpdate(request);
				}
			}

			// if client getting team status...
			else if (parts[0].equals("GET")) {

				// if client does not give the right number of parameters..
				if (parts.length != 2) {

					handleInvalidInput(request);
				}

				// otherwise...
				else {
					handleGet(request);
				}

			}

			// if client leaving team...
			else if (parts[0].equals("SWITCH")) {

				// if client does not give the right number of parameters..
				if (parts.length != 3) {

					handleInvalidInput(request);
				}

				// otherwise...
				else {
					handleSwitch(request);
				}

			}

			// if invalid command...
			else {

				handleInvalidInput(request);
			}
		}
	}
}
