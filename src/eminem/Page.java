package eminem;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;

public class Page extends Vector implements Serializable {
	public int number;
	public Vector<Tuple> vtrTuples;
	public String pageName;
//	public Object minKey;
//	public Object maxKey;

	public Page(String name, int num) throws DBAppException {

		this.number = num;
		this.pageName = name + number;
		// this.vtrTuples = new Vector<Tuple>(getPageMaxSize());
		this.vtrTuples = new Vector<Tuple>();
		try {
			ObjectOutputStream bin = new ObjectOutputStream(new FileOutputStream("data//" + name + number + ".class"));
			bin.writeObject(this);
			bin.flush();
			bin.close();

			// check what exactly to be serialized

		} catch (IOException e) {
			throw new DBAppException("error in finding file");
		}
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < vtrTuples.size(); i++) {
			s += vtrTuples.get(i);
		}
		return s;
	}

	public static void shiftAllOneDown(Vector<Tuple> t, int startIndex) {
		int lastIndexWanted = t.size();
		for (int i = lastIndexWanted; i > startIndex; i--) {
			t.add(i, t.remove(i - 1));
		}
	}
//
//	public static int getPages(String dir) throws IOException {
//		int num = 1; // number will be added as is!
//
//		try {
//
//			FileReader reader = new FileReader(dir);
//			BufferedReader br = new BufferedReader(reader);
//			String s;
//
//			while ((s = br.readLine()) != null) {
//				if (s.charAt(0) == '$')
//					num++;
//
//			}
//
//		} catch (FileNotFoundException e) {
//
//			e.printStackTrace();
//		}
//		return num;
//	}

	public static int getPageMaxSize() throws DBAppException {
		int num;

		try {
			FileReader reader = new FileReader("config\\DBApp.properties");

			Properties p = new Properties();
			p.load(reader);

			return num = Integer.parseInt(p.getProperty("MaximumRowsCountinPage"));
			// System.out.println(p.getProperty("password")); }
		} catch (IOException e) {
			throw new DBAppException("error in finding config file");
		}

	}
}
