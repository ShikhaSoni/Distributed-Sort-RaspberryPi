/**
 * @author Shikha Soni
 * @author Harsh Patil
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;


public class SlaveTCP {
	LinkedList<String> sortedChunks = new LinkedList<>();
	ArrayBlockingQueue<Chunk> send=new ArrayBlockingQueue<>(10);
			
	Comparator<String> compare = new Comparator<String>() {

		@Override
		public int compare(String o, String b) {
			if (o.charAt(0) < b.charAt(0)) {
				return -1;
			} else if (o.charAt(0) > b.charAt(0)) {
				return 1;
			} else if (o.charAt(0) == b.charAt(0)) {
				if (Integer.parseInt(o.substring(1)) < Integer.parseInt(b
						.substring(1))) {
					return -1;
				} else if (Integer.parseInt(o.substring(1)) > Integer
						.parseInt(b.substring(1))) {
					return 1;
				}
			}
			return 0;
		}
	};

	ServerSocket mySocket;
	Socket masterSocket;
	int myport=8999;
	
	ObjectInputStream reader;
	public SlaveTCP(){
		try {
			mySocket= new ServerSocket(myport);
			masterSocket=mySocket.accept();
			//System.out.println("Connection received");
//			System.out.println("Making streams");
			reader= new ObjectInputStream(masterSocket.getInputStream());
			new Send(masterSocket).start();
	//		System.out.println("Streams made");
			rec();
			//start receiving chunks
			
		} catch (IOException e) {
			System.out.println("Master closed");
			//e.printStackTrace();
			System.exit(0);
		}
	}
	public void rec(){
		Chunk unsorted;
		while(true){
			try {
				unsorted=(Chunk) reader.readObject();
				Collections.sort(unsorted.getStrings(), compare);
				send.put(unsorted);
				/*try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
			} catch (ClassNotFoundException e) {
			} catch (IOException e) {
				System.out.println("Master stopped");
				break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public class Send extends Thread{
		Chunk sorted;
		ObjectOutputStream writer;
		public Send(Socket socket){
			try {
				this.writer= new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void run(){
			while(true){
				try {
					sorted=send.take();
					//System.out.println("The object: "+ sorted.getChunkNumber());
					writer.writeObject(sorted);
					writer.flush();
				} catch (IOException e) {
					System.exit(0);
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String args[]){
		try {
			System.out.println("My IP: "+InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		new SlaveTCP();
	}
}
