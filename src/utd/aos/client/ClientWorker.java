package utd.aos.client;

import java.io.IOException;
import java.net.Socket;

import utd.aos.utils.SimpleControl;
import utd.aos.utils.SocketMap;

public class ClientWorker implements Runnable {
	private SocketMap clientSock;
	
	public ClientWorker() {
		
	}
	public ClientWorker(Socket socket) {
		clientSock = new SocketMap(socket);		
	}
	
	@Override
	public void run() {
		// TODO: handle ACKs
		while(true) {
			try {
				SimpleControl message = (SimpleControl)clientSock.getO_in().readObject();
				if(message.getType().equals(SimpleControl.Type.ACK)) {
					Main.ackReceived = true;
					Main.reply = message;
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
