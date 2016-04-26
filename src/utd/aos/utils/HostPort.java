package utd.aos.utils;

public class HostPort {
	private int id;
	private String host;
	private int port;
	
	public HostPort(int id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
	}

	public int getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}	
	
}
