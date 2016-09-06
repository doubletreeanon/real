/*
 * Master.java
 * 
 * Version : Java 1.8
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Class Master is the one which receives the request from the clients and
 * forwards the request to one of the servers in the cluster using Hash
 * function.
 * 
 * @author
 * 
 */

public class Master {

	// list of available servers
	HashMap<Integer, String> serverList;

	// object of util functions
	public UtilFuncs util;
	private int port;

	// port to communicate with client
	private static int clientPort = 1024;

	// port to communicate with servers
	public int serverPort = 1025;

	// host name of servers simulated
	public static String name = "element.cs.rit.edu";
	public Socket clientSocket;

	// constructor
	public Master() {
		util = new UtilFuncs();
		port = util.getPort();
	}

	/**
	 * The function getServerList is used to get the available servers in the
	 * cluster
	 * 
	 * @param None
	 * 
	 * @return None
	 */

	public void getServerlist() {
		try {

			// getting server list from the util funcs
			serverList = util.getServerList();
		} catch (IOException e) {
			System.out.println("Error : Problem in getting the server list");
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * The function displayServerList is used to display the servers in the
	 * cluster
	 * 
	 * @param None
	 * 
	 * @return None
	 */

	public void displayServerList() {

		// iterating through the server list and displaying
		for (Integer key : serverList.keySet()) {
			String hostName = serverList.get(key);
			InetAddress hostDetails;

			try {
				hostDetails = InetAddress.getByName(hostName);
				System.out.println("Server ID : " + key + " ipaddr : "
						+ hostDetails.getHostAddress());
			} catch (UnknownHostException e) {
				System.out.println("ERROR : UNKNOWN HOST");
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	/**
	 * The function storeFile is used to store the file in the randomly chosen
	 * server and it is the root of tree for this particular file.
	 * 
	 * @param fileName
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public void storeFile(String fileName) throws UnknownHostException,
			IOException, ClassNotFoundException {

		// server id is chosen using hash function
		int serverId = util.calcServerHash(fileName, 0, 1);

		// resolving host
		String hostName = serverList.get(serverId);
		int tport = Integer.parseInt(hostName);

		// sending request
		Request req = new Request(fileName, true, null, false, false, null,
				null, null);
		req.cLevel = 0;
		req.cPos = 1;

		// sending file
		util.sendFile(fileName, name, tport, req);
	}

	/**
	 * The function startServer is used to start server socket connection in
	 * master to receive the requests from the client and process it.
	 * 
	 * @param None
	 * @return None
	 * @throws IOException
	 */

	public void startServer() throws IOException {
		System.out.println("Starting server");

		// creating server socket
		ServerSocket serverSock = new ServerSocket(clientPort);
		Socket client;

		// waiting to receive the connection
		while (true) {
			client = serverSock.accept();
			clientSocket = client;

			// A thread is started to process the request
			Thread t = new Thread(new MasterRequestHandler(this, client));
			t.start();
		}
	}

	/**
	 * The function collectStats is used to collect statistics from all the
	 * servers in the cluster and compute the final statistics of the system.
	 * 
	 * @param None
	 * 
	 * @return None
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public void collectStats() throws UnknownHostException, IOException,
			ClassNotFoundException {
		int maxQueueLength = 0;
		int avgQueueLength = 0;
		int replicas = 0;
		int count = 0;
		int total = 0;
		int sd = 0;

		// sending request to all the servers
		for (Integer key : serverList.keySet()) {
			String hostName = serverList.get(key);
			int tport = Integer.parseInt(hostName);
			Request req = new Request(true, false);

			// receiving statsReq object
			Stats statsReq = util.collectStats(name, tport, req);
			int length = statsReq.queueLength;
			replicas += statsReq.numOfReplicas;
			if (length != 0) {
				count += 1;
				total += length;
				if (maxQueueLength < length) {
					maxQueueLength = length;
				}
			}
		}

		// displaying the final statistics
		avgQueueLength = total / count;
		System.out.println("STATISTICS : ");
		System.out.println("Max Queue Length = " + maxQueueLength);
		System.out.println("Average Queue Length = " + avgQueueLength);
		System.out.println("Total requets in the cluster = " + total);
		System.out.println("Number of replications  = " + replicas);

		// calculating standard deviation
		count = 0;
		// sending request to all the servers
		for (Integer key : serverList.keySet()) {
			String hostName = serverList.get(key);
			int tport = Integer.parseInt(hostName);
			Request req = new Request(true, false);

			// receiving statsReq object
			Stats statsReq = util.collectStats(name, tport, req);
			int length = statsReq.queueLength;
			if (length != 0) {
				sd += (length - avgQueueLength) * (length - avgQueueLength);
				count += 1;
			}
		}
		sd = sd / count;
		sd = (int) Math.sqrt(sd);

		// displaying the standard deviation
		System.out.println("Standard Deviation : " + sd);
	}

	/**
	 * The function serveRequest is used to send request to all the servers to
	 * process a request
	 * 
	 * @param None
	 * 
	 * @return None
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public void serveRequest() throws UnknownHostException, IOException,
			ClassNotFoundException {

		// iterating through all the servers in the cluster
		for (Integer key : serverList.keySet()) {
			String hostName = serverList.get(key);
			int tport = Integer.parseInt(hostName);
			Request req = new Request(false, true);
			Socket client = new Socket(name, tport);
			OutputStream out = client.getOutputStream();
			ObjectOutputStream objOutput = new ObjectOutputStream(out);

			// sending request to the server
			objOutput.writeObject(req);
			InputStream in = client.getInputStream();
			ObjectInputStream objInput = new ObjectInputStream(in);

			// waiting for ack
			String ack = (String) objInput.readObject();
			objOutput.close();
			objInput.close();
			client.close();
		}
	}

	/**
	 * sendTermination is defined to send termination request to all the servers
	 * 
	 * @param None
	 * 
	 * @return None
	 * 
	 * @throws UnknownHostException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */

	public void sendTermination() throws UnknownHostException,
			ClassNotFoundException, IOException {

		// iterating through all the servers and sending termination request
		Request req = new Request(true);

		// iterating through all the servers
		for (Integer key : serverList.keySet()) {
			String hostName = serverList.get(key);
			int tport = Integer.parseInt(hostName);

			// sending termination request
			util.sendRequest(null, name, tport, req);
		}
	}

	/**
	 * The main function of the program
	 * 
	 * @param args
	 *            command line argument ignored
	 * 
	 * @return None
	 * 
	 * @throws UnknownHostException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */

	public static void main(String[] args) throws UnknownHostException,
			ClassNotFoundException, IOException {

		System.out.println("Master is running");
		Master master = new Master();

		// update the server list when the master starts
		master.getServerlist();

		// storing all the files in the cluster
		for (int i = 1; i <= master.util.getFileNum(); i++) {
			String file = i + ".txt";
			master.storeFile(file);
		}

		// starting server
		master.startServer();
	}
}
