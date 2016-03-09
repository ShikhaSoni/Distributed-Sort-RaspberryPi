/**
 * @author Shikha Soni
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Chunk implements Serializable {

	private static final long serialVersionUID = 1L;
	private int chunkNumber;
	private ArrayList<String> strings;

	/**
	 * 
	 * @param chunkNumber
	 */
	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}

	/**
	 * 
	 * @return
	 */
	public int getChunkNumber() {
		return this.chunkNumber;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<String> getStrings() {
		return strings;
	}

	/**
	 * 
	 * @param strings
	 */
	public void setStrings(ArrayList<String> strings) {
		this.strings = new ArrayList<>();
		// this.strings = strings;
		Iterator<String> i = strings.iterator();
		while (i.hasNext()) {
			this.strings.add(i.next());
		}
	}
}
