package utd.aos.client;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import utd.aos.utils.ConfigurationUtils;
import utd.aos.utils.SharedInfo;
import utd.aos.utils.SimpleControl;
import utd.aos.utils.SimpleControl.Type;
import utd.aos.utils.SocketMap;

public class Main {
  
	private static final int NUM_OF_WRITES = 40;
	private static final int RANGE_START = 10;
	private static final int RANGE_END = 50;
	private static final int NUM_OF_SERVERS = 3;
	
	public static void main (String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("java Main <client_id>");
			System.exit(-1);
		}
		
		int my_id = Integer.parseInt(args[0]);
		
		//Server configuration
		String filename = "server.list";
		List<SocketMap> sConnections = new ArrayList<SocketMap>();		
		ConfigurationUtils.setupConnections(filename, sConnections);
		
		// client configurations
		filename = "client_id.list";
		String statsFile = "stats" + my_id + ".log";
		SharedInfo sharedInfo = new SharedInfo(statsFile,my_id);
		ConfigurationUtils.setupPeers(my_id, sharedInfo, filename);
		
		// start client server
		new Thread(new ClientServer(sharedInfo)).start();
		
		// wait till all clients are up
		while (!arePeersUp(sharedInfo)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				// Do nothing
			}
		}			
				
		int numOfWrites = 0;
		
		while (numOfWrites < NUM_OF_WRITES) {
			try {
				// Sleep for random amount of time;
				int sleepTime = getRandom(RANGE_START, RANGE_END);
				Thread.sleep(sleepTime);
				
				int serverNum = getRandom(1, NUM_OF_SERVERS);
				
				// get a server conn by random
				SocketMap sMap = sConnections.get(serverNum - 1);

				SimpleControl message = new SimpleControl(Type.DATA, 
						Integer.parseInt(args[0]), numOfWrites, 
						InetAddress.getLocalHost().getHostName());
				
				sMap.getO_out().writeObject(message);
				
				// TODO: Have to wait for ACK from leader
				sMap.getO_in().readObject();
				System.out.println(numOfWrites);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			numOfWrites++;
		}
	}

	private static boolean arePeersUp(SharedInfo sharedInfo) {
		for (SharedInfo.ConnInfo info : sharedInfo.getConnections()) {
			if (info.getSockMap() == null) {
				// try to connect;
				try {
					Socket sock = new Socket(info.getPeerConfig().getHost(),
							info.getPeerConfig().getPort());
					info.setSockMap(new SocketMap(sock));
				} catch (UnknownHostException e) {
					return false;
				} catch (IOException e) {
					return false;
				}
			}
		}
		return true;
	}

	private static int getRandom(int rangeStart, int rangeEnd) {
		Random rand = new Random();
		return rand.nextInt(rangeEnd - rangeStart + 1) + rangeStart;
	}
}
