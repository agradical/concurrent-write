package utd.aos.utils;

import java.io.Serializable;

public class SimpleControl implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5492385140694252853L;

	public static enum Type {
		DATA, REQUEST, COMMIT, AGREED, COMMIT_REQUEST, ACK, TERM 
	}
	
	private Type type;
	private int clientId;
	private int writeNum;
	private String hostName;
	
	private int requestCount;
	private int commitCount;
	private int agreedCount;
	private int commitrequestCount;
	private int ackCount;
	
	public SimpleControl(Type type, int clientId, int writeNum, String hostName) {
		this.type = type;
		this.clientId = clientId;
		this.writeNum = writeNum;
		this.hostName = hostName;
	
		this.requestCount = 0;
		this.agreedCount = 0;
		this.commitCount = 0;
		this.commitrequestCount = 0;
		this.ackCount = 0;
	}
	
	// Copy constructor
	public SimpleControl(SimpleControl that) {
		this.type = that.type;
		this.clientId = that.clientId;
		this.writeNum = that.writeNum;
		this.hostName = that.hostName;
		
		this.requestCount = that.requestCount;
		this.agreedCount = that.agreedCount;
		this.commitCount = that.commitCount;
		this.commitrequestCount = that.commitrequestCount;
		this.ackCount = that.ackCount;
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

	public int getRequestCount() {
		return requestCount;
	}

	public void incrRequestCount() {
		this.requestCount++;
	}

	public int getCommitCount() {
		return commitCount;
	}

	public void incrCommitCount() {
		this.commitCount++;
	}

	public int getAgreedCount() {
		return agreedCount;
	}

	public void incrAgreedCount() {
		this.agreedCount++;
	}

	public int getCommitrequestCount() {
		return commitrequestCount;
	}

	public void incrCommitrequestCount() {
		this.commitrequestCount++;
	}

	public int getAckCount() {
		return ackCount;
	}

	public void incrAckCount() {
		this.ackCount++;
	}
}
