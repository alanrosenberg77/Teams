import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 * TeamClient contains the main that clients will use to interact with the
 * server. Clients can choose from a set of commands that will cause the server
 * to do a number of things. Conveniently, a couple helper methods exist too, to
 * facilitate sending packets and printing data of packets received from the
 * server.
 * 
 * @author Alan Rosenberg
 */
public class TeamClient {

	/**
	 * This method will handle the task of sending a packet
	 * 
	 * @param message
	 * @param destAddr destination address
	 * @param destPort destination port
	 * @return DatagramSocket socket
	 * @throws Exception
	 */
	public static void sendPacket(String message, InetAddress destAddr, int destPort, DatagramSocket socket)
			throws Exception {

		System.out.println("Sending packet...");

		// Formatting parameters
		byte[] content = message.getBytes();

		// Datagram for sending things
		DatagramPacket msg = new DatagramPacket(content, content.length, destAddr, destPort);

		// yeet
		socket.send(msg);

		// will store incoming packet
		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

		// pause until packet received
		System.out.println("Waiting on response...\n");
		socket.receive(packet);

		printData(packet);
	}

	/**
	 * I ripped this from the previous lab ngl
	 * 
	 * @param pkt
	 * @throws Exception
	 */
	private static void printData(DatagramPacket pkt) throws Exception {
		// Obtain references to the packet's array of bytes.
		byte[] buf = pkt.getData();

		// Wrap the bytes in a byte array input stream,
		// so that you can read the data as a stream of bytes.
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		// Wrap the byte array output stream in an input stream reader,
		// so you can read the data as a stream of characters.
		InputStreamReader isr = new InputStreamReader(bais);

		// Wrap the input stream reader in a bufferred reader,
		// so you can read the character data a line at a time.
		// (A line is a sequence of chars terminated by any combination of \r and \n.)
		BufferedReader br = new BufferedReader(isr);

		// The message data is contained in a single line, so read this line.
		String line = br.readLine();

		// Print host address and data received from it.
		System.out.println("Received from " + pkt.getAddress().getHostAddress() + ": " + new String(line));
	}

	public static void main(String[] args) throws Exception {

		// defining range of commands
		String[] commands = { "CONN (name) (new team id) (your status)", "UPDATE (your id) (your status)",
				"GET (your id)", "SWITCH (your id) (new team id)" };

		// some setup
		System.out.println("Welcome to Teams!");
		Scanner scan = new Scanner(System.in);

		// getting server IP
		System.out.println("\nPlease enter the server IP address: (format: a.b.c.d)");
		String sAddr = scan.nextLine().trim();
		InetAddress srvAddr = InetAddress.getByName(sAddr);

		// getting server port
		System.out.println("\nPlease enter the server port:");
		String sPort = scan.nextLine();
		int srvPort = Integer.parseInt(sPort);

		// initializing that socket
		DatagramSocket s = new DatagramSocket();

		// til user gets tired of it...
		while (true) {

			// printing command selection
			System.out.println("\nPlease choose from the following commands:");
			for (int i = 0; i < commands.length; i++) {

				System.out.println(commands[i]);

			}
			System.out.println("\n");

			// getting user's input
			String input = scan.nextLine().trim();

			// yeet
			sendPacket(input, srvAddr, srvPort, s);
		}
	}

}
