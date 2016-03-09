/**
 * @author Shikha Soni: srs6573@rit.edu
 * @author Harsh Patil
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MasterTCP {
	/* Blocking */Queue<Chunk> chunks;
	List<Chunk> lookup = Collections.synchronizedList(new ArrayList<Chunk>());
	private static volatile ConcurrentHashMap<Socket, ArrayList<Integer>> activeWorkers;
	HashMap<Socket, ObjectOutputStream> outputStreams;
	ArrayList<String> IP = new ArrayList<>();
	TreeSet<String> mainFile;
	boolean EOFflag = false;
	int chunkNumber = 1;
	static volatile boolean recoveryFlag = false;
	boolean doTask2 = false;

	// --------------------------------
	// ObjectInputStream inputStream;
	int fileNum = 1;
	BlockingQueue<Chunk> recChunk;
	static volatile int counter;
	static volatile int cNumber, packetsRec;
	ArrayList<File> merge = new ArrayList<>();
	static volatile ArrayList<File> mergePass = new ArrayList<>();
	static volatile int countRecPackets;
	static long startTime;
	static long endTime;
	static long finalTime = 0;

	// --------------------------------

	String file;
	int packetSize;
	int masterPort, slavePort = 8999, numPi;

	/**
	 * 
	 * @param file
	 */
	public MasterTCP(String file) {
		this.file = file;
		packetSize = 20000;
		chunks = new /* ArrayBlockingQueue */LinkedList<Chunk>();
		activeWorkers = new ConcurrentHashMap<Socket, ArrayList<Integer>>();
	}

	/**
	 * 
	 * @author Shikha Soni: srs6573@rit.edu
	 *
	 */
	class Send {
		boolean recovered = false;

		public Send() {
			Socket socket = null;
			outputStreams = new HashMap<>();
			//System.out.println("No of IP: " + IP.size());
			for (int i = 0; i < IP.size(); i++) {
				try {
					socket = new Socket(InetAddress.getByName(IP.get(i)), slavePort);
					activeWorkers.put(socket, new ArrayList<Integer>());
					outputStreams.put(socket, new ObjectOutputStream(socket.getOutputStream()));
					Socket t = socket;
					new Receive(t).start();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Sender1 " + e);
					recovery(socket);
					System.out.println("Out of recover sender constructor");
				}
			}
			//System.out.println("Sender thread");
			Socket tmpsocket = null;
			Chunk readyToSend;
			while (true) {
				try {
					for (Socket s : activeWorkers.keySet()) {
						tmpsocket = s;
						while (recoveryFlag) {
							// waiting for flag
							recovered = true;
						}
						if (recovered) {
							recovered = false;
							break;
						}
						synchronized (chunks) {
							if (chunks.isEmpty()) {
								chunks.wait();
							}
							readyToSend = chunks.poll();
						}
						activeWorkers.get(s).add(readyToSend.getChunkNumber());
						outputStreams.get(s).writeObject(readyToSend);
						//System.out.println(
							//	"Chunk sent: " + readyToSend.getChunkNumber() + " to " + s.getInetAddress().toString());
						outputStreams.get(s).flush();

					}
				} catch (IOException e) {
					System.out.println("Socket failed: " + tmpsocket.getInetAddress().toString());
					while (recoveryFlag) {

					}
					recovery(tmpsocket);
					System.out.println("Back from recovery sender");
					continue;
					// recovery
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param socket
	 */
	public void recovery(Socket socket) {
		recoveryFlag = true;
		// look up for the active workers and fetch the list of packets
		if (!activeWorkers.containsKey(socket)) {
			System.out.println("Already removed:" + activeWorkers.size());
			recoveryFlag = false;
			return;
		}
		//System.out.println();
		synchronized (lookup) {
			for (int i : activeWorkers.get(socket)) {
				System.out.println("Looking for: " + i);
				for (Chunk c : lookup) {
					if (c.getChunkNumber() == i) {
						synchronized (chunks) {
							chunks.add(c);
						}
						System.out.println("Packet added back to the queue: " + c.getChunkNumber());
						break;
					}
				}
			}
			activeWorkers.remove(socket);
			System.out.println("Removed socket: " + socket.getInetAddress());
			recoveryFlag = false;

		}
	}

	/**
	 * 
	 * @param socket
	 * @param chunkNumber
	 */
	public void remove(Socket socket, int chunkNumber) {
		Iterator<Integer> i = activeWorkers.get(socket).iterator();
		while (i.hasNext()) {
			if (i.next() == chunkNumber) {
				i.remove();
				i = null;
				return;
			}
		}
	}

	/**
	 * 
	 * @param chunk
	 */
	public void remove(Chunk chunk) {
		synchronized (lookup) {
			Iterator<Chunk> i = lookup.iterator();
			while (i.hasNext()) {
				Chunk c = i.next();
				if (c.getChunkNumber() == chunk.getChunkNumber()) {
					i.remove();
					i = null;
					return;
				}
			}
		}
	}

	/*class ReadSum extends Thread {

		String stringRead;

		public void run() {
			BufferedReader br;
			try {
				startTime = System.currentTimeMillis();
				br = new BufferedReader(new FileReader("new_dataset_10000.txt"));
				while ((stringRead = br.readLine()) != null) {
					CountSum.createCountSumFile(stringRead);
				}
				CountSum.createSumFile(args[0]);
				endTime = System.currentTimeMillis();
				finalTime = endTime - startTime;
				System.out.println("****TASK 2 COMPLETED IN: " + finalTime / 1000 + " seconds****");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/

	/**
	 * 
	 * @author Shikha Soni: srs6573@rit.edu
	 *
	 */
	class Reader extends Thread {
		Chunk ch;
		ArrayList<String> tmp = new ArrayList<>();
		String stringRead;

		public void run() {
			BufferedReader br;
			try {
				startTime = System.currentTimeMillis();
				br = new BufferedReader(new FileReader(file));
				while ((stringRead = br.readLine()) != null) {
					if (doTask2 == true) {
						CountSum.createCountSumFile(stringRead);
					}
					if (tmp.size() < packetSize) {
						tmp.add(stringRead);
					} else {
						ch = new Chunk();
						ch.setStrings(tmp);
						ch.setChunkNumber(chunkNumber);
						synchronized (chunks) {
							chunks.add(ch);
							chunks.notify();
						}
						synchronized (lookup) {
							lookup.add(ch);
						}
						chunkNumber++;
						tmp.clear();
						new Receive(chunkNumber);
						tmp.add(stringRead);
					}
				}
				ch = new Chunk();
				ch.setStrings(tmp);
				ch.setChunkNumber(chunkNumber);
				synchronized (chunks) {
					chunks.add(ch);
				}
				synchronized (lookup) {
					lookup.add(ch);
				}
				chunkNumber++;
				tmp.clear();
				new Receive(chunkNumber);
				EOFflag = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[]) {
		String stringRead = null;
		MasterTCP master = new MasterTCP(args[0]);
		try {
			// task 2
			if ((args.length > 1) && (args[2].equals("true"))) {
				BufferedReader br;
				try {
					startTime = System.currentTimeMillis();
					br = new BufferedReader(new FileReader(args[0]));
					while ((stringRead = br.readLine()) != null) {
						CountSum.createCountSumFile(stringRead);
					}
					CountSum.createSumFile(args[0]);

					endTime = System.currentTimeMillis();
					finalTime = endTime - startTime;
					System.out.println("****TASK 2 COMPLETED IN: " + finalTime + " milliseconds****");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				BufferedReader br = new BufferedReader(new FileReader(new File(args[1])));
				while ((stringRead = br.readLine()) != null) {
					master.IP.add(stringRead);
				}
				br.close();
				master.numPi = master.IP.size();
				master.new Reader().start();
				master.new Send();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @author Shikha Soni: srs6573@rit.edu
	 *
	 */
	class Receive extends Thread {
		ObjectInputStream inputStream;
		Socket socket;
		boolean recovered = false;
		volatile int counter;

		/**
		 * 
		 * @param socket
		 */
		public Receive(Socket socket) {
			// this.numPi = numPi;
			recChunk = new ArrayBlockingQueue<Chunk>(4 * numPi);
			this.socket = socket;
			try {
				this.inputStream = new ObjectInputStream(socket.getInputStream());
				new FileMaker().start();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("error while getting the streams");
				recovery(socket);
				System.out.println("Out from recovery rec constructor");
			}
		}

		/**
		 * 
		 * @param chunkNumber
		 */
		public Receive(int chunkNumber) {
			// System.out.println(chunkNumber + " set");
			cNumber = chunkNumber;
		}

		public void run() {
			Chunk c = null;
			while (true) {// the number of packets sent have been received break
							// and
							// start other passes
				try {
					while (recoveryFlag) {
						// waiting for recovery flag
						recovered = true;
					}
					if (recovered) {
						recovered = false;
						break;
					}
					c = (Chunk) inputStream.readObject();
					synchronized (lookup) {
						remove(c);
					}
					remove(socket, c.getChunkNumber());
					packetsRec++;
					recChunk.put(c);
				} catch (IOException e) {
					System.out.println("Receiver: " + e);
					while (recoveryFlag) {

					}
					recovery(this.socket);
					//System.out.println("Out of recovery from receiver");
					break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					break;
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		/**
		 * 
		 * @author Shikha Soni: srs6573@rit.edu
		 *
		 */
		class FileMaker extends Thread {
			public void run() {
				int count = 0;
				// ArrayList<File> temp;
				while (true) {
					count++;
					Chunk c = null;
					try {
						while (recoveryFlag) {

						}
						//long heapSize = Runtime.getRuntime().totalMemory();
						//System.out.println(heapSize);

						/*
						 * if(heapSize>=200257856){ System.gc(); }
						 */

						if (count == 100) {
							System.gc();
							count = 0;
						}
						c = recChunk.take();
						countRecPackets++;

						if (counter >= (3 * numPi)) {
							Thread t = new Thread(new Merge(merge, 1, fileNum));
							t.start();
							t.join();
							mergePass.add(new File("PASS_" + 1 + "_" + fileNum + ".txt"));

							// break;
							fileNum++;
							counter = 0;
							// temp.clear();
							merge.clear();
						}
						// make text file
						File file = new File(c.getChunkNumber() + ".txt");
						BufferedWriter bw = new BufferedWriter(new FileWriter(file));
						for (String s : c.getStrings()) {
							bw.write(s + "\n");
						}
						merge.add(file);
						counter++;
						bw.close();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println(countRecPackets + "==" + (cNumber - 1));
					if (countRecPackets == cNumber - 1) {
						break;
					}
				}
				Thread t = new Thread(new Merge(merge, 1, fileNum));
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// break;
				mergePass.add(new File("PASS_" + 1 + "_" + fileNum + ".txt"));
				fileNum++;
				counter = 0;
				merge.clear();
				mergeTillEnd(mergePass);
				// Call a method for the final merge pass
			}
		}

		/**
		 * 
		 * @param files
		 */
		public void mergeTillEnd(ArrayList<File> files) {
			//Thread t = new Thread(new Merge(files, 2, 1, false));
			//t.start();
			int count = 2;
			int passNumber= 2;
			int fileNumber=1;
			ArrayList<File> last = new ArrayList<>();
			ArrayList<File> listFile = new ArrayList<>();
			for(File f: files){			
				if(count>0){
					listFile.add(f);
					count--;
	
				}else{
					//System.out.println("inside else:++++++++++++++++"+listFile);
					ArrayList<File> toBeAdded= new ArrayList<>();
					toBeAdded.addAll(listFile);
					Thread t = new Thread(new Merge(toBeAdded,passNumber,fileNumber));
					t.start();
					//System.out.println("passnumber" +passNumber+" filenumber "+fileNumber);
					last.add(new File("PASS_"+passNumber+"_"+fileNumber+".txt"));
					
					fileNumber++;
					count=2;
					listFile.clear();
					listFile.add(f);					
					try {
						t.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			ArrayList<File> toBeAdded= new ArrayList<>();
			toBeAdded.addAll(listFile);
			Thread t = new Thread(new Merge(toBeAdded,passNumber,fileNumber));
			t.start();
			last.add(new File("PASS_"+passNumber+"_"+fileNumber+".txt"));
			
			fileNumber++;
			count=2;
			listFile=null;
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//System.out.println("before new thread:------------------" +last);
			Thread x = new Thread(new Merge(last,3,1));
			x.start();
			try {
				x.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			endTime = System.currentTimeMillis();
			finalTime = endTime - startTime;
			//System.out.println("****SORTING COMPLETED IN: " + finalTime / 1000 + " seconds*****");
			System.exit(0);
		}
	}
}