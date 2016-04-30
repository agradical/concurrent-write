package utd.aos.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utd.aos.utils.SharedInfo;

public class ClientServer implements Runnable {
	
	public static final int NTHREADS = 20;
    public static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);

	private SharedInfo sharedInfo;
	ServerSocket serverSocket;
	boolean doStop;
	
	public ClientServer(SharedInfo info) {
		this.sharedInfo = info;
		this.doStop = false;
	}
	
	public void stop() {
		this.doStop = true;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(sharedInfo.getMyPort());
			while(!doStop) {
				try {
					serverSocket.setSoTimeout(1000);
					Socket socket = serverSocket.accept();
					exec.submit(new ClientWorker(socket));
				}
				catch (SocketTimeoutException ste) {
					continue;
				} catch(Exception e) {
					exec.shutdownNow();
					break;
				}
			}
			exec.shutdownNow();
			serverSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
