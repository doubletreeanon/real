/*
 * ClientFileReceiver.java
 * 
 * version : Java 1.8
 * 
 */

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ClientFileReceiver class is a thread used to receive files from the server.
 * 
 * @author
 * 
 */
public class ClientFileReceiver implements Runnable {

	private String fileName;
	private int port;
	public static int count = 0;
	private Client cli;

	// constructor
	public ClientFileReceiver(Client cli, String fileName, int port) {
		this.fileName = fileName;
		this.port = port;
		this.cli = cli;
	}

	/**
	 * The function receiveFile is used to receive a file from the server
	 * 
	 * @param client
	 *            socket connection through which file should be received
	 * @param fileName
	 *            name of the file
	 * @throws IOException
	 * 
	 * @return None
	 */
	public void receiveFile(Socket client, String fileName) throws IOException {

		// opening file streams to receive file
		InputStream in = (InputStream) client.getInputStream();
		byte[] data = new byte[100];
		int bytesread;

		// received files are stored
		fileName = fileName + "_copy_" + count;
		count++;
		FileOutputStream fos = new FileOutputStream(fileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bytesread = in.read(data, 0, data.length);
		while (bytesread != -1) {
			bos.write(data);
			bytesread = in.read(data, 0, data.length);
		}
		bos.flush();
		bos.close();
		client.close();
	}

	@Override
	public void run() {
		ServerSocket serverSock;
		try {

			// Socket connecting with specified port number
			serverSock = new ServerSocket(port);
			Socket client = serverSock.accept();

			// System.out.println("Receiving file");
			long start = System.currentTimeMillis();
			receiveFile(client, fileName);
			long end = System.currentTimeMillis();

			// calculating the time taken to receive file
			cli.totalTime += (end - start);
			cli.totalRequestServed += 1;
			serverSock.close();
		} catch (IOException e) {
			System.out.println("ERROR occured in IO");
		}
	}
}
