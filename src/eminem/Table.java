package eminem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Table implements Serializable {

	public String name;
	public Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
	public String strClusteringKeyColumn;
	public Vector<String> usedPagesNames;
	public int index; // check if needed
	public String[] colNames;// added for delete

	public Table(String name, String strClusteringKeyColumn, Hashtable<String, String> htblColNameType)
			throws DBAppException {

		// this.name = name;
		this.name = name;
		this.htblColNameType.putAll(htblColNameType);
		htblColNameType.put("TouchDate", "java.util.Date");
		this.strClusteringKeyColumn = strClusteringKeyColumn;
		this.usedPagesNames = new Vector<String>();
		colNames = new String[htblColNameType.size()];
		// added for delete

		try {

			boolean flag = true;
			Enumeration e = htblColNameType.keys();
			Enumeration n = htblColNameType.elements();
			FileWriter writer = new FileWriter("data//metadata.csv", true);

			File mymetadata = new File("data//metadata.csv");
			if (mymetadata.length() == 0) {
				writer.append("Table Name, Column Name, Column Type, ClusteringKey, Indexed" + "\n");
			}

			int i = 0;
			while (e.hasMoreElements()) {

				String key = (String) e.nextElement();
				String value = (String) n.nextElement();
				switch (value) {
				case ("java.lang.Integer"):
					break;
				case ("java.lang.String"):
					break;
				case ("java.lang.Double"):
					break;
				case ("java.lang.Boolean"):
					break;
				case ("java.util.Date"):
					break;
				case ("java.awt.Polygon"):
					break;
				default:
					flag = false;

				}
				if (flag == false) {
					throw new DBAppException("invalid type");
				}

				colNames[i] = key;
//				System.out.println(colNames[i]);
				writer.append(name + ",");
				writer.append(key + ",");
				writer.append(value + ",");
				writer.append("" + strClusteringKeyColumn.equals(key) + ",");
				writer.append("false" + ",");

				writer.append("\n");
				i++;
			}

			writer.flush();
			writer.close();

			ObjectOutputStream bin = new ObjectOutputStream(new FileOutputStream("data//" + name + ".class"));
			bin.writeObject(this);
			bin.flush();
			bin.close();

		} catch (FileNotFoundException e1) {
			throw new DBAppException("error in finding file");
		} catch (IOException e) {
			throw new DBAppException("error in reading hashtable");
		}
	}

	public void createPage() {
		try {
			if (usedPagesNames.isEmpty()) {
				Page p = new Page(this.name, 0);
				usedPagesNames.add(p.pageName);
			} else {
				String lastname = usedPagesNames.lastElement();
				int newnum = Integer.parseInt(lastname.substring(this.name.length())) + 1;
				Page p = new Page(this.name, newnum);
				usedPagesNames.add(p.pageName);
			}

//			System.out.print(usedPagesNames.size());

		} catch (DBAppException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		System.out.println();
	}
}
