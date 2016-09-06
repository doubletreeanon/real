/*
 * Request.java
 * 
 * Version : Java 1.8
 * 
 */

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Request is used to communicate the type of request and useful
 * information between master and servers
 * 
 * @author
 * 
 */

public class Request implements Serializable {

	private static final long serialVersionUID = 1L;
	public String fileName;
	public boolean isNotify;
	public InetAddress client;
	public int num;
	public boolean isClient;
	public boolean sendFile;
	private static int counter = 0;
	public int cLevel;
	public int cPos;
	public int parentPos;
	public int parentLevel;
	public InetAddress dstIp;
	public int dstPort;
	public int clientPort;
	public boolean collectStats;
	public boolean serveRequest;
	public boolean doTerm;
	public String type;
	public String mode;
	public ArrayList<Node> path;

	/**
	 * Constructor
	 * 
	 * @param fileName
	 *            name of file
	 * @param isNotify
	 *            boolean notification to receive file
	 * @param client
	 *            InetAddress of client
	 * @param isClient
	 *            boolean whether it is client
	 * @param sendFile
	 *            boolean whether to request for file
	 */

	public Request(String fileName, boolean isNotify, InetAddress client,
			boolean isClient, boolean sendFile, String type, String mode,
			ArrayList<Node> path) {
		this.fileName = fileName;
		this.isNotify = isNotify;
		this.client = client;
		this.isClient = isClient;
		this.sendFile = sendFile;
		counter += 1;
		this.num = counter;
		parentPos = -1;
		parentLevel = -1;
		dstIp = null;
		cLevel = -1;
		cPos = -1;
		clientPort = -1;
		dstPort = -1;
		collectStats = false;
		doTerm = false;
		this.type = type;
		this.mode = mode;
		this.path = path;
	}

	/**
	 * 
	 * Constructor
	 * 
	 * @param collectStats
	 *            boolean to collect stats
	 * 
	 * @param serveRequest
	 *            boolean to serve request
	 * 
	 */

	public Request(boolean collectStats, boolean serveRequest) {
		this.fileName = null;
		this.isNotify = false;
		this.client = null;
		this.isClient = false;
		this.sendFile = false;
		counter += 1;
		this.num = counter;
		parentPos = -1;
		parentLevel = -1;
		dstIp = null;
		cLevel = -1;
		cPos = -1;
		clientPort = -1;
		dstPort = -1;
		this.collectStats = collectStats;
		this.serveRequest = serveRequest;
		doTerm = false;
		this.type = null;
		this.mode = null;
		this.path = null;
	}

	/**
	 * Constructor
	 * 
	 * @param doTerm
	 *            boolean to notify termination request
	 */

	public Request(boolean doTerm) {
		this.fileName = null;
		this.isNotify = false;
		this.client = null;
		this.isClient = false;
		this.sendFile = false;
		counter += 1;
		this.num = counter;
		parentPos = -1;
		parentLevel = -1;
		dstIp = null;
		cLevel = -1;
		cPos = -1;
		clientPort = -1;
		dstPort = -1;
		collectStats = false;
		serveRequest = false;
		this.doTerm = doTerm;
		this.type = null;
		this.mode = null;
		this.path = null;
	}
}
