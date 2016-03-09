import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * 
 * @author Ganesh
 * 
 */
public class CountSum {

	static HashMap<Character, Integer> charCountMap = new HashMap<Character, Integer>();
	static HashMap<Character, Long> charSumMap = new HashMap<Character, Long>();

	/**
	 * 
	 * @param o
	 */
	public static void createCountSumFile(String o) {
		char ch = 0;
		if (o != null) {
			ch = o.charAt(0);

			if (charCountMap.get(ch) == null) {
				charCountMap.put(ch, 1);
			} else {
				charCountMap.put(ch, charCountMap.get(ch) + 1);
			}

			if (charSumMap.get(ch) == null) {
				charSumMap.put(ch, Long.parseLong((o.substring(1))));
			} else {
				charSumMap.put(ch,
						charSumMap.get(ch) + Long.parseLong(o.substring(1)));
			}

		}

	}

	/**
	 * 
	 * @param opFileName
	 */
	public static void createSumFile(String opFileName) {

		FileWriter fw;
		try {
			// String finName = opFileName.replace(".txt", "");
			File f = new File(opFileName);
			String finName = f.getName();
			finName = finName.replace(".txt", "");
			fw = new FileWriter(finName + "_sum.txt");
			PrintWriter pw = new PrintWriter(fw);

			char s;

			for (int i = 65; i <= 90; i++) {
				s = (char) i;

				if (charCountMap.get(s) == null) {

					pw.write(s + " " + "0 0" + "\n");
				}

				else {

					pw.write(s + " " + charCountMap.get(s) + " "
							+ charSumMap.get(s) + "\n");
				}
			}

			for (int i = 97; i <= 122; i++) {
				s = (char) i;

				if (charCountMap.get(s) == null) {

					pw.write(s + " " + "0 0" + "\n");
				}

				else {
					pw.write(s + " " + charCountMap.get(s) + " "
							+ charSumMap.get(s) + "\n");

				}
			}

			pw.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

}
