package utd.aos.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * Information shared across server threads
 */
public class SharedInfo {
	public final static int LEADER_ID = 1;
	
	private boolean isLeader;
	private FileIO fileIO;
	private int myId;
	private int myPort;
	private int clientsFinihed;
	private boolean doTerminate;
	
	private ConcurrentMap<String, SimpleControl> map; 
	
	//Only to be used by leader in order to maintain consistency	
	private boolean pendingCommitAck;
	
	private List<ConnInfo> connections;
		
	public SharedInfo (String filename,
			int myId) {
		this.fileIO = new FileIO(filename);
		this.myId = myId;
		this.isLeader = (LEADER_ID == myId);
		this.map = new ConcurrentHashMap<String, SimpleControl>();
		this.connections = new ArrayList<ConnInfo>();
		this.pendingCommitAck = false;
		this.clientsFinihed = 0;
		this.doTerminate = false;
	}
	
	public static class ConnInfo {
		private HostPort peerConfig;
		private SocketMap sockMap;
		
		public ConnInfo(HostPort peerConfig, SocketMap sockMap) {
			this.peerConfig = peerConfig;
			this.sockMap = sockMap;
		}

		public HostPort getPeerConfig() {
			return peerConfig;
		}

		public void setSockMap(SocketMap sockMap) {
			this.sockMap = sockMap;
		}

		public SocketMap getSockMap() {
			return sockMap;
		}
		
	}

	public boolean isLeader() {
		return isLeader;
	}
	
	public List<ConnInfo> getConnections() {
		return connections;
	}

	public FileIO getFileIO() {
		return fileIO;
	}

	public int getMyId() {
		return myId;
	}
	
	public int getMyPort() {
		return myPort;
	}

	public void setMyPort(int myPort) {
		this.myPort = myPort;
	}

	public void bufferMessage(SimpleControl message) {
		map.put(message.getKey(), message);
	}

	public void removeMessageFromBuffer(String key) {
		map.remove(key);
	}

	public SimpleControl getMessage(String key) {		
		return map.get(key);
	}

	public boolean isPendingCommitAck() {
		return pendingCommitAck;
	}

	public void setPendingCommitAck(boolean pendingCommitAck) {
		this.pendingCommitAck = pendingCommitAck;
	}

	public int getClientsFinihed() {
		return clientsFinihed;
	}

	public synchronized void incClientsFinihed() {
		this.clientsFinihed++;
	}

	public synchronized void terminate() {
		this.doTerminate = true;
	}
	
	public boolean doTerminate() {
		return this.doTerminate;
	}
}
