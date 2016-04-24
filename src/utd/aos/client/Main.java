package utd.aos.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import utd.aos.utils.SimpleMessage;
import utd.aos.utils.SimpleMessage.Type;
import utd.aos.utils.SocketMap;

public class Main {
  
	private static final int NUM_OF_WRITES = 40;
	private static final int RANGE_START = 10;
	private static final int RANGE_END = 50;
	private static final int NUM_OF_SERVERS = 3;

	public static void main (String[] args) throws Exception {
		
		//Server configuration
		String filename = "server.list";
		File file = new File(filename);
		List<SocketMap> sConnections = new ArrayList<SocketMap>();
		
		if(file.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String server = "";
			while((server = br.readLine()) != null) {
				String[] params = server.split(" ");
				
				// Create sockets and keep them alive
				// through the application lifetime
				try {
					Socket sock = new Socket(params[0],
							Integer.parseInt(params[1]));
					ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
					SocketMap sMap = new SocketMap(sock, oos, ois);
					
					sConnections.add(sMap);
				} catch (SocketException se) {
					se.printStackTrace();
				}
			}
			br.close();
		} else {
			System.err.println("Server configuration file not found");
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

				SimpleMessage message = new SimpleMessage(Type.REQUEST, 
						Integer.parseInt(args[0]), numOfWrites, 
						InetAddress.getLocalHost().getHostName());
				
				sMap.getO_out().writeObject(message);
				sMap.getO_out().flush();
				
				System.out.println((String)sMap.getO_in().readObject());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			numOfWrites++;
		}
		
	}

	private static int getRandom(int rangeStart, int rangeEnd) {
		Random rand = new Random();
		return rand.nextInt(rangeEnd - rangeStart + 1) + rangeStart;
	}
}
