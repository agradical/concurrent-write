package utd.aos.utils;

import java.io.Serializable;

public class SimpleControl implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5492385140694252853L;

	public static enum Type {
		DATA, REQUEST, COMMIT, AGREED, COMMIT_REQUEST, ACK
	}
	
	private Type type;
	private int clientId;
	private int writeNum;
	private String hostName;
	
	public SimpleControl(Type type, int clientId, int writeNum, String hostName) {
		this.type = type;
		this.clientId = clientId;
		this.writeNum = writeNum;
		this.hostName = hostName;
	}
	
	// Copy constructor
	public SimpleControl(SimpleControl that) {
		this.type = that.type;
		this.clientId = that.clientId;
		this.writeNum = that.writeNum;
		this.hostName = that.hostName;
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
	
	public String getHostName() {
		return this.hostName;
	}
	
	public int getClientId() {
		return clientId;
	}
	
	public String getKey() {
		return this.clientId + "_" + this.writeNum;
	}
}
