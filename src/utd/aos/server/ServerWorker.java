package utd.aos.server;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import utd.aos.utils.ConfigurationUtils;
import utd.aos.utils.SharedInfo;
import utd.aos.utils.SharedInfo.ConnInfo;
import utd.aos.utils.SimpleControl;
import utd.aos.utils.SimpleControl.Type;
import utd.aos.utils.SocketMap;

public class ServerWorker implements Runnable {
	
	private SocketMap clientSock;
	private SharedInfo sharedInfo;
	
	public static List<SocketMap> clientConnections;
		
	public ServerWorker() {
		
	}
	
	public ServerWorker(Socket socket, SharedInfo info) {
		clientSock = new SocketMap(socket);
		
		this.sharedInfo = info;
	}

	@Override
	public void run() {
		// setup peer connections, one time
		ServerWorker.setup(this.sharedInfo);
		
		while (!sharedInfo.doTerminate()) {
			try {
				SimpleControl message = (SimpleControl)clientSock.getO_in().readObject();				
				processMessage(message);
				
				//Setup connections to client only for leader and only once
				if(sharedInfo.isLeader() && clientConnections == null) {
					setupClients();
				}
				
			} catch (EOFException eofe) {
				break;
			} catch (SocketException soe) {
				break;
			}catch (IOException | ClassNotFoundException ioe) {
				ioe.printStackTrace();
			}
		}				
	}
	
	private void processMessage(SimpleControl message) {
		switch (message.getType()) {
			// data from client
			case DATA:
				processData(message);
				break;
			case REQUEST:
				processRequest(message);
				break;
			case COMMIT_REQUEST:
				processCommitRequest(message);
				break;
			case COMMIT:
				performCommit(message);
				break;
			case TERM:
				sharedInfo.terminate();
				break;
			default:
				break;
		}
	}

	/*
	 * Setup connections with peers
	 */
	private static synchronized void setup(SharedInfo sharedInfo) {
		for (ConnInfo conn : sharedInfo.getConnections()) {
			// set up connection if not done so
			if (conn.getSockMap() == null){
				try {
					Socket sock = new Socket(conn.getPeerConfig().getHost(),
							conn.getPeerConfig().getPort());
					
					SocketMap sockMap = new SocketMap(sock);
					conn.setSockMap(sockMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void processData(SimpleControl message) {
		// put the message in the buffer
		message.incrRequestCount();
		sharedInfo.bufferMessage(message);		
		
		boolean allAgreed = true;
		SimpleControl newMessage = new SimpleControl(message);
		newMessage.setType(Type.REQUEST);
		for (ConnInfo conn : sharedInfo.getConnections()) {
			try {
				message.incrRequestCount();
				SimpleControl reply = sendReceive(newMessage, conn);				
				if (!reply.getType().equals(Type.AGREED)) {
					allAgreed = false;
				} else {
					message.incrAgreedCount();
				}
			} catch (IOException e) {
				allAgreed = false;
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		
		// if all have agreed, send commit request to peers 
		if (allAgreed) {
			if (sharedInfo.isLeader()) {
				processCommitRequest(message);
			} else {
				message.incrCommitrequestCount();
				SimpleControl commitReq = new SimpleControl(message);
				commitReq.setType(Type.COMMIT_REQUEST);
				sendMessage(commitReq, SharedInfo.LEADER_ID);
			}
		} else {
			// TODO: handle this unhappy case
		}
		
	}
	
	/*
	 * Send message and receive the reply over the same connection
	 */
	private SimpleControl sendReceive(SimpleControl newMessage, ConnInfo conn)
			throws IOException, ClassNotFoundException {
		SimpleControl reply;
		synchronized (conn) {
			conn.getSockMap().getO_out().writeObject(newMessage);
			reply = (SimpleControl) conn.getSockMap().getO_in().readObject();
		}
		return reply;
	}
	
	private void sendMessage(SimpleControl message, int serverId) {

		for (ConnInfo conn : sharedInfo.getConnections()) {
			if (conn.getPeerConfig().getId() == serverId) {
				try {
					synchronized (conn) {
						conn.getSockMap().getO_out().writeObject(message);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}		
	}
	
	private void processRequest(SimpleControl message) {
		// put the message in the buffer
		sharedInfo.bufferMessage(message);
		
		if (sharedInfo.getFileIO().canWrite()) {
			SimpleControl newMessage = new SimpleControl(message);
			newMessage.setType(Type.AGREED);
			try {
				clientSock.getO_out().writeObject(newMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//TODO: send abort
		}
	}
	
	/*
	 * Called for only leader
	 */
	private void processCommitRequest(SimpleControl message) {
		/*
		while(sharedInfo.isPendingCommitAck()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		sharedInfo.setPendingCommitAck(true);
		*/
		synchronized (sharedInfo.sharedLock) {
			performCommit(message);
			broadcastCommit(message);			
		}
		/*
		sharedInfo.setPendingCommitAck(false);*/
		
		if(sharedInfo.isLeader()) {
			returnAck(message);
		}
	}
	
	private void performCommit(SimpleControl message) {
		
		SimpleControl data = sharedInfo.getMessage(message.getKey());
		sharedInfo.getFileIO().write(data);

		sharedInfo.removeMessageFromBuffer(message.getKey());
		
		//if NON-LEADER return ACK to leader
		if(!sharedInfo.isLeader()) {	
			message.incrAckCount();
			SimpleControl newMessage = new SimpleControl(message);
			newMessage.setType(Type.ACK);
			try {
				clientSock.getO_out().writeObject(newMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean broadcastCommit(SimpleControl message) {
				
		SimpleControl newMessage = new SimpleControl(message);
		newMessage.setType(Type.COMMIT);
		
		boolean allAcks = true;
		
		for (ConnInfo conn : sharedInfo.getConnections()) {
			try {
				message.incrCommitCount();
				SimpleControl ack = sendReceive(newMessage, conn);
				if(!ack.getType().equals(Type.ACK)) {
					allAcks = false;
				} else {
					message.incrAckCount();
				}
			} catch (ClassNotFoundException e) {
				allAcks = false;
				e.printStackTrace();
			} catch (IOException e) {
				allAcks = false;
				e.printStackTrace();
			}
		}
		
		return allAcks;
	}
	
	public static synchronized void setupClients() {
		if(clientConnections == null) {

			String filename = "client_id.list";
			clientConnections = new ArrayList<SocketMap>();
			
			ConfigurationUtils.setupConnections(filename, clientConnections);
		}
	}
	
	private void returnAck(SimpleControl message) {
		int client_id = message.getClientId();
		SimpleControl newMessage = new SimpleControl(message);
		newMessage.setType(Type.ACK);

		try {
			clientConnections.get(client_id-1).getO_out().writeObject(newMessage);
			if (message.getWriteNum() == 39) {
				clientConnections.get(client_id - 1).getSocket().close();
				sharedInfo.incClientsFinihed();;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
