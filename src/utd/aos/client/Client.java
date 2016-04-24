package utd.aos.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import utd.aos.utils.Message;
import utd.aos.utils.MessageRecord;
import utd.aos.utils.MutexMessage;
import utd.aos.utils.MutexMessage.MessageType;
import utd.aos.utils.Operations;
import utd.aos.utils.Resource;
import utd.aos.utils.SocketMap;
import utd.aos.utils.Operations.OperationMethod;
import utd.aos.utils.Operations.OperationType;

public class Client implements Runnable{
	
	public static int id;
	
	public static Map<String, SocketMap> allClientsSockets;
	public static Map<Integer, InetSocketAddress> otherClients;
	public static Map<String, Integer> hostIdMap;	
	public static Map<String, SocketMap> allClientsListenerSockets;
		
	public static SocketMap serverSocketMap;
	
	public static InetAddress ip;
	public static Integer port;

	public static int count = 1;
	public static MessageRecord record = new MessageRecord();
	
	boolean curr_req_done = false;
	public Client() {
	
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		init();
		Random rand = new Random();
		
		
		while(count <= 40) {

			curr_req_done = false;
			Integer delay = rand.nextInt(40);
			
			delay += 10;
			
			reset();

			try {
				
				Thread.sleep(delay);

				Resource resource = new Resource();
				resource.setFilename("test");

				Operations operation = new Operations();
				String uuid = UUID.randomUUID().toString();
				operation.setUuid(uuid);
				operation.setOperation(OperationMethod.WRITE);
				operation.setType(OperationType.REQUEST);
				operation.setInputResource(resource);
				operation.setArg(id+" : "+count+" : "+InetAddress.getLocalHost().getHostName()+"\n");

				
				System.out.println("--adding my request to fifo--");
				
				//Request request = new Request(id, null, MessageType.REQUEST);
				
				request(operation);
				
			} catch (Exception e) {
				e.printStackTrace();
				try{
					shutdown();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			count++;

		}
		
		try {
			shutdown();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void printreport(){
		File file = new File("record_"+id);
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file);
			
			int total_message = record.request + record.reply + record.release + record.fail + record.enquire + record.yield + record.grant;
			
			String report = "For Request: "+count+"\n";
			report += "TIME: "+record.time+" milis\n";
			report += "TOTALMESSAGES:"+total_message+"\n";
			report += "REQUEST: "+record.request+"\n";
			report += "REPLY: "+record.reply+"\n";
			report += "RELEASE: "+record.release+"\n";
			report += "FAIL: "+record.fail+"\n";
			report += "ENQUIRE: "+record.enquire+"\n";
			report += "YIELD: "+record.yield+"\n";
			report += "GRANT: "+record.grant+"\n";
			
			report += "---------------\n";
			
			fw.write(report);
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void init() {
		
		//Check for all clients to be started
		for(Map.Entry<Integer, InetSocketAddress> entry: otherClients.entrySet()) {
			InetSocketAddress addr = entry.getValue();
			while(true) {
			    try {	    	
					Socket socket = new Socket(addr.getHostName(), addr.getPort());
					InputStream in = socket.getInputStream();
					OutputStream out = socket.getOutputStream();

					ObjectInputStream o_in = new ObjectInputStream(in);
					ObjectOutputStream o_out = new ObjectOutputStream(out);
					
					System.out.println("--Saving streams--");
					
					MutexMessage testmessage = new MutexMessage();
					testmessage.setId(id);
					testmessage.setType(MessageType.TEST);
					o_out.writeObject(testmessage);

					SocketMap socketmap = new SocketMap(socket, o_out, o_in, addr);
					
					if(allClientsSockets == null) {
						allClientsSockets = new HashMap<String, SocketMap>();
						allClientsSockets.put(addr.getHostName(), socketmap);
					} else {
						allClientsSockets.put(addr.getHostName(), socketmap);
					}
										
					System.out.println("Connect success: "+ip.getHostName()+"->"+addr.getHostName());					
					
					break;
			    
			    } catch(ConnectException e) {
			    	System.out.println("Connect failed, waiting and trying again: "+ip.getHostName()+"->"+addr.getHostName());
			        try {
			            Thread.sleep(1000);
			        }
			        catch(InterruptedException ie) {
			            ie.printStackTrace();
			        }
			    } catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		}

	}
	
	public void shutdown() throws IOException, ClassNotFoundException {
		Operations operation = new Operations();
		operation.setOperation(OperationMethod.TERMINATE);
		serverSocketMap.getO_out().writeObject(operation);
		serverSocketMap.getO_in().readObject();
		
		if(!allClientsListenerSockets.isEmpty()) {
			for (Map.Entry<String, SocketMap> entry : allClientsListenerSockets.entrySet()) {
				if(!entry.getValue().getSocket().isClosed()) {
					entry.getValue().getO_out().close();
				}
			}
		}
		
		if(!allClientsSockets.isEmpty()) {
			for (Map.Entry<String, SocketMap> entry : allClientsSockets.entrySet()) {
				if(!entry.getValue().getSocket().isClosed()) {
					entry.getValue().getO_out().close();
				}
			}
		}
	}
	
	public void reset() {
		
		record.enquire = 0;
		record.fail = 0;
		record.release = 0;
		record.reply = 0;
		record.request = 0;
		record.yield = 0;
		record.grant = 0;
		record.time = 0;
		
		/*
		pendingReleaseToReceive = 0;
		gotFailed = 0;
		sentYield = 0;
		
		pendingRepliesToReceive = new HashMap<Integer, Boolean>();
		gotFailedMessageFrom = new HashMap<Integer, Boolean>();
		sentYieldMessageTo = new HashMap<Integer, Boolean>();
		*/
		
	}
	
	public void sendRelease() throws IOException {
		System.out.println("--send release to all--");
		for(Map.Entry<String, SocketMap> entry: allClientsSockets.entrySet()) {
			SocketMap quorum_client = entry.getValue();
			MutexMessage message = new MutexMessage(id, MessageType.RELEASE);
			quorum_client.getO_out().writeObject(message);
		}
	}
	
	public Message request(Operations operation) throws IOException, ClassNotFoundException {	
		//creating the request
		System.out.println("--sending request to server--");

		if(operation.getOperation().equals(OperationMethod.CREATE)) {
			Resource resource = operation.getInputResource();
			File file = new File(resource.getFilename());
			if(file.exists()) {
				String fileContent = "";
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = "";
				while((line = br.readLine()) != null ) {
					fileContent += line;
				}					
				resource.setFileContent(fileContent);
				br.close();
				
				operation.setInputResource(resource);
			}
		}
		
		//sends the operation request
		serverSocketMap.getO_out().writeObject(operation);
		
		//wait for the response
		Object object = serverSocketMap.getO_in().readObject();
		
		
		Message m;
		
		if (object instanceof Message) {
			 m = (Message)object;
			 return m;
		} else {
			return null;
		}
	}

	public int getId() {
		return id;
	}

	public SocketMap getServerSocketMap() {
		return serverSocketMap;
	}

	public InetAddress getIp() {
		return ip;
	}

	public Integer getPort() {
		return port;
	}


	public Map<Integer, InetSocketAddress> getOtherClients() {
		return otherClients;
	}


	public Map<String, Integer> getHostIdMap() {
		return hostIdMap;
	}


}
