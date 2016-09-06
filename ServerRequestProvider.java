/*
 * ServerRequestProvider.java
 * 
 * Version : Java 1.8
 * 
 */
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Class ServerRequestProvider is defined to serve the requests in the queue. (
 * sending files to the client )
 * 
 * @author
 * 
 */

public class ServerRequestProvider implements Runnable {

	private Server server;
	private Request req;
	private boolean flag;

	// constructor
	public ServerRequestProvider(Server server) {
		this.server = server;
		flag = false;
	}

	@Override
	public void run() {

		// if queue is not empty
		synchronized (server.reqQueue) {
			if (server.reqQueue.size() != 0) {
				req = server.reqQueue.remove();
				flag = true;
			}
		}
		try {

			// if queue is not empty, send the requested file to the client
			if (flag == true) {
				server.util.sendFiletoClient(req.fileName, req.client,
						req.clientPort);
				flag = false;
			}

			// logic to decrement the popularity of file for every 100 ticks
			// for every 100 ticks the popularity counter is reset to zero
			// if in the next 100 ticks, counter value remains zero decrement
			// countUp and countLeft
			// if both the counters become zero remove the file from the node
			// unless it is root node for the file
			if (server.tickCount == 100) {
				// System.out.println("Serving request");
				boolean flag1 = false;
				boolean flag2 = false;

				for (String key : server.filePopular.keySet()) {
					int num = server.filePopular.get(key);
					if (num == 0) {
						if (server.countUp.containsKey(key)) {
							num = server.countUp.get(key);
							if (num != 0) {
								// System.out.println("Decrementing the counter");
								num -= 1;
								server.countUp.put(key, num);
							} else {
								flag1 = true;
							}
						} else {
							flag1 = true;
						}

						if (server.countLeft.containsKey(key)) {
							num = server.countLeft.get(key);
							if (num != 0) {
								// System.out.println("Decrementing the counter");
								num -= 1;
								server.countLeft.put(key, num);
							} else {
								flag2 = true;
							}
						} else {
							flag2 = true;
						}

					}

					if (flag1 == true && flag2 == true) {
						if (server.files.contains(key) == true) {
							int serverId = server.util
									.calcServerHash(key, 0, 1);

							// resolving host
							String hostName = server.serverList.get(serverId);
							int tport = Integer.parseInt(hostName);

							if (tport != server.myPort) {
								// System.out.println("Deleting file from the node");
								server.files.remove(key);
							}
						}
					}

					// reset the counter
					server.filePopular.put(key, 0);
				}
				server.tickCount = 0;
				// System.out.println("Successfully completed serving");
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
