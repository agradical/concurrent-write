package utd.aos.client;

import java.io.ObjectInputStream;

import utd.aos.utils.MutexMessage;
import utd.aos.utils.Request;
import utd.aos.utils.MutexMessage.MessageType;
import utd.aos.utils.SocketMap;

public class ClientsServerThreadListener extends Client {
	
	SocketMap socketmap;
	public ClientsServerThreadListener(SocketMap socketmap) {
		this.socketmap = socketmap;
	}
	
	@Override
	public void run() {
		try {
			ObjectInputStream o_in = socketmap.getO_in();
			
			Object object = null;
			try {
				object = o_in.readObject();
			} catch (Exception e) {
				//Closing connection with other servers in case of termination from client
				System.out.println("--Closing connection--");
			}
			
			MutexMessage message = null;
			if(object instanceof MutexMessage) {
				message = (MutexMessage)object;
			} else {
				System.out.println("--->"+socketmap.getSocket().getInetAddress().getHostName()+"<---");
				System.out.println("------------DOOMED----------"+ object.getClass());
			}
			
			int client_id = message.getId();
			
			
		} catch (Exception e) {

			e.printStackTrace();
			try {
				shutdown();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
