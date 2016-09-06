/*
 * Stats.java
 * 
 * Version : Java 1.8
 */

import java.io.Serializable;

/**
 * Class Stats is defined to send the statistics of server to the master.
 * 
 * @author
 * 
 */
public class Stats implements Serializable {

	private static final long serialVersionUID = 1L;
	public int queueLength;
	public int numOfReplicas;

	// constructor
	public Stats(int qLen, int numOfReplicas) {
		// statistics includes queue length and number of replicas made
		queueLength = qLen;
		this.numOfReplicas = numOfReplicas;
	}
}
