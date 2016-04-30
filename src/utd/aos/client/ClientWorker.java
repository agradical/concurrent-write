package utd.aos.client;

import java.io.EOFException;
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
		while(true) {
			try {
				SimpleControl message = (SimpleControl)clientSock.getO_in().readObject();
				if(message.getType().equals(SimpleControl.Type.ACK)) {
					Main.ackReceived = true;
					Main.reply = message;
				}
			} catch (EOFException eof) {
				break;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
