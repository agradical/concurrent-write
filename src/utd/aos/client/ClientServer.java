package utd.aos.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utd.aos.utils.SharedInfo;

public class ClientServer implements Runnable {
	
	public static final int NTHREADS = 20;
    public static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);

	private SharedInfo sharedInfo;
	
	public ClientServer(SharedInfo info) {
		this.sharedInfo = info;
	}

	@Override
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(sharedInfo.getMyPort());
			while(true) {
				try {
				Socket socket = serverSocket.accept();
				exec.submit(new ClientWorker(socket));
				} catch(Exception e) {
					break;
				}
			}
			serverSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
