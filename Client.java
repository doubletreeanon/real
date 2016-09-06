/*
 * Client.java
 * 
 * Version : Java 1.8
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

/**
 * Client is used to simulate the requests randomly to the files stored in the
 * server.
 * 
 * @author
 * 
 */

public class Client {

	// private String[] files = {"IntrotoDS.txt", "ramprasad.txt",
	// "AbdulKalambio.txt", "ObamaBio.txt"};

	// port number to communicate with the master
	public static final int masterPort = 1024;

	// master's host name
	public static final String masterName = "dargo.cs.rit.edu";
	public static int port = 1026;
	public static int reqNum = 0;

	// number of files in the cluster
	public static final int numOfFiles = 422;
	long totalRequestServed;
	long totalTime;

	// constructor
	public Client() {
		totalRequestServed = 0;
		totalTime = 0;
	}

	/**
	 * SendRequest function sends request to master for the randomly chosen file
	 * 
	 * @param option
	 *            file number to be sent
	 * @param isEnd
	 *            boolean to request for stats collection
	 * @param isTick
	 *            boolean to request for service
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * @return None
	 */

	public void sendRequest(int option, boolean isEnd, boolean isTick,
			boolean doTerm) throws UnknownHostException, IOException,
			ClassNotFoundException {

		RequestClient req = null;

		// reinitialize port num when it reaches max
		if (port > 65535) {
			port = 1026;
		}

		// sending termination request to all the servers
		if (doTerm) {
			req = new RequestClient(null, port, false, false, true);
		} else if (isEnd) {

			System.out.println("Collect stats request sent");
			// request is constructed to collect the stats
			req = new RequestClient(null, port, true, false, false);
		} else if (isTick) {

			// request is constructed to serve one request in all the servers
			// System.out.println("tick request sent");
			req = new RequestClient(null, port, false, true, false);
		} else {

			// sending request for file to the master
			String file = option + ".txt";
			req = new RequestClient(file, port, false, false, false);

			// Thread is started to receive file from the server
			Thread T = new Thread(new ClientFileReceiver(this, req.fileName,
					req.port));
			T.start();
			// System.out.println("Request : " + reqNum + " for " + req.fileName
			// + " sent successfully");
		}

		// sending request through socket connection
		Socket cliSocket = new Socket(masterName, masterPort);
		OutputStream out = cliSocket.getOutputStream();
		ObjectOutputStream objOutput = new ObjectOutputStream(out);
		objOutput.writeObject(req);
		InputStream in = cliSocket.getInputStream();

		// waiting for ack from the master
		ObjectInputStream objInput = new ObjectInputStream(in);
		String ack = (String) objInput.readObject();

		// System.out.println("Received : " + ack);
		objInput.close();
		objOutput.close();
		cliSocket.close();

		// incrementing port number and req num for the next request
		port++;
		reqNum++;
	}

	/**
	 * Main function of the program
	 * 
	 * @param args
	 *            ignored
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */

	public static void main(String[] args) throws UnknownHostException,
			IOException, InterruptedException, ClassNotFoundException {
		Client client = new Client();
		Scanner in = new Scanner(System.in);

		// getting request rate from the user
		System.out.println("Enter the request rate : ");
		int reqRate = in.nextInt();

		// getting the num of ticks from the user
		System.out.println("Enter the tick : ");
		int tick = in.nextInt();
		int option;

		// sending request for each tick
		for (int i = 0; i < tick; i++) {

			// System.out.println("Tick : " + i);

			// send request to server one request in all the servers
			// at the end of each tick
			if (i > 0) {
				Thread.sleep(500);
				client.sendRequest(-1, false, true, false);
			}

			// sending the reqRate request in each tick
			for (int j = 0; j < reqRate; j++) {
				Random rand = new Random();
				option = rand.nextInt(numOfFiles) + 1;
				client.sendRequest(option, false, false, false);
			}

			// collecting stats at appropriate ticks
			int k = i + 1;
			if (k == 1 || k == 10 || k == 100 || k == 200 || k == 400
					|| k == 600 || k == 800) {
				Thread.sleep(500);
				// System.out.println("TICK = " + i);
				client.sendRequest(-1, true, false, false);
				System.out.println(" For tick : " + k);

				if (client.totalRequestServed == 0) {
					System.out.println("Average lookup time : Infinity");
				} else {

					// calculating the average lookup time
					double avg = ((double) client.totalTime)
							/ ((double) client.totalRequestServed);
					System.out.println("Average lookup time : " + avg);
				}
			}
		}

		// sending request to serve request and collect stats at the end
		// of ticks
		Thread.sleep(500);
		client.sendRequest(-1, false, true, false);
		Thread.sleep(500);
		client.sendRequest(-1, true, false, false);

		// sending termination requests
		client.sendRequest(-1, false, false, true);
		System.out.println("For tick : 1000");

		// calculating average lookup time for the last tick
		double avg = ((double) client.totalTime)
				/ ((double) client.totalRequestServed);
		System.out.println("Average lookup time : " + avg);
		System.exit(0);
	}
}
