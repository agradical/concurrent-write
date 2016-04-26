package utd.aos.client;

import java.net.Socket;

import utd.aos.utils.SharedInfo;
import utd.aos.utils.SocketMap;

public class ClientWorker implements Runnable {
	private SocketMap clientSock;
	private SharedInfo sharedInfo;
	
	public ClientWorker() {
		
	}
	public ClientWorker(Socket socket, SharedInfo info) {
		clientSock = new SocketMap(socket);		
		this.sharedInfo = info;
	}
	
	@Override
	public void run() {
		// TODO: handle ACKs

	}

}
