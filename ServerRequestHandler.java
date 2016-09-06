/*
 * ServerRequestHandler.java
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
import java.util.ArrayList;

/**
 * Class ServerRequestHandler is defined to handle the requests received in the
 * server.
 * 
 * @author
 * 
 */
public class ServerRequestHandler implements Runnable {

	private Server server;
	private Socket client;
	private Request req;
	private boolean isFile;
	private int popularity;

	// constructor
	public ServerRequestHandler(Server server, Socket client) {
		this.server = server;
		this.client = client;
	}

	/**
	 * sendActToMaster function is defined to send the acknowledge to the master
	 * 
	 * @param None
	 * 
	 * @return None
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void sendAckToMaster() throws UnknownHostException, IOException {
		Socket client = new Socket(Server.masterName, Server.masterPort);
		OutputStream out = client.getOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(out);
		objOut.writeObject("ACK");
		objOut.close();
		client.close();
	}

	@Override
	public void run() {
		try {

			// receive the request from the master
			InputStream in = client.getInputStream();
			ObjectInputStream objInput = new ObjectInputStream(in);
			req = (Request) objInput.readObject();

			// getting the file availability and the file popularity
			synchronized (server.files) {
				synchronized (server.popular) {
					isFile = server.files.contains(req.fileName);
					if (isFile) {
						popularity = server.popular.get(req.fileName);
					}
				}
			}

			// if it is a termination request exit
			if (req.doTerm) {
				System.exit(0);
			} else if (req.serveRequest) {

				// if it is serve request create a thread to process one request
				// in the queue
				server.tickCount += 1;
				Thread t = new Thread(new ServerRequestProvider(server));
				t.start();

				// waiting for the thread to finish its process
				t.join();

				// sending ack to the master
				OutputStream out = client.getOutputStream();
				ObjectOutputStream objOutput = new ObjectOutputStream(out);
				String str = "ACK";
				objOutput.writeObject(str);
				objInput.close();
				objOutput.close();

			} else if (req.collectStats) {

				// if it is a collectStats request collect the stats
				OutputStream out = client.getOutputStream();
				ObjectOutputStream objOutput = new ObjectOutputStream(out);
				Stats statsReq;

				// collect the stats
				synchronized (server.reqQueue) {
					synchronized (server.popular) {
						synchronized (server) {
							statsReq = new Stats(server.reqQueue.size(),
									Server.numOfReplicas);
						}
					}
				}

				// send it back to the master
				objOutput.writeObject(statsReq);
				objInput.close();
				objOutput.close();

			} else if (req.isNotify) {

				// received notification to receive file
				server.util.receiveFile(client, req.fileName);

				// add the file in the availability list and the popularity
				synchronized (server.files) {
					synchronized (server.popular) {
						server.files.add(req.fileName);
						server.popular.put(req.fileName, 0);
					}
				}

				// if client is waiting for file, add the request in the queue
				if (req.client != null) {
					synchronized (server.reqQueue) {
						synchronized (server.popular) {
							server.reqQueue.add(req);
							server.popular.put(req.fileName,
									server.popular.get(req.fileName) + 1);
						}
					}

					// at the end of the process send the ack to master
					sendAckToMaster();
				}
			} else {

				// Read request
				if (req.type.equals("read")) {

					if (server.filePopular.containsKey(req.fileName)) {
						int num = server.filePopular.get(req.fileName);
						server.filePopular.put(req.fileName, num + 1);
					} else {
						server.filePopular.put(req.fileName, 1);
					}

					// if mode is "up"
					if (req.mode.equals("up")) {

						// System.out.println("Read Up received in " +
						// server.myPort);

						if (server.countUp.containsKey(req.fileName)) {
							int num = server.countUp.get(req.fileName);
							server.countUp.put(req.fileName, num + 1);
						} else {
							server.countUp.put(req.fileName, 1);
						}

						// file not available in the server
						if (!server.files.contains(req.fileName)) {

							// System.out.println("File is not available : countUp : "
							// + server.countUp.get(req.fileName));

							if (server.countUp.get(req.fileName) <= Server.upThreshold1) {

								int temp = req.cPos % server.D;

								// computing the position of parent node
								int pos = (temp == 0) ? req.cPos / server.D
										: (req.cPos / server.D) + 1;

								int serverId = server.util.calcServerHash(
										req.fileName, req.cLevel - 1, pos);
								String hostName = server.serverList
										.get(serverId);
								int port = Integer.parseInt(hostName);

								// System.out.println("Sending to parent node : level : "
								// + (req.cLevel -1) + " pos : " + pos);

								req.parentLevel = req.cLevel;
								req.parentPos = req.cPos;

								req.cLevel = req.cLevel - 1;
								req.cPos = pos;

								// sending request to that server ID
								server.util.sendRequest(req.fileName,
										Server.name, port, req);

							} else if (server.countUp.get(req.fileName) <= Server.upThreshold2) {

								// if the popularity of UP is exceeded

								// choose the left parent and forward the
								// request
								Node leftParent = server.util.selectLeftParent(
										req.cLevel, req.cPos);

								req.parentLevel = req.cLevel;
								req.parentPos = req.cPos;

								req.cLevel = leftParent.level;
								req.cPos = leftParent.pos;

								int serverId = server.util.calcServerHash(
										req.fileName, req.cLevel, req.cPos);
								String hostName = server.serverList
										.get(serverId);
								int port = Integer.parseInt(hostName);

								// mode is changed to "left"
								req.mode = "left";
								// sending request to that server ID
								server.util.sendRequest(req.fileName,
										Server.name, port, req);
							} else {

								// construct write request and forward to the
								// left
								// parent
								ArrayList<Node> list = new ArrayList<>();
								Node node = new Node(req.cLevel, req.cPos,
										req.client, req.clientPort);
								list.add(node);

								Request newReq = new Request(req.fileName,
										false, null, false, false, "write",
										"left", list);

								Node leftParent = server.util.selectLeftParent(
										req.cLevel, req.cPos);

								newReq.parentLevel = req.cLevel;
								newReq.parentPos = req.cPos;

								newReq.cLevel = leftParent.level;
								newReq.cPos = leftParent.pos;

								int serverId = server.util.calcServerHash(
										newReq.fileName, newReq.cLevel,
										newReq.cPos);
								String hostName = server.serverList
										.get(serverId);
								int port = Integer.parseInt(hostName);

								// sending request to that server ID
								server.util.sendRequest(newReq.fileName,
										Server.name, port, newReq);

							}
						} else {

							// if countUp is less than popular threshold or it
							// is a leaf
							// node
							// service the request

							// choose the left parent of the sender node and
							// forward
							// the request

							if (server.countUp.get(req.fileName) <= Server.POPULAR_THRESHOLD
									|| server.util.isLeaf(req.cLevel, req.cPos)) {

								// add the request in the queue and update its
								// popularity
								synchronized (server.reqQueue) {
									synchronized (server.popular) {
										server.reqQueue.add(req);
										server.popular.put(req.fileName,
												server.popular
														.get(req.fileName) + 1);
									}
								}
								// sending ack to master
								sendAckToMaster();
							} else {

								Node leftParent = server.util.selectLeftParent(
										req.parentLevel, req.parentPos);

								if (leftParent.level == req.cLevel
										&& leftParent.pos == req.cPos) {
									// add the request in the queue and update
									// its
									// popularity
									synchronized (server.reqQueue) {
										synchronized (server.popular) {
											server.reqQueue.add(req);
											server.popular
													.put(req.fileName,
															server.popular
																	.get(req.fileName) + 1);
										}
									}
									// sending ack to master
									sendAckToMaster();
								} else {

									req.parentLevel = req.cLevel;
									req.parentPos = req.cPos;
									req.cLevel = leftParent.level;
									req.cPos = leftParent.pos;

									// mode is changed from "up" to "left"
									req.mode = "left";

									int serverId = server.util.calcServerHash(
											req.fileName, req.cLevel, req.cPos);
									String hostName = server.serverList
											.get(serverId);
									int port = Integer.parseInt(hostName);

									// sending request to that server ID
									server.util.sendRequest(req.fileName,
											Server.name, port, req);

									int num = server.countUp.get(req.fileName);
									num = num - 1;
									server.countUp.put(req.fileName, num);
								}
							}
						}
					} else if (req.mode.equals("left")) {

						// if read request has mode as "left"

						if (server.countLeft.containsKey(req.fileName)) {
							int num = server.countLeft.get(req.fileName);
							server.countLeft.put(req.fileName, num + 1);
						} else {
							server.countLeft.put(req.fileName, 1);
						}

						// if it doesn't have the file
						if (!server.files.contains(req.fileName)) {

							// if popularity of countLeft is not exceeded
							if (server.countLeft.get(req.fileName) <= server.leftThreshold) {

								// forwarding request to left parent
								Node leftParent = server.util.selectLeftParent(
										req.cLevel, req.cPos);
								req.cLevel = leftParent.level;
								req.cPos = leftParent.pos;

								int serverId = server.util.calcServerHash(
										req.fileName, req.cLevel, req.cPos);
								String hostName = server.serverList
										.get(serverId);
								int port = Integer.parseInt(hostName);

								// sending request to that server ID
								server.util.sendRequest(req.fileName,
										Server.name, port, req);

							} else {

								// construct write request and forward to the
								// left
								// parent
								ArrayList<Node> list = new ArrayList<>();
								Node node = new Node(req.cLevel, req.cPos,
										req.client, req.clientPort);
								list.add(node);

								Request newReq = new Request(req.fileName,
										false, null, false, false, "write",
										"left", list);

								Node leftParent = server.util.selectLeftParent(
										req.cLevel, req.cPos);

								newReq.parentLevel = req.cLevel;
								newReq.parentPos = req.cPos;

								newReq.cLevel = leftParent.level;
								newReq.cPos = leftParent.pos;

								int serverId = server.util.calcServerHash(
										newReq.fileName, newReq.cLevel,
										newReq.cPos);
								String hostName = server.serverList
										.get(serverId);
								int port = Integer.parseInt(hostName);

								// sending request to that server ID
								server.util.sendRequest(newReq.fileName,
										Server.name, port, newReq);
							}
						} else {

							synchronized (server.reqQueue) {
								synchronized (server.popular) {
									server.reqQueue.add(req);
									server.popular
											.put(req.fileName, server.popular
													.get(req.fileName) + 1);
								}
							}

							// sending ack to master
							sendAckToMaster();
						}

					} else {

						// Incorrect request mode
						System.out.println("Incorrect request type");
					}
				} else if (req.type.equals("write")) {

					// write request with mode as "left"
					if (req.mode.equals("left")) {

						// if server has a file
						if (server.files.contains(req.fileName)) {

							// replicate the file to the last node in the list
							// remove the last node and forward the write
							// request to the
							// popped node
							int length = req.path.size();

							length -= 1;
							Node obj = req.path.remove(length);

							int serverId = server.util.calcServerHash(
									req.fileName, obj.level, obj.pos);
							String hostName = server.serverList.get(serverId);
							int port = Integer.parseInt(hostName);

							Request newReq = new Request(req.fileName, true,
									null, false, false, null, null, null);

							newReq.parentLevel = req.cLevel;
							newReq.parentPos = req.cPos;

							newReq.cLevel = obj.level;
							newReq.cPos = obj.pos;

							if (obj.client != null && obj.clientPort != -1) {
								newReq.clientPort = obj.clientPort;
								newReq.client = obj.client;
							}

							server.util.sendFile(req.fileName, Server.name,
									port, newReq);

							synchronized (server) {
								Server.numOfReplicas += 1;
							}

							// if list is non empty forward the write request
							// with type
							// as "right"
							if (req.path.size() != 0) {
								req.mode = "right";
								req.parentLevel = req.cLevel;
								req.parentPos = req.parentPos;
								req.cLevel = obj.level;
								req.cPos = obj.pos;

								// sending request to that server ID
								server.util.sendRequest(req.fileName,
										Server.name, port, req);

							}
						} else {

							// if file is not available, add itself to the list
							// forward the request to the left parent
							// construct write request and forward to the left
							// parent
							ArrayList<Node> list = req.path;
							Node node = new Node(req.cLevel, req.cPos);
							list.add(node);

							Request newReq = new Request(req.fileName, false,
									null, false, false, "write", "left", list);

							Node leftParent = server.util.selectLeftParent(
									req.cLevel, req.cPos);

							newReq.parentLevel = req.cLevel;
							newReq.parentPos = req.cPos;

							newReq.cLevel = leftParent.level;
							newReq.cPos = leftParent.pos;

							int serverId = server.util
									.calcServerHash(newReq.fileName,
											newReq.cLevel, newReq.cPos);
							String hostName = server.serverList.get(serverId);
							int port = Integer.parseInt(hostName);

							// sending request to that server ID
							server.util.sendRequest(newReq.fileName,
									Server.name, port, newReq);
						}
					} else if (req.mode.equals("right")) {

						// replicate the file to the last node in the list
						// remove the last node and forward the write request to
						// the
						// popped node
						int length = req.path.size();

						length -= 1;
						Node obj = req.path.remove(length);

						int serverId = server.util.calcServerHash(req.fileName,
								obj.level, obj.pos);
						String hostName = server.serverList.get(serverId);
						int port = Integer.parseInt(hostName);

						Request newReq = new Request(req.fileName, true, null,
								false, false, null, null, null);

						newReq.parentLevel = req.cLevel;
						newReq.parentPos = req.cPos;

						newReq.cLevel = obj.level;
						newReq.cPos = obj.pos;

						if (obj.client != null && obj.clientPort != -1) {
							newReq.clientPort = obj.clientPort;
							newReq.client = obj.client;
						}

						server.util.sendFile(req.fileName, Server.name, port,
								newReq);

						synchronized (server) {
							Server.numOfReplicas += 1;
						}

						// if list is non empty forward the write request with
						// type
						// as "right"
						if (req.path.size() != 0) {
							req.mode = "right";
							req.parentLevel = req.cLevel;
							req.parentPos = req.parentPos;
							req.cLevel = obj.level;
							req.cPos = obj.pos;

							// sending request to that server ID
							server.util.sendRequest(req.fileName, Server.name,
									port, req);

						}
					} else {
						System.out.println("Invalid MODE");
					}
				} else {
					System.out.println("Invalid TYPE");
				}

			}

			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
