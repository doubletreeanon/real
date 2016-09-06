/*
 * MasterRequestHandler.java
 * 
 * Version : Java 1.8
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * MasterRequestHandler class is defined to handle the requests received by
 * master
 * 
 * @author
 * 
 */

public class MasterRequestHandler implements Runnable {

	private Master master;
	private Socket client;
	private RequestClient req;

	// constructor
	public MasterRequestHandler(Master master, Socket client) {
		this.master = master;
		this.client = client;
	}

	/**
	 * The function receiveAck is define to receive the ack from the server
	 * 
	 * @param None
	 * 
	 * @return None
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public void receiveAck() throws IOException, ClassNotFoundException {

		// opening socket connection to receive the ack
		ServerSocket server = new ServerSocket(master.serverPort);
		Socket client = server.accept();
		InputStream in = client.getInputStream();
		ObjectInputStream objInput = new ObjectInputStream(in);
		String str = (String) objInput.readObject();

		objInput.close();
		client.close();
		server.close();
	}

	@Override
	public void run() {

		InputStream in;
		OutputStream out;

		try {

			in = client.getInputStream();
			out = client.getOutputStream();
			ObjectInputStream objInput = new ObjectInputStream(in);
			req = (RequestClient) objInput.readObject();

			// if termination request is received
			if (req.doTerm == true) {

				master.sendTermination();
				ObjectOutputStream objOutput = new ObjectOutputStream(out);
				String str = "ACK";
				objOutput.writeObject(str);
				System.exit(0);

			} else if (req.isEnd == true) {

				System.out.println("Request for stats collection sent");

				// collecting stats
				master.collectStats();

				// sending ack to the client to proceed further
				ObjectOutputStream objOutput = new ObjectOutputStream(out);
				String str = "ACK";
				objOutput.writeObject(str);

			} else if (req.isTick == true) {

				// if the request is to server the request
				// sending reuest to all the servers
				master.serveRequest();

				// sending ack to the client to proceed further
				ObjectOutputStream objOutput = new ObjectOutputStream(out);
				String str = "ACK";
				objOutput.writeObject(str);

			} else {

				// if it is file request, randomly choose one of the servers in
				// the last level
				// and forward the request

				// constructing request
				Request newReq = new Request(req.fileName, false,
						client.getInetAddress(), true, false, "read", "up",
						null);

				// computing the num of nodes in the last level
				int nodesAtLastLevel = master.util.getLeafCount(
						master.serverList.size(), "lookup");

				// randomly choosing the position
				int pos = master.util.getRandom(1, nodesAtLastLevel);

				// computing the last level
				int lastLevel = master.util.getLevels(master.serverList.size());

				// System.out.println("Level : " + lastLevel + " pos : " + pos);
				// computing server id based on filename, level, pos
				int id = master.util.calcServerHash(req.fileName, lastLevel,
						pos);

				// resolving host
				String hostName = master.serverList.get(id);
				int tport = Integer.parseInt(hostName);
				newReq.cLevel = lastLevel;
				newReq.cPos = pos;
				newReq.clientPort = req.port;

				// System.out.println("Sending request to " + tport);

				// sending request to the server
				master.util.sendRequest(newReq.fileName, master.name, tport,
						newReq);

				// waiting to receive ack from the server
				receiveAck();

				// System.out.println("Received ACK");

				// sending ack to client to proceed further
				ObjectOutputStream objOutput = new ObjectOutputStream(out);
				String str = "ACK";
				objOutput.writeObject(str);

				client.close();
				objInput.close();
				objOutput.close();

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
