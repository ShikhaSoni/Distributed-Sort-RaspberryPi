import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Ganesh
 *
 */
public class Merge implements Runnable {

	List<File> fileList ;
	String outputFileName;
	String[] smallStrings;
	String outputFile;
	int passNum;
	int num=0;
	boolean startCount = false;
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

	/**
	 * Constructor
	 * 
	 * @param fileList  The list of files to be merged
	 * @param passNum	The current pass number of the merging
	 * @param num   The count of number of merges in a single pass  
	 */
	public Merge(List<File> fileList, int passNum, int num) {
		this.passNum=passNum;
		this.fileList=new ArrayList<File>();
		this.fileList.addAll(fileList);
		this.fileList = fileList;
		this.num=num;		
	}

	/**
	 * Constructor
	 * 
	 * @param fileList  The list of files to be merged
	 * @param passNum	The current pass number of the merging
	 * @param num   The count of number of merges in a single pass  
	 * @param startCount  the flag to perform the sum and count of prefixes.
	 */
	public Merge(List<File> fileList, int passNum, int num, boolean startCount) {
		this.passNum=passNum;
		this.fileList=new ArrayList<File>();
		this.fileList.addAll(fileList);
		this.fileList = fileList;
		this.num=num;
		this.startCount = startCount;
	}

	@Override
	public void run() {
		outputFile="PASS_"+passNum+"_"+num+".txt";
		mergeSortedFiles(fileList, new File(outputFile), compare);
	}

	/**
	 * @param fSize2
	 * @param fileList2
	 * @param outputFileName2
	 */
	
	public int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp) {
        PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(11, 
            new Comparator<BinaryFileBuffer>() {
              public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
                return cmp.compare(i.peek(), j.peek());
              }
            }
        );
        for (File f : files) {
            BinaryFileBuffer bfb = new BinaryFileBuffer(f);
            pq.add(bfb);
            bfb=null; ///
        }
        
        int rowcounter = 0;
        BufferedWriter fbw = null;
        try {
        	fbw = new BufferedWriter(new FileWriter(outputfile));
            while(pq.size()>0) {
                BinaryFileBuffer bfb = pq.poll();
                String r = bfb.pop();
                fbw.write(r);
                fbw.newLine();
                ++rowcounter;
                if(bfb.empty()) {
                    bfb.fbr.close();
                    bfb.originalfile.delete();
                   // System.out.println("created files: "+outputfile+" with "+files.toString());
                } else {
                    pq.add(bfb); // add it back
                }
                bfb=null; ///
            }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally { 
            try {
				fbw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            for(BinaryFileBuffer bfb : pq ) bfb.close();
        }
        pq = null;  ///
        return rowcounter;
    }


	class BinaryFileBuffer  {
	  //  public static int BUFFERSIZE = 2048;
	    public BufferedReader fbr;
	    public File originalfile;
	    private String cache;
	    private boolean empty;
	     
	    public BinaryFileBuffer(File f) {
	        originalfile = f;
	        try {
				fbr = new BufferedReader(new FileReader(f), 3072);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        reload();
	    }
	     
	    public boolean empty() {
	        return empty;
	    }
	     
	    private void reload(){
	        try {
	          if((this.cache = fbr.readLine()) == null){
	            empty = true;
	            cache = null;
	          }
	          else{
	            empty = false;
	          }
	      } catch(EOFException oef) {
	        empty = true;
	        cache = null;
	      } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    }
	     
	    public void close() {
	        try {
				fbr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	     
	     
	    public String peek() {
	        if(empty()) return null;
	        return cache.toString();
	    }
	    public String pop() throws IOException {
	      String answer = peek();
	        reload();
	      return answer;
	    }
	     
	}

}