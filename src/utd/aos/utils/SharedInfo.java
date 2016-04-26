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
	
	private ConcurrentMap<String, SimpleControl> map; 
	
	private List<ConnInfo> connections;
	
	public SharedInfo (String filename,
			int myId) {
		this.fileIO = new FileIO(filename);
		this.myId = myId;
		this.isLeader = (LEADER_ID == myId);
		this.map = new ConcurrentHashMap<String, SimpleControl>();
		this.connections = new ArrayList<ConnInfo>();
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
}
