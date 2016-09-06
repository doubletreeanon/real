/*
 * UtilFuncs.java
 * 
 * Version : Java 1.8
 * 
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Class UtilFuncs is defined to provide utility functions that are used by
 * servers, masters and clients
 * 
 * @author
 * 
 */

public class UtilFuncs {

	private static int D = 14;
	private static int d = 2;
	private HashMap<Integer, String> serversList;
	private int seedValue = 0;
	private static String SALT = "23IshitaMalhotra";
	private static String FUNCTION_NAME = "SHA-512";
	private static int port = 1199;
	public static int numOfFiles = 422;

	// constructor
	public UtilFuncs() {
		serversList = new HashMap<Integer, String>();
	}

	/**
	 * getLookupDegree function is used to return the degree of lookup tree
	 * 
	 * @return int degree of lookup tree
	 */

	public int getLookupDegree() {
		return D;
	}

	/**
	 * getRepDegree function returns the degree of replication tree
	 * 
	 * @return int degree of replication tree
	 */

	public int getRepDegree() {
		return d;
	}

	/**
	 * getPort function returns the port number
	 * 
	 * @return int port number
	 */

	public int getPort() {
		return port;
	}

	/**
	 * getFileNum returns the number of files used in the system
	 * 
	 * @return int number of files
	 */

	public int getFileNum() {
		return numOfFiles;
	}

	/**
	 * getServerList function reads the server list from the servers.txt file
	 * and updates the servers list hash map
	 * 
	 * @return server list
	 * @throws IOException
	 */

	public HashMap<Integer, String> getServerList() throws IOException {
		FileInputStream fStream;
		BufferedReader br;
		try {

			// reads the server name and the associated ip address
			fStream = new FileInputStream("servers.txt");
			br = new BufferedReader(new InputStreamReader(fStream));
			String strline;
			int key = 0;
			while ((strline = br.readLine()) != null) {

				// update the server list
				serversList.put(key, strline);
				key += 1;
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
			System.exit(0);
		}

		// returns the server list
		return serversList;
	}

	/**
	 * calcServerHash is a hash function that calculate the server Id based on
	 * fileName, level and position
	 * 
	 * @param fileName
	 *            FileName
	 * @param level
	 *            level in the tree
	 * @param pos
	 *            position in the tree
	 * @return server ID
	 */

	public int calcServerHash(String fileName, int level, int pos) {
		int degree = D;
		int size = serversList.size();
		int[] res = new int[2];

		// simple math expression to calculate the hash value
		if (level == 0) {
			res[0] = 0;
		} else {
			int num = 0;
			for (int i = level - 1; i >= 0; i--) {
				num += Math.pow(degree, i);
			}
			num += pos;
			res[0] = num - 1;
		}
		res[1] = fileHash(fileName) % size;

		// returns server id
		return (((Math.abs(res[1]) % size) + res[0] + seedValue) % size);

	}

	/**
	 * fileHash function uses SHA hash function to calculate the hash value for
	 * the file name
	 * 
	 * @param fileName
	 *            File Name
	 * @return hash value
	 */

	public int fileHash(String fileName) {
		try {

			// SHA function is called to find the hash value
			MessageDigest digest = MessageDigest.getInstance(FUNCTION_NAME);
			digest.update(SALT.getBytes("UTF-8"));
			byte[] out = digest.digest((fileName + SALT).getBytes("UTF-8"));
			String hex = hexToString(out);
			BigInteger val = new BigInteger(hex, 16);
			int mappedValue = val.intValue();
			return mappedValue;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return 0;
	}

	/**
	 * hexToString function converts the hexa decimal value to String
	 * 
	 * @param output
	 *            byte value of hexa decimal value
	 * @return String
	 */

	public String hexToString(byte[] output) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		StringBuffer buf = new StringBuffer();

		for (int j = 0; j < output.length; j++) {
			buf.append(hexDigit[(output[j] >> 4) & 0x0f]);
			buf.append(hexDigit[output[j] & 0x0f]);
		}

		return buf.toString();
	}

	/**
	 * getLevels function is used to calculate the number of levels in the tree
	 * 
	 * @param size
	 *            number of nodes in the tree
	 * 
	 * @return int level size
	 * 
	 */

	public int getLevels(int size) {
		int degree = D;
		int exp = -1;
		while (!((size - Math.pow((double) degree, (++exp))) <= (double) (0))) {
			size = (int) ((size - Math.pow((double) degree, (exp))));
		}
		return exp;
	}

	/**
	 * getLeafCount is used to calculate the number of leaf nodes in the tree
	 * 
	 * @param size
	 *            number of nodes in the tree
	 * 
	 * @param type
	 *            type replication or lookup tree
	 * 
	 * @return int number of leaf nodes
	 */

	public int getLeafCount(int size, String type) {
		int degree = (type.equals("rep")) ? d : D;
		int exp = -1;
		while (!((size - Math.pow((double) degree, (++exp))) <= (double) (0))) {
			size = (int) ((size - Math.pow((double) degree, (exp))));
		}
		return size;
	}

	/**
	 * getRandom is a function to choose the random number from min to max
	 * 
	 * @param min
	 *            minimum number
	 * @param max
	 *            maximum number
	 * 
	 * @return int random number
	 */

	public int getRandom(int min, int max) {
		Random r = new Random();
		max = max + 1;
		return r.nextInt(max - min) + min;
	}

	/**
	 * selectReplicationChild is a function to randomly choose one of its
	 * children in the replication tree with this node as parent in the
	 * replication tree
	 * 
	 * @param level
	 *            level of node in the lookup tree
	 * @param pos
	 *            position of node in the replication tree
	 * @return int position of randomly chosen child in the lookup tree
	 */

	public int selectReplicationChild(int level, int pos) {
		int size = serversList.size();
		int degree = D;
		int sum = 0;

		// calculating the remaining nodes
		for (int i = level; i >= 0; i--) {
			sum += Math.pow(degree, i);
		}
		size = size - sum;
		sum = 0;
		for (int i = 1; i < pos; i++) {
			sum += degree;
		}
		degree = d;
		int numOfChild;

		// calculating the number of child a node has
		if (degree <= (size - sum)) {
			numOfChild = degree;
		} else {
			numOfChild = (size - sum);
		}

		// choosing randomly a node
		return getRandom(sum + 1, sum + numOfChild);

	}

	/**
	 * selectMyChild function is used to choose randomly one of the children in
	 * the replication tree
	 * 
	 * @param level
	 *            level of node in the lookup tree
	 * @param pos
	 *            position of node in the lookup tree
	 * @param pLevel
	 *            level of node in the lookup tree which is root in the
	 *            replication tree
	 * @param pPos
	 *            position of node in the lookup tree which is root in the
	 *            replication tree
	 * @return int position of the node in the lookup tree
	 */

	public int selectMyChild(int level, int pos, int pLevel, int pPos) {
		int size = serversList.size();
		int sum = 0;

		// calculating the number of child for the node in pLevel and pPos
		for (int i = pLevel; i >= 0; i--) {
			sum += Math.pow(D, i);
		}
		size = size - sum;
		sum = 0;
		for (int i = 1; i < pPos; i++) {
			sum += D;
		}

		size = size - sum;
		int numOfChild;
		if (D <= size) {
			numOfChild = D;
		} else {
			numOfChild = size;
		}

		// randomly choosing the child for the node in pos and level
		int maxLimit = sum + numOfChild;
		int min = (d * (pos - sum)) + 1;
		min += sum;
		int max = (d * (pos - sum)) + d;
		max += sum;

		if (max <= maxLimit) {
			return getRandom(min, max);
		} else {
			return getRandom(min, maxLimit);
		}
	}

	/**
	 * isLeafRep function is used to find whether a node is leaf in replication
	 * tree
	 * 
	 * @param level
	 *            level of node in the lookup tree
	 * @param pos
	 *            position of node in the lookup tree
	 * @param pLevel
	 *            level of node in the lookup tree which is root node in the
	 *            replication tree
	 * @param pPos
	 *            position of node in the lookup tree which is root node in the
	 *            replication tree
	 * @return boolean true if it is leaf
	 */

	public boolean isLeafRep(int level, int pos, int pLevel, int pPos) {
		int size = serversList.size();
		int sum = 0;

		// calculating the number of child for the node in pLevel and pPos
		for (int i = pLevel; i >= 0; i--) {
			sum += Math.pow(D, i);
		}
		size = size - sum;
		sum = 0;
		for (int i = 1; i < pPos; i++) {
			sum += D;
		}

		size = size - sum;
		int numOfChild;
		if (D <= size) {
			numOfChild = D;
		} else {
			numOfChild = size;
		}

		// checking whether the node in level and pos has any children
		int maxLimit = sum + numOfChild;
		int min = (d * (pos - sum)) + 1;
		min += sum;
		int max = (d * (pos - sum)) + d;
		max += sum;
		if (min <= maxLimit || max <= maxLimit) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * isLeaf checks whether the node is leaf in lookup tree
	 * 
	 * @param level
	 *            level of node in the lookup tree
	 * @param pos
	 *            position of node in the lookup tree
	 * 
	 * @return boolean true or false
	 */

	public boolean isLeaf(int level, int pos) {
		int levels = getLevels(serversList.size());
		int degree;
		degree = D;
		if (levels == level) {
			return true;
		}
		int sum = 0;
		for (int i = level; i >= 0; i--) {
			sum += Math.pow(degree, i);
		}
		for (int i = 1; i < pos; i++) {
			sum += degree;
		}
		if (sum <= serversList.size()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * The function sendFile is used to send the file to one of the randomly
	 * chosen client using hash function.
	 * 
	 * @param fileName
	 *            name of the file to be sent
	 * 
	 * @param hostName
	 *            hostName to which file to be sent
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * @return None
	 */

	public void sendFile(String fileName, String hostName, int tport,
			Request req) throws UnknownHostException, IOException,
			ClassNotFoundException {

		// establishing socket connection
		Socket client = new Socket(hostName, tport);

		// streams to transfer the files
		OutputStream out = client.getOutputStream();
		ObjectOutputStream objOutput = new ObjectOutputStream(out);
		objOutput.writeObject(req);
		File file = new File(fileName);
		byte[] data = new byte[100];
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedOutputStream bos = new BufferedOutputStream(out);
		int bytesRead = bis.read(data, 0, data.length);

		// reading data from the file and sending it to the stream
		while (bytesRead != -1) {
			bos.write(data, 0, bytesRead);
			bytesRead = bis.read(data, 0, data.length);
		}

		bos.flush();
		bis.close();
		client.close();
	}

	/**
	 * sendRequest function is used to send request to the other nodes
	 * 
	 * @param fileName
	 *            fileName for which the request is generated
	 * @param hostName
	 *            name of the node to which request to be sent
	 * @param tport
	 *            port number of the node
	 * @param req
	 *            request to be sent
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * @return None
	 */

	public void sendRequest(String fileName, String hostName, int tport,
			Request req) throws UnknownHostException, IOException,
			ClassNotFoundException {

		// establishing socket connection
		Socket client = new Socket(hostName, tport);

		// object stream to send the request
		OutputStream out = client.getOutputStream();
		ObjectOutputStream objOutput = new ObjectOutputStream(out);
		objOutput.writeObject(req);

		objOutput.close();
		client.close();
	}

	/**
	 * receiveFile function is used to receive the file from the node
	 * 
	 * @param client
	 *            socket connection through which file should be received
	 * @param fileName
	 *            name of the file to be received
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * @return None
	 */

	public void receiveFile(Socket client, String fileName)
			throws UnknownHostException, IOException, ClassNotFoundException {

		// creating file streams to receive the file
		InputStream in = (InputStream) client.getInputStream();
		byte[] data = new byte[100];
		int bytesread;
		FileOutputStream fos = new FileOutputStream(fileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bytesread = in.read(data, 0, data.length);

		// reading data from the stream and storing it in file
		while (bytesread != -1) {
			bos.write(data);
			bytesread = in.read(data, 0, data.length);
		}

		bos.flush();
		bos.close();
	}

	/**
	 * sendFiletoClient function is used to send the files to the client
	 * 
	 * @param fileName
	 *            name of the file
	 * @param host
	 *            host name of the client
	 * @param tport
	 *            port number of the client
	 * @throws UnknownHostException
	 * @throws IOException
	 * 
	 * @return None
	 */

	public void sendFiletoClient(String fileName, InetAddress host, int tport)
			throws UnknownHostException, IOException {

		// establishing socket connection to transfer the files
		Socket client = new Socket(host, tport);
		OutputStream out = client.getOutputStream();
		File file = new File(fileName);
		byte[] data = new byte[100];
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedOutputStream bos = new BufferedOutputStream(out);
		int bytesRead = bis.read(data, 0, data.length);
		while (bytesRead != -1) {
			bos.write(data, 0, bytesRead);
			bytesRead = bis.read(data, 0, data.length);
		}
		bos.flush();
		bis.close();
		client.close();
	}

	/**
	 * collectStats function is used to collect the statistics of the server
	 * 
	 * @param hostName
	 *            host name of the master
	 * @param tport
	 *            port number of the master
	 * @param req
	 *            request to be sent
	 * 
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public Stats collectStats(String hostName, int tport, Request req)
			throws UnknownHostException, IOException, ClassNotFoundException {

		// establishing socket connection to receive the statistics
		Socket client = new Socket(hostName, tport);
		OutputStream out = client.getOutputStream();
		ObjectOutputStream objOutput = new ObjectOutputStream(out);
		objOutput.writeObject(req);
		InputStream in = client.getInputStream();
		ObjectInputStream objInput = new ObjectInputStream(in);
		Stats statsReq = (Stats) objInput.readObject();
		objOutput.close();
		objInput.close();
		client.close();

		// returning stats object
		return statsReq;
	}

	public Node selectLeftParent(int cLevelLookup, int cPosLookup) {

		int temp = cPosLookup % D;

		// computing the position of parent node
		int parentPos = (temp == 0) ? cPosLookup / D : (cPosLookup / D) + 1;

		int parentLevel = cLevelLookup - 1;

		ArrayList<Node> arr = new ArrayList<>();

		arr.add(new Node(parentLevel, parentPos));

		int size = serversList.size();
		int degree = D;
		int level = parentLevel;
		int pos = parentPos;
		int sum = 0;

		// computing the number of children
		for (int i = level; i >= 0; i--) {
			sum += Math.pow(degree, i);
		}
		size = size - sum;
		sum = 0;
		for (int i = 1; i < pos; i++) {
			sum += degree;
		}

		int numOfChild;
		if (degree <= (size - sum)) {
			numOfChild = degree;
		} else {
			numOfChild = (size - sum);
		}

		int min = sum + 1;
		int max = sum + numOfChild;
		int index = -1;

		for (int i = min; i <= max; i++) {
			arr.add(new Node(parentLevel + 1, i));
			if (i == cPosLookup) {
				index = arr.size() - 1;
				break;
			}
		}
		if (index == -1) {
			System.out.println("-------------");
			System.out.println("cLevel : " + cLevelLookup + " cPos : "
					+ cPosLookup);
			System.out.println("pLevel : " + parentLevel + " pPos : "
					+ parentPos);
			System.out.println("min : " + min + " max : " + max);
			System.out.println("num of child : " + numOfChild);
			System.out.println("Size : " + size);
			System.out.println("Server List " + serversList.size());
			System.out.println("arr size : " + arr.size());
			System.out.println("Serious error");
			System.out.println("--------------");
			System.exit(0);
		}

		int value = (index - 1) / d;

		return arr.get(value);
	}
}
