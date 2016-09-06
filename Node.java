/*
 * Node.java
 * 
 * Version : Java 1.8
 */

import java.io.Serializable;
import java.net.InetAddress;

public class Node implements Serializable {

	private static final long serialVersionUID = 1L;
	public int pos;
	public int level;
	public InetAddress client;
	public int clientPort;

	public Node(int level, int pos) {
		this.level = level;
		this.pos = pos;
		this.client = null;
		this.clientPort = -1;
	}

	public Node(int level, int pos, InetAddress client, int clientPort) {
		this.level = level;
		this.pos = pos;
		this.client = client;
		this.clientPort = clientPort;
	}

}
