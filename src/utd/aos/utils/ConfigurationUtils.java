package utd.aos.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ConfigurationUtils {

	public static void setupConnections(String filename, 
			List<SocketMap> sConnections) {
		File file = new File(filename);
		if(file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String server = "";
				while((server = br.readLine()) != null) {
					String[] params = server.split(" ");
					
					// Create sockets and keep them alive
					// through the application lifetime
					try {
						Socket sock = new Socket(params[0],
								Integer.parseInt(params[1]));
						SocketMap sMap = new SocketMap(sock);
						
						sConnections.add(sMap);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("File not found: " + file.getName());
		}
	}

	public static void setupPeers(int my_id, SharedInfo sharedInfo,
			String filename) {
		File file = new File(filename);
		if (file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String conf = "";
				int line_num = 1;
				
				// Configure peer config
				// Lazy approach: Establish connection when required 
				// for the first time and persist it
				while((conf = br.readLine()) != null) {
					String inet[] = conf.split(" ");
					int port = Integer.parseInt(inet[1]);
					if (line_num != my_id) {
						HostPort hp = new HostPort(line_num, inet[0], 
								port);
						SharedInfo.ConnInfo connInfo = new SharedInfo.ConnInfo(hp, null);
						sharedInfo.getConnections().add(connInfo);
					} else
						sharedInfo.setMyPort(port);
					
					line_num++;
				}
				br.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else {
			
		}
		
	}

}
