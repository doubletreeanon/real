/*
 * Server.java
 * 
 * Version : Java 1.8
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class Server is defined to provide the functionalities of servers in the
 * cluster.
 * 
 * @author
 * 
 */

public class Server {

	// server list
	public HashMap<Integer, String> serverList;

	// degrees of lookup tree and replication tree
	public int D;
	public int d;

	// util functions
	public UtilFuncs util;

	// sockets
	private ServerSocket serverSock;
	private Socket client;
	private int port;
	public int tickCount;

	// request queue
	public Queue<Request> reqQueue;

	// file availability and popularity
	public HashSet<String> files;
	public HashMap<String, Integer> popular;

	HashMap<String, Integer> filePopular;

	HashMap<String, Integer> countUp;
	HashMap<String, Integer> countLeft;

	// host information
	public static String name = "localHost";
	public int myPort;

	public static int numOfReplicas = 0;

	// port num to communicate with master
	public static final int masterPort = 1025;
	public static final String masterName = "dargo.cs.rit.edu";

	// threshold values
	public static final int BUSY_THRESHOLD = 6;

	public static final int upThreshold1 = 1;
	public static final int upThreshold2 = 2;

	public static final int POPULAR_THRESHOLD = 4;

	public static final int leftThreshold = 1;

	public Server() {
		util = new UtilFuncs();
		D = util.getLookupDegree();
		d = util.getRepDegree();
		port = util.getPort();
		reqQueue = new LinkedList<Request>();
		files = new HashSet<String>();
		popular = new HashMap<String, Integer>();
		filePopular = new HashMap<String, Integer>();
		countUp = new HashMap<String, Integer>();
		countLeft = new HashMap<String, Integer>();
		tickCount = 0;
	}

	/**
	 * getServerList function is used to update the server list
	 * 
	 * @param None
	 * 
	 * @return None
	 * 
	 */

	public void getServerList() {
		try {
			serverList = util.getServerList();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROR in getting server list");
			System.exit(0);
		}
	}

	/**
	 * startServer function waits for the socket connection and it starts thread
	 * for each connection
	 * 
	 * @param None
	 * @return None
	 * @throws IOException
	 */

	public void startServer() throws IOException {
		serverSock = new ServerSocket(myPort);
		while (true) {
			client = serverSock.accept();
			Thread t = new Thread(new ServerRequestHandler(this, client));
			t.start();
		}
	}

	/**
	 * Main function of the program
	 * 
	 * @param args
	 *            command line argument should have the port number
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException {

		Server server = new Server();
		server.getServerList();

		if (args.length < 1) {
			System.out.println("Please Enter the port number");
			System.exit(0);
		}
		server.myPort = Integer.parseInt(args[0]);

		// starting servers to receive the request
		server.startServer();
	}
}
