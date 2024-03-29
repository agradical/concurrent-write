package utd.aos.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utd.aos.utils.ConfigurationUtils;
import utd.aos.utils.SharedInfo;
import utd.aos.utils.SharedInfo.ConnInfo;
import utd.aos.utils.SimpleControl;
import utd.aos.utils.SimpleControl.Type;

public class Main {
	
	//Executor service to handle multiple clients
	public static final int NTHREADS = 20;
    public static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
  
	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.err.println("java Main <server_id>");
			System.exit(-1);
		}
		int my_id = Integer.parseInt(args[0]);

		// set output file
		String outfile = "server" + my_id + ".out";
		
		// init SharedInfo
		SharedInfo sharedInfo = new SharedInfo(outfile, my_id);
		//List of server with their Host name and port		
		String filename = "server.list";
		
		try {			
			ConfigurationUtils.setupPeers(my_id, sharedInfo, filename);
			
			ServerSocket serverSocket = new ServerSocket(sharedInfo.getMyPort());
			while(sharedInfo.getClientsFinihed() < 7 &&
					!sharedInfo.doTerminate()) {
				try {
					serverSocket.setSoTimeout(1000);
					Socket socket = serverSocket.accept();
					exec.submit(new ServerWorker(socket, sharedInfo));
				} catch (SocketTimeoutException ste) {					
					continue;
				} catch(Exception e) {
					break;
				}
			}
			
			sendTerminateToPeers(sharedInfo);
			exec.shutdownNow();
			serverSocket.close();
			
		} catch(FileNotFoundException f) {
			System.out.print(f.getMessage());
			return;
		} catch(IOException i) {
			System.out.print(i.getMessage());
			return;
		}
	}

	private static void sendTerminateToPeers(SharedInfo sharedInfo) {
		SimpleControl msg = new SimpleControl(Type.TERM, sharedInfo.getMyId(),
				-1, null);
		List<ConnInfo> peers = sharedInfo.getConnections();
		for (ConnInfo connInfo : peers) {
			try {
				connInfo.getSockMap().getO_out().writeObject(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
