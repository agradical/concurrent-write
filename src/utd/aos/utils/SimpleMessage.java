package utd.aos.utils;

import java.io.Serializable;

public class SimpleMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5492385140694252853L;

	public static enum Type {
		REQUEST, COMMIT, AGREED, COMMIT_REQUEST, ACK
	}
	
	private Type type;
	private int clientId;
	private int writeNum;
	private String hostName;
	
	public SimpleMessage(Type type, int clientId, int writeNum, String hostName) {
		this.type = type;
		this.clientId = clientId;
		this.writeNum = writeNum;
		this.hostName = hostName;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getWriteNum() {
		return writeNum;
	}

	public void setWriteNum(int writeNum) {
		this.writeNum = writeNum;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getClientId() {
		return clientId;
	}

	public String getHostName() {
		return hostName;
	}	
	
}
