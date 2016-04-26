package utd.aos.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileIO {
	private File file;
	
	public FileIO(String filename) {
		file = new File(filename);
		try {
			// create file; overwrite
			FileWriter fw = new FileWriter(file, false);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public boolean canWrite() {
		return file.exists() && file.canWrite();
	}
	
	public synchronized void write(SimpleControl message) {
		String entry = "[" + message.getClientId()
			+ ", " + message.getWriteNum()
			+ ", " + message.getHostName() + "]";
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(entry);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
