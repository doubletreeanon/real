/*
 * RequestClient.java
 * 
 * Version : Java 1.8
 * 
 */
import java.io.Serializable;

/**
 * Class RequestClient is defined to construct request and useful information
 * between client and master
 * 
 * @author
 * 
 */

public class RequestClient implements Serializable {

	private static final long serialVersionUID = 1L;
	public String fileName;
	public int port;
	public boolean isEnd;
	public boolean isTick;
	public boolean doTerm;

	/**
	 * Constructor
	 * 
	 * @param fileName
	 *            name of file
	 * @param port
	 *            port number to send file
	 * @param isEnd
	 *            boolean to collect stats
	 * @param isTick
	 *            boolean to serve request
	 * @param doTerm
	 *            termination request
	 */

	public RequestClient(String fileName, int port, boolean isEnd,
			boolean isTick, boolean doTerm) {
		this.fileName = fileName;
		this.port = port;
		this.isEnd = isEnd;
		this.isTick = isTick;
		this.doTerm = doTerm;
	}
}
