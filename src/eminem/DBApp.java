package eminem;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class DBApp {

	public static int maxPageSize = initializePageSize();

	public static int initializePageSize() {
		try {
			int n = Page.getPageMaxSize();
			return n;
		} catch (DBAppException e) {
			System.out.print(e.getMessage());
			;
			return 0;
		}
	}

	// this method produces an array of column names with corresponding data types
	public static ArrayList<String> getArrayOfColoumnDataTyoe(String tableName) throws DBAppException {

		String csvFile = "data/metadata.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		ArrayList<String> arrColumn = new ArrayList<String>();
		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] d = line.split(cvsSplitBy);
				if (d[0].equals(tableName)) {

					arrColumn.add(d[1] + "," + d[2]);
				}
			}
			br.close();

		} catch (Exception e) {
			throw new DBAppException("error in getting column data types");
		}

//		System.out.println(arrColumn.toString());
		return arrColumn;
	}

//  this  does  whatever  initialization  you  would  like 
// or leave it empty if there is no code you want to 
// execute at application startup
	public void init() {

		try {
			maxPageSize = Page.getPageMaxSize();
			FileWriter writer = new FileWriter("data//metadata.csv", true);

			writer.append("Table Name");
			writer.append(',');
			writer.append("Column Name");
			writer.append(',');
			writer.append("Column Type");
			writer.append(',');
			writer.append("ClusteringKey");
			writer.append(',');
			writer.append("Indexed");
			writer.append(',');

			writer.append('\n');

		} catch (Exception e) {
			System.out.println("error in initialization");
		}
	}

	// this method check if there exists a table with this name
	public static boolean checkIfTableFound(String tableName) {

		File folder = new File("data");
		File[] listOfFiles = folder.listFiles();
		boolean flag = false;

		for (File file : listOfFiles) {
			if (file.isFile()) {
				if (file.getName().equals(tableName + ".class")) {
					flag = true;
					break;

				}
			}
		}
		return flag;
	}

	public void createTable(String strTableName, String strClusteringKeyColumn,
			Hashtable<String, String> htblColNameType) throws DBAppException {

		boolean foundFile = checkIfTableFound(strTableName);
		if (foundFile == true) {
			throw new DBAppException("Table already existing");
		} else {
			Table t = new Table(strTableName, strClusteringKeyColumn, htblColNameType);

		}

	}

	public static Object getDeserlaized(String path) throws DBAppException {
		try {
			// Creating stream to read the object
			// System.out.println(path);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
			Object a = in.readObject();
			in.close();
			return a;
		} catch (Exception e) {
			throw new DBAppException("error in deserialization");
		}

	}

	public void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {

		boolean found = checkIfTableFound(strTableName);

		if (!found) {
			throw new DBAppException("Table does not exist");
		} else {
			try {

				checkTypeSize(htblColNameValue, strTableName);
				Tuple nTuple = createTuple(htblColNameValue, strTableName);
				Table toBeInstertedIn = (Table) getDeserlaized("data//" + strTableName + ".class");

				if (toBeInstertedIn.usedPagesNames.isEmpty()) {
					toBeInstertedIn.createPage();
					Page pageToBeInstertedIn = (Page) getDeserlaized(
							"data//" + toBeInstertedIn.usedPagesNames.get(0) + ".class");
					pageToBeInstertedIn.vtrTuples.add(nTuple);

					FileOutputStream f = new FileOutputStream(
							"data//" + toBeInstertedIn.usedPagesNames.get(0) + ".class");

					ObjectOutputStream bin = new ObjectOutputStream(f);
					bin.writeObject(pageToBeInstertedIn);
					bin.flush();
					bin.close();
					f.close();
				} else {
					Vector<String> usedPages = toBeInstertedIn.usedPagesNames;

					int flag = 0;

					for (int i = 0; i <= usedPages.size() - 1 && flag == 0; i++) {

						Page pageToBeInstertedIn = (Page) (getDeserlaized(
								"data//" + toBeInstertedIn.usedPagesNames.get(i) + ".class"));
						Vector<Tuple> Tuples = pageToBeInstertedIn.vtrTuples;

						for (int j = 0; j < Tuples.size(); j++) {
							Tuple TuplesinPage = Tuples.get(j);
							int compare = TuplesinPage.compareTo(nTuple);
							if (compare > 0) {

								Tuple temp = Tuples.get(j);
								pageToBeInstertedIn.vtrTuples.remove(j);
								pageToBeInstertedIn.vtrTuples.insertElementAt(nTuple, j);
								nTuple = temp;
							}
						}
						if (pageToBeInstertedIn.vtrTuples.size() < maxPageSize
								&& i != toBeInstertedIn.usedPagesNames.size() - 1) {
							Page nextPage = (Page) (getDeserlaized(
									"data//" + toBeInstertedIn.usedPagesNames.get(i + 1) + ".class"));
							int compare = (nextPage.vtrTuples.get(0)).compareTo(nTuple);
							if (compare >= 0) {
								pageToBeInstertedIn.vtrTuples.add(nTuple);
								nTuple = null;
								flag = 1;

							}

						}
						ObjectOutputStream bin = new ObjectOutputStream(
								new FileOutputStream("data//" + toBeInstertedIn.usedPagesNames.get(i) + ".class"));
						bin.writeObject(pageToBeInstertedIn);
						bin.flush();
						bin.close();
					}

					if (nTuple != null) {
						Page lastPage = (Page) (getDeserlaized(
								"data//" + toBeInstertedIn.usedPagesNames.get(toBeInstertedIn.usedPagesNames.size() - 1)
										+ ".class"));
						if (lastPage.vtrTuples.size() < maxPageSize) {
							lastPage.vtrTuples.add(nTuple);
							lastPage.vtrTuples.sort(null);
							ObjectOutputStream bin = new ObjectOutputStream(new FileOutputStream("data//"
									+ toBeInstertedIn.usedPagesNames.get(toBeInstertedIn.usedPagesNames.size() - 1)
									+ ".class"));
							bin.writeObject(lastPage);
							bin.flush();
							bin.close();
						} else if (lastPage.vtrTuples.size() == maxPageSize) {
							toBeInstertedIn.createPage();
							Page p = (Page) (getDeserlaized("data//"
									+ toBeInstertedIn.usedPagesNames.get(toBeInstertedIn.usedPagesNames.size() - 1)
									+ ".class"));

							p.vtrTuples.add(nTuple);

							ObjectOutputStream bin = new ObjectOutputStream(new FileOutputStream("data//"
									+ toBeInstertedIn.usedPagesNames.get(toBeInstertedIn.usedPagesNames.size() - 1)
									+ ".class"));
							bin.writeObject(p);
							bin.flush();
							bin.close();
						}
					}

				}
				FileOutputStream f1 = new FileOutputStream("data//" + strTableName + ".class");
				ObjectOutputStream bin1 = new ObjectOutputStream(f1);
				bin1.writeObject(toBeInstertedIn);

			} catch (IOException e) {
				throw new DBAppException("error in insertion");
			}

		}
	}

	// following method inserts one row at a time
	public void insertIntoTable2(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws DBAppException {

		boolean found = checkIfTableFound(strTableName);

		if (!found) {
			System.out.print("Table does not exist");
		} else {
			try {

				checkTypeSize(htblColNameValue, strTableName);
				Tuple nTuple = createTuple(htblColNameValue, strTableName);
				// System.out.println(nTuple.vtrTupleObj.toString());
				Table toBeInstertedIn = (Table) getDeserlaized("data//" + strTableName + ".class");
				if (toBeInstertedIn.usedPagesNames.isEmpty()) {
					// System.out.println("wpw");
					toBeInstertedIn.createPage();
					Page pageToBeInstertedIn = (Page) getDeserlaized(
							"data//" + toBeInstertedIn.usedPagesNames.get(0) + ".class");
					pageToBeInstertedIn.vtrTuples.add(nTuple);

					// System.out.print(pageToBeInstertedIn.vtrTuples.get(0).vtrTupleObj);
					FileOutputStream f = new FileOutputStream(
							"data//" + toBeInstertedIn.usedPagesNames.get(0) + ".class");
					ObjectOutputStream bin = new ObjectOutputStream(f);
					// System.out.print(nTuple);
					bin.writeObject(pageToBeInstertedIn);
					bin.flush();
					bin.close();
					f.close();
				} else {
					Vector<String> usedPages = toBeInstertedIn.usedPagesNames;

					// System.out.println(pageToBeInstertedInIndex + " this is the index");
					boolean flag2 = false;
					int i = 0;
					for (i = 0; i < toBeInstertedIn.usedPagesNames.size(); i++) {

						if (i == toBeInstertedIn.usedPagesNames.size() - 1) {
							// System.out.println("hello1");

							Page pageToBeInstertedIn = (Page) (getDeserlaized(
									"data//" + toBeInstertedIn.usedPagesNames.get(i) + ".class"));

							Vector<Tuple> Tuples = pageToBeInstertedIn.vtrTuples;
							// System.out.println(maxPageSize);
							if (Tuples.size() < maxPageSize) {
								Tuples.add(nTuple);
								Tuples.sort(null);

								FileOutputStream f = new FileOutputStream(
										"data//" + pageToBeInstertedIn.pageName + ".class");
								ObjectOutputStream bin = new ObjectOutputStream(f);
								bin.writeObject(pageToBeInstertedIn);

								break;

							} else if (maxPageSize == Tuples.size()) {

								Tuples.add(nTuple);
								Tuples.sort(null);
								nTuple = Tuples.remove(Tuples.size() - 1);

								toBeInstertedIn.createPage();
								Page newPage = (Page) (getDeserlaized("data//"
										+ toBeInstertedIn.usedPagesNames.get(toBeInstertedIn.usedPagesNames.size() - 1)
										+ ".class"));
								newPage.vtrTuples.add(nTuple);

								FileOutputStream f = new FileOutputStream("data//" + newPage.pageName + ".class");
								ObjectOutputStream bin = new ObjectOutputStream(f);
								bin.writeObject(newPage);

								FileOutputStream f1 = new FileOutputStream(
										"data//" + pageToBeInstertedIn.pageName + ".class");
								ObjectOutputStream bin1 = new ObjectOutputStream(f1);
								bin1.writeObject(pageToBeInstertedIn);

								break;

							}
						} else {

							Page pageToBeInstertedIn = (Page) (getDeserlaized(
									"data//" + toBeInstertedIn.usedPagesNames.get(i) + ".class"));

							Vector<Tuple> Tuples = pageToBeInstertedIn.vtrTuples;
							if (Tuples.size() < maxPageSize) {
								Tuples.add(nTuple);
								Tuples.sort(null);

								FileOutputStream f = new FileOutputStream(
										"data//" + pageToBeInstertedIn.pageName + ".class");
								ObjectOutputStream bin = new ObjectOutputStream(f);
								bin.writeObject(pageToBeInstertedIn);
								flag2 = true;
								break;

							} else if (Tuples.size() == maxPageSize) {
								Tuples.add(nTuple);
								Tuples.sort(null);
								nTuple = Tuples.remove(Tuples.size() - 1);
								FileOutputStream f = new FileOutputStream(
										"data//" + pageToBeInstertedIn.pageName + ".class");
								ObjectOutputStream bin = new ObjectOutputStream(f);
								bin.writeObject(pageToBeInstertedIn);

							}
						}

					}
				}

				FileOutputStream f1 = new FileOutputStream("data//" + strTableName + ".class");
				ObjectOutputStream bin1 = new ObjectOutputStream(f1);
				bin1.writeObject(toBeInstertedIn);
				// System.out.println(toBeInstertedIn.usedPagesNames + "this are the names");

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void deleteFromTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {

		Table newTable = (Table) getDeserlaized("data//" + strTableName + ".class");
		// String type = clusteringKeyType(newTable);
		Object[] dTupleArray = getArrayToDelete(htblColNameValue, strTableName);

		// System.out.println(dTupleArray[3]);
		Vector<String> usedPages = newTable.usedPagesNames;

		// System.out.println(pageToBeInstertedInIndex + " this is the index");
		boolean flag2 = false;
		int i = 0;
		int[] compareTuple = getDeleteIndexOfArray(dTupleArray);

		for (i = 0; i < newTable.usedPagesNames.size(); i++) {

			Page pageToBeDeleteFrom = (Page) (getDeserlaized("data//" + newTable.usedPagesNames.get(i) + ".class"));

			Vector<Tuple> tuples = pageToBeDeleteFrom.vtrTuples;
			for (int j = 0; j < tuples.size(); j++) {
				Tuple t = tuples.get(j);

				boolean flag = true;
				for (int k = 0; k < compareTuple.length; k++) {
					// System.out.println(k);
					// System.out.println(dTupleArray[compareTuple[k]] +"");
					// System.out.println(t.vtrTupleObj.get(compareTuple[k]));
					//////////////////////////// deleting based on a polygon
					if (dTupleArray[compareTuple[k]] instanceof Polygon) {
						myPolygon p1 = new myPolygon((Polygon) dTupleArray[compareTuple[k]]);
						myPolygon p2 = new myPolygon((Polygon) t.vtrTupleObj.get(compareTuple[k]));
						if (!p1.equals(p2)) {
							flag = false;
							break;
						}
					}
					///////////////////////////////
					else if (!dTupleArray[compareTuple[k]].equals(t.vtrTupleObj.get(compareTuple[k]))) {
						flag = false;
						System.out.println("check6");

						break;
					}

				}
				if (flag) {

					tuples.remove(j);

					j--;
					if (tuples.size() == 0) {
						// delete page and from table
						File file = new File("data//" + newTable.usedPagesNames.get(i) + ".class");

						if (file.delete()) {

							System.out.println("File deleted successfully");
						} else {
							System.out.println("Failed to delete the file");
						}

						newTable.usedPagesNames.remove(i);
						i--;

					}
				}

			}

			if (tuples.size() != 0) {
				try {

					String n = pageToBeDeleteFrom.pageName;
					ObjectOutputStream bin = new ObjectOutputStream(new FileOutputStream("data//" + n + ".class"));

					bin.writeObject(pageToBeDeleteFrom);
					bin.flush();
					bin.close();
				} catch (Exception e) {
					throw new DBAppException("error in serializing file");
				}
			}

		}
		try {

			ObjectOutputStream bin1 = new ObjectOutputStream(new FileOutputStream("data//" + newTable.name + ".class"));

			bin1.writeObject(newTable);
			bin1.flush();
			bin1.close();
		} catch (Exception e) {
			throw new DBAppException("error in serializing file");
		}

	}

	public int[] getDeleteIndexOfArray(Object[] o) {
		int count = 0;
		for (int i = 0; i < o.length; i++) {
//			System.out.println(o[i]);
			if (!(o[i] == null)) {

				count++;
			}

		}
		int[] a = new int[count];
		int j = 0;
		for (int i = 0; i < o.length; i++) {
			if (o[i] != null) {
				a[j] = i;
				j++;

			}

		}
		return a;
	}

	// do not forget to serialize everything back

	public static boolean checkType(Hashtable<String, Object> h, String tableName) throws DBAppException {

		Enumeration type = h.keys();
		Enumeration value = h.elements();
		boolean flag = false;
		ArrayList<String> arrColoumnData = getArrayOfColoumnDataTyoe(tableName);
		try {
			while (type.hasMoreElements()) {
				String key = (String) type.nextElement();
				Object obj = value.nextElement();
				flag = false;
				for (int i = 0; i < arrColoumnData.size(); i++) {
					String[] d = arrColoumnData.get(i).split(",");
					if (d[0].equals(key)) {
						String x = "" + obj.getClass();
						if (x.contains(d[1])) {
							flag = true;
							break;
						} else {
							flag = false;
							throw new DBAppException("Wrong Data Type");
						}
					}
				}
			}
			return flag;
		} catch (Exception e) {
			throw new DBAppException("error in entered types");
		}

	}

	public static boolean checkTypeSize(Hashtable<String, Object> h, String tableName) throws DBAppException {
		try {
			Enumeration type = h.keys();
			Enumeration value = h.elements();
			boolean flag = false;
			ArrayList<String> arrColoumnData = getArrayOfColoumnDataTyoe(tableName);
			if (arrColoumnData.size() == h.size() + 1) {
				while (type.hasMoreElements()) {
					String key = (String) type.nextElement();
					Object obj = value.nextElement();
					boolean flag2 = false;
					flag = false;
					int count = 0;
					for (int i = 0; i < arrColoumnData.size(); i++) {
						String[] d = arrColoumnData.get(i).split(",");
						if (d[0].equals(key)) {
							flag2 = true;
							String x = "" + obj.getClass();
							if (x.contains(d[1])) {
								flag = true;
								break;
							} else {
								flag = false;
								throw new DBAppException("Wrong Data Type for column ");
							}
						}
						count++;
					}
					if (!flag2) {
						throw new DBAppException("Wrong column name");
					}
					if (!flag && count == arrColoumnData.size()) {
						throw new DBAppException("Column missing");
					}
				}
				return flag;

			} else {
				throw new DBAppException("A column does not exist");
			}
		} catch (Exception e) {
			throw new DBAppException("error in size or types entered");
		}
	}

	public static boolean checkType2(Hashtable<String, Object> h, String tableName) throws DBAppException {
		try {
			Enumeration type = h.keys();
			Enumeration value = h.elements();
			boolean flag = false;

			ArrayList<String> arrColoumnData = getArrayOfColoumnDataTyoe(tableName);
			while (type.hasMoreElements()) {
				boolean flag2 = false;
				String key = (String) type.nextElement();
				Object obj = value.nextElement();
				flag = false;
				int count = 0;
				for (int i = 0; i < arrColoumnData.size(); i++) {
					String[] d = arrColoumnData.get(i).split(",");
					if (d[0].equals(key)) {

						flag2 = true;
						String x = "" + obj.getClass();
						if (x.contains(d[1])) {
							System.out.println("check5");
							flag = true;
							break;
						} else {
							flag = false;
							throw new DBAppException("Wrong Data Type");
						}
					}
				}
				if (!flag2) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			throw new DBAppException("error in type");
		}
	}

	public static String getClusteringKey(String strTableName) throws DBAppException {

		try {
			BufferedReader br = new BufferedReader(new FileReader("data//metadata.csv"));
			String line;
			String result = "";
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].equals(strTableName)) {
					if (values[3].equals("true")) {
						result = values[1];
						break;
					}
				}
			}
			br.close();
			return result;
		} catch (IOException e) {
			throw new DBAppException("error in getting clustering key");
		}
	}

	public static Tuple createTuple(Hashtable<String, Object> h, String strTableName) {
		Enumeration type = h.keys();
		Enumeration value = h.elements();
		Vector<Object> tupObj = new Vector<Object>();
		int index = 0;
		try {
			String clusterKey = getClusteringKey(strTableName);
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();
			tupObj.add(formatter.format(date));
			while (type.hasMoreElements()) {
				String key = (String) type.nextElement();
				Object obj = value.nextElement();
				if (key.equals(clusterKey)) {
					index = (tupObj.size());
					tupObj.add(obj);
				} else {
					tupObj.add(obj);
				}
			}
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
		Tuple t = new Tuple(tupObj, index);
		return t;
	}

	public void updateTable(String strTableName, String strClusteringKey, Hashtable<String, Object> htblColNameValue)
			throws DBAppException {
		try {
			boolean found = checkIfTableFound(strTableName);
			if (!found) {
				System.out.print("Table does not exist ");
			} else {
				boolean checkType = checkType(htblColNameValue, strTableName);

				if (checkType) {
					// these are the col names with the same order of the tuple
					ArrayList<String> colNames = getColNames(strTableName);

					// in the tuple with the clustering key I have to get the index of the columns
					// with the same name in the hashtable
					Table toBeUpdatedIn = (Table) getDeserlaized("data//" + strTableName + ".class");

					int startPageIndex = getPageToBeInsertedIndexUsingClusteringKey(toBeUpdatedIn, strClusteringKey);
					// System.out.println(startPageIndex);

					String startPageName = toBeUpdatedIn.usedPagesNames.get(startPageIndex);
					Page startPage = (Page) getDeserlaized("data//" + startPageName + ".class");

					int clusterKeyIndex = -1;
					for (int i = 0; i < colNames.size(); i++) {
						if (colNames.get(i).equals(toBeUpdatedIn.strClusteringKeyColumn)) {
							clusterKeyIndex = i;
						}
					}

					Enumeration type = htblColNameValue.keys();
					Enumeration value = htblColNameValue.elements();
					ArrayList<String> colToBeUpdated = new ArrayList<String>();
					ArrayList<Object> valuesToBeUpdated = new ArrayList<Object>();
					boolean enough = false;

					while (type.hasMoreElements()) {
						String key = (String) type.nextElement();
						Object obj = value.nextElement();
						colToBeUpdated.add(key);
						valuesToBeUpdated.add(obj);
					}

					int indexToBeUpdated;

					boolean done = false;
					String keyType = clusteringKeyType(toBeUpdatedIn);
					Object enteredKey = new Object();

					if (keyType.equals("java.util.Date")) {

						String[] enteredArr = strClusteringKey.split("-");
						Date date = new Date(Integer.parseInt(enteredArr[0]), Integer.parseInt(enteredArr[1]),
								Integer.parseInt(enteredArr[2]));
						enteredKey = date;
					} else if (keyType.equals("java.lang.Integer")) {
						enteredKey = Integer.parseInt(strClusteringKey);
					} else if (keyType.equals("java.lang.String")) {
						enteredKey = strClusteringKey;
					} else if (keyType.equals("java.lang.Boolean")) {
						enteredKey = Boolean.parseBoolean(strClusteringKey);
					} else if (keyType.equals("java.lang.Double")) {
						enteredKey = Double.parseDouble(strClusteringKey);
					} else if (keyType.equals("java.awt.Polygon")) {

						
						String[] bracketedPoints = strClusteringKey.split("\\)");
						Polygon p = new Polygon();
						for (int i = 0; i < bracketedPoints.length; i++) {
							//System.out.println(bracketedPoints[i]);
							if ((bracketedPoints[i].charAt(0)) == ',') {
								bracketedPoints[i] = bracketedPoints[i].substring(1);
							}
							String[] points = bracketedPoints[i].split(",");
							int p1 = Integer.parseInt(points[0].substring(1));
							//System.out.println(p1);
							int p2 = Integer.parseInt(points[1]);
							//System.out.println(p2);
							p.addPoint(p1, p2);
						}
						enteredKey = p;
					}
					int startTupleIndex = getStartIndexStartUpdate(enteredKey, startPageIndex, toBeUpdatedIn);
					//System.out.println("start updating at tuple index:" + startTupleIndex);
					int tupleIndex = startTupleIndex;
					while (!enough && (tupleIndex < startPage.vtrTuples.size())) {

						Tuple old = startPage.vtrTuples.get(tupleIndex);
						String parsed;
						if (keyType.equals("java.awt.Polygon")) {
							myPolygon p = new myPolygon((Polygon) old.vtrTupleObj.get(old.index));
							parsed = p.toString();

						} else
							parsed = old.vtrTupleObj.get(old.index) + ""; // polyyy
						 //System.out.println(parsed);
						// System.out.println(parsed);
						 //System.out.println(enteredKey);
						if (parsed.equals(strClusteringKey+"")) {

							//System.out.println("check");
							for (int i = 0; i < colToBeUpdated.size(); i++) {
								String col = colToBeUpdated.get(i);
//System.out.println("loop1");
								// System.out.println(colToBeUpdated.get(i));
								for (int j = 0; j < colNames.size(); j++) {
									// System.out.println(colNames.get(j));
//System.out.println("loop2");
									if (colNames.get(j).equals(col)) {

										indexToBeUpdated = j;

										// go change the value of this index with the value in index i in
										// valuesToBeUpdated

										old.vtrTupleObj.setElementAt(valuesToBeUpdated.get(i), j);

										// tupleIndex++;
										// System.out.println(old.vtrTupleObj.get(j));
										// done = true;
										// break;
									}

									// check if the next page has the same key
									// case2:
									// check next page if the key is the same update
									// serialize back
								}
							}
							tupleIndex++;

						} else {
							enough = true;
						}

					}
					serialize(startPage);
					if (enough != true) {
						System.out.println("check53");

						startPageIndex++;
						boolean next = true;
						while (next) {
//System.out.println("loop4");
//System.out.println(startPageIndex +" less than "+ toBeUpdatedIn.usedPagesNames.size());
							while (startPageIndex < toBeUpdatedIn.usedPagesNames.size()) {
//System.out.println("loop5");
								Page nextPage = (Page) getDeserlaized(
										"data//" + toBeUpdatedIn.usedPagesNames.get(startPageIndex) + ".class");

								for (int z = 0; z < nextPage.vtrTuples.size(); z++) {
//System.out.println("loop6");
									Tuple nextTup = nextPage.vtrTuples.get(z);
									int indexKeyOfFirst = nextTup.index;
									String k;
									if (keyType.equals("java.awt.Polygon")) {
										myPolygon p = new myPolygon((Polygon) nextTup.vtrTupleObj.get(indexKeyOfFirst));
										k = p.toString();
									} else
										k = nextTup.vtrTupleObj.get(indexKeyOfFirst) + "";// polyyy
//System.out.println(k);
									if (k.equals(strClusteringKey)) {
										for (int i = 0; i < colToBeUpdated.size(); i++) {
											String col = colToBeUpdated.get(i);
											for (int j = 0; j < colNames.size(); j++) {
												if (colNames.get(j).equals(col)) {
													indexToBeUpdated = j;
													nextTup.vtrTupleObj.setElementAt(valuesToBeUpdated.get(i), j);
												}

											}
										}
									} else {

										// continue updating next tuples with the same clustering key until we hit a
										// wrong one
										next = false;
										break;

									}

								}
								startPageIndex++;
								serialize(nextPage);

							}
							if (startPageIndex == toBeUpdatedIn.usedPagesNames.size())
								next = false;
						}
					}

					ObjectOutputStream bin = new ObjectOutputStream(
							new FileOutputStream("data//" + toBeUpdatedIn.name + ".class"));
					bin.writeObject(toBeUpdatedIn);
					bin.flush();
					bin.close();

				}

				else {
					System.out.print("wrong data types");
				}

			}
		} catch (Exception e) {
			throw new DBAppException("error in updating");

		}
	}

	// serialize back everything
	public static int getStartIndexStartUpdate(Object key, int pageIndex, Table toBeInsertedIn) throws DBAppException {

		try {
			boolean flag = false;
			Page startPage = (Page) (getDeserlaized(
					"data//" + toBeInsertedIn.usedPagesNames.get(pageIndex) + ".class"));
			String keyType = clusteringKeyType(toBeInsertedIn);

			int lowerBound = 0;
			int upperBound = startPage.vtrTuples.size() - 1;
			int curIn;
			int i = 0;
			while (i >= -3) {
				i--;

				curIn = (lowerBound + upperBound) / 2;
				//System.out.println(curIn);
				Tuple testTuple = startPage.vtrTuples.get(curIn);
				Object comkey = testTuple.vtrTupleObj.get(testTuple.index);

				//System.out.println(comkey);
				//System.out.println(key);
				if (Tuple.compareToHelper(comkey, key) == 0) {
					//System.out.println("check5");
					serialize(startPage);
					flag = true;
					//to handle duplicates
					while (curIn > 0) {
						Tuple prevTuple = startPage.vtrTuples.get(curIn-1);
						Object prevkey = prevTuple.vtrTupleObj.get(prevTuple.index);
						if (prevkey.equals(key)) {
							System.out.println(prevkey);
							System.out.println(curIn);
							curIn--;
						}
						else {
							break;
						}
					}
					return curIn;
				} else if (lowerBound > upperBound) {
					throw new DBAppException("key not found");
					// return -1; // can’t find it
				}
				if (Tuple.compareToHelper(comkey, key) < 0) {
					// this means that my key is greater search down
					lowerBound = curIn + 1;
				} else {
					if (Tuple.compareToHelper(comkey, key) > 0) {
						// this means that my key is smaller search up
						upperBound = curIn - 1;
					}
				}
			}

		} catch (Exception e) {
			
			throw new DBAppException("error in getting startIndexUpdate");
		}
		return -1;

	}
//			if(a[curIn] == searchKey)
//			return curIn; // found it
//			else if(lowerBound > upperBound)
//			return nElems; // can’t find it
//			else // divide range
//			{
//			if(a[curIn] < searchKey)
//			lowerBound = curIn + 1; // it’s in upper half
//			else
//			upperBound = curIn - 1; // it’s in lower half
//			} // end else divide range
//			} // end while
//			} // end find()

//			for (int i = 0; i < startPage.vtrTuples.size(); i++) {
//
//				Tuple testTuple = startPage.vtrTuples.get(i);
//				String comkey;
//				if (keyType.equals("java.awt.Polygon")) {
//					myPolygon p = new myPolygon((Polygon) testTuple.vtrTupleObj.get(testTuple.index));
//					comkey = p.toString();
//				} else
//					comkey = testTuple.vtrTupleObj.get(testTuple.index) + ""; // polyyy
////System.out.println("comparing " + key +" with "+comkey+":"+comkey.equals(key));
//				if (comkey.equals(key)) {
//					serialize(startPage);
//					flag = true;
//					return i;
//				}
//			}

//		int upperIndex = 0;
//		int lowerIndex = middlePage.size() - 1;
//
//		while (upperIndex <= lowerIndex) {
//			int middleIndex = upperIndex + (lowerIndex - upperIndex) / 2;
//			int clusterIndex = middlePage.vtrTuples.get(middleIndex).index;
//			String middleKey = (String) middlePage.vtrTuples.get(middleIndex).get(clusterIndex);
//			if (Tuple.compareToHelper(middleKey, key) < 0) {
//				// this means that my key is greater search down
//				upperIndex = middleIndex + 1;
//			} else {
//				if (Tuple.compareToHelper(middleKey, key) > 0) {
//					// this means that my key is smaller search up
//					lowerIndex = middleIndex - 1;
//				} else {
//					return middleIndex;
//				}
//			}
//		}
//		throw new DBAppException("no clustering key with this value");
//	}

	// delete helper return an array for hashtable inserted

	public Object[] getArrayToDelete(Hashtable<String, Object> htblColNameValue, String strTableName)
			throws DBAppException {

		Table newTable = (Table) (getDeserlaized("data//" + strTableName + ".class"));

		String[] col = newTable.colNames;
		Object[] toBeReturned = new Object[col.length];

		Enumeration keys = htblColNameValue.keys();
		Enumeration values = htblColNameValue.elements();

		boolean checkType2 = checkType2(htblColNameValue, strTableName);

		if (checkType2) {

			while (keys.hasMoreElements()) {

				String key = (String) keys.nextElement();
				Object value = values.nextElement();
				// System.out.println(key);

				for (int i = 0; i < col.length; i++) {
					// System.out.println(col[i]);
					if (key.equals(col[i])) {

						// System.out.println("Print 3");
						toBeReturned[i] = value;
						// System.out.println(value +"");

					}
				}

			}
		}
		return toBeReturned;

	}

	public static void serialize(Object name) throws DBAppException {

		try {
			Page p = (Page) name;
			String n = p.pageName;
			ObjectOutputStream bin = new ObjectOutputStream(new FileOutputStream("data//" + n + ".class"));

			bin.writeObject(name);
			bin.flush();
			bin.close();
		} catch (Exception e) {
			throw new DBAppException("error in serialization");
		}
	}

	public static int getPageToBeInsertedIndexUsingClusteringKey(Table toBeInstertedIn, String clusteringKey)
			throws DBAppException {

		try {

			boolean found = false;
			int i = 0;
			String keyType = clusteringKeyType(toBeInstertedIn);
			System.out.println(keyType);
			if (keyType.equals("java.util.Date")) {
				// String entered = obj + "";
				String[] enteredArr = clusteringKey.split("-");
				Date date = new Date(Integer.parseInt(enteredArr[0]), Integer.parseInt(enteredArr[1]),
						Integer.parseInt(enteredArr[2]));

				clusteringKey = date + "";
				// String pattern = "yyyy-MM-dd";
				// SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				// Date date = simpleDateFormat.parse("" + obj);
				// obj = date;
//				System.out.println(date.getClass());
			}
			for (i = 0; i < toBeInstertedIn.usedPagesNames.size(); i++) {

				Page testPage = (Page) (getDeserlaized("data//" + toBeInstertedIn.usedPagesNames.get(i) + ".class"));

				for (int j = 0; j < testPage.vtrTuples.size(); j++) {

					Tuple testTuple = testPage.vtrTuples.get(j);
					String key;
					if (keyType.equals("java.awt.Polygon")) {
						myPolygon p = new myPolygon((Polygon) testTuple.vtrTupleObj.get(testTuple.index));
						key = p.toString();
					} else
						key = testTuple.vtrTupleObj.get(testTuple.index) + ""; // polyyy
//System.out.println("comparing " + clusteringKey +" with "+key);
					// System.out.println(key);
					// System.out.println(clusteringKey);
					// System.out.println(key.equals(clusteringKey));
					if (key.equals(clusteringKey)) {

						found = true;
						break;
						// return i;
					}
				}
				if (found == true) {
					return i;
				}
			}

		} catch (Exception e) {
			throw new DBAppException("error in getting page to be inserted in");
		}
		return -1;
	}

//		int right = toBeInstertedIn.usedPagesNames.size() - 1;
//		// System.out.println(toBeInstertedIn.usedPagesNames.size());
//		int left = 0;
//
//		while (left <= right) {
//			int middle = left + (right - left) / 2;
//
//			Page middlePage = (Page) (getDeserlaized("data//" + toBeInstertedIn.usedPagesNames.get(middle) + ".class"));
//			int x = middlePage.vtrTuples.size();
//			//System.out.println(x);
//			Tuple leftTupleMiddlePage = middlePage.vtrTuples.get(0);
//
//			int keyIndex = leftTupleMiddlePage.index;
//			 //System.out.println(keyIndex);
//
//			int y = leftTupleMiddlePage.vtrTupleObj.size();
//			//System.out.println(y);
//			String leftKey = leftTupleMiddlePage.vtrTupleObj.get(keyIndex) + "";
//			
//			Tuple rightTupleMiddlePage = middlePage.vtrTuples.get(middlePage.vtrTuples.size() - 1);
//			
//			String rightKey = rightTupleMiddlePage.vtrTupleObj.get(keyIndex) + "";
//			
//			// System.out.println(leftTupleMiddlePage.vtrTupleObj.toString() + " this is
//			// leftTupleMiddlePage");
//			// System.out.println(rightTupleMiddlePage.vtrTupleObj.toString() + " this is
//			// rightTupleMiddlePage");
//
//			if (Tuple.compareToHelper(leftKey, clusteringKey) <= 0
//					&& Tuple.compareToHelper(rightKey, clusteringKey) >= 0) {
//				return middle;
//			} else if (Tuple.compareToHelper(rightKey, clusteringKey) < 0) {
//				
//				left = middle + 1;
//			} else if (Tuple.compareToHelper(leftKey, clusteringKey) > 0) {
//				System.out.println("koky2");
//				right = middle - 1;
//			}
//
//		}

//	public static int getPageToBeInsertedInIndex(Table toBeInstertedIn, Tuple nTuple) {
//		System.out.println(nTuple);
//		int right = toBeInstertedIn.usedPagesNames.size() - 1;
//		int left = 0;
//
//		while (left <= right) {
//			int middle = left + (right - left) / 2;
//
////			Page leftPage = (Page) (getDeserlaized("data//" + toBeInstertedIn.usedPagesNames.get(left) + ".class"));
////			Tuple rightTupleLeftPage = leftPage.vtrTuples.get(leftPage.vtrTuples.size() - 1);
////			System.out.println(rightTupleLeftPage.vtrTupleObj.toString() + " this is rightTupleLeftPage");
////
////			Page rightPage = (Page) (getDeserlaized("data//" + toBeInstertedIn.usedPagesNames.get(right) + ".class"));
////			if(rightPage==null) {
////				return left+1;
////			}
////			Tuple leftTupleRightPage = rightPage.vtrTuples.get(0);
////			System.out.println(leftTupleRightPage.vtrTupleObj.toString() + " this is leftTupleRightPage");
//
//			Page middlePage = (Page) (getDeserlaized("data//" + toBeInstertedIn.usedPagesNames.get(middle) + ".class"));
//
//			Tuple leftTupleMiddlePage = middlePage.vtrTuples.get(0);
//			Tuple rightTupleMiddlePage = middlePage.vtrTuples.get(middlePage.vtrTuples.size() - 1);
//			System.out.println(leftTupleMiddlePage.vtrTupleObj.toString() + " this is leftTupleMiddlePage");
//			System.out.println(rightTupleMiddlePage.vtrTupleObj.toString() + " this is rightTupleMiddlePage");
//
//			if (leftTupleMiddlePage.compareTo(nTuple) <= 0 && rightTupleMiddlePage.compareTo(nTuple) >= 0) {
//				return middle;
//			} else if (rightTupleMiddlePage.compareTo(nTuple) < 0) {
//				left = middle + 1;
//			} else {
//				right = middle - 1;
//			}
//
//		}
//		return -1;
//	}

	public static ArrayList<String> getColNames(String strTableName) throws DBAppException {
		String csvFile = "data/metadata.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		ArrayList<String> arrColumn = new ArrayList<String>();
		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] d = line.split(cvsSplitBy);
				if (d[0].equals(strTableName)) {

					arrColumn.add(d[1]);
				}
			}
			br.close();

		} catch (Exception e) {
			throw new DBAppException("error in getting col names");
		}
		return arrColumn;

	}

	public static String clusteringKeyType(Table t) {
		String keyName = t.strClusteringKeyColumn;
		Enumeration e = t.htblColNameType.keys();
		Enumeration n = t.htblColNameType.elements();
		while (e.hasMoreElements()) {

			String key = (String) e.nextElement();
			String value = (String) n.nextElement();
			if (keyName.equals(key))
				return value;
		}
		return "";
	}

	public static void displayTableContent(String tName) throws DBAppException {
		Table t = (Table) getDeserlaized("data//" + tName + ".class");
		System.out.println("Displaying table: " + tName);
		// column names:
		ArrayList<String> col = getColNames(tName);
		for (String c : col) {
			System.out.print(c + "  ");
		}
		System.out.println();

		ArrayList<String> p = new ArrayList<String>();
		// System.out.println(t.usedPagesNames.size());
		for (int i = 0; i < t.usedPagesNames.size(); i++) {

			p.add(t.usedPagesNames.get(i));
		}
		for (int i = 0; i < p.size(); i++) {
			Page pa = (Page) getDeserlaized("data//" + p.get(i) + ".class");
			System.out.println(pa.pageName);
			for (int j = 0; j < pa.vtrTuples.size(); j++) {
				System.out.println(pa.vtrTuples.get(j).toString());
			}
			serialize(pa);
		}
		try {
			ObjectOutputStream bin = new ObjectOutputStream(new FileOutputStream("data//" + tName + ".class"));
			bin.writeObject(t);
			bin.flush();
			bin.close();

		} catch (Exception e) {
			throw new DBAppException("error in displaying data in table");
		}
	}

	public static boolean isIndexed(String tableName, String colName) throws DBAppException {
		boolean flag = false;
		boolean colFound = false;
		String csvFile = "data/metadata.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		ArrayList<String> arrColumn = new ArrayList<String>();
		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] d = line.split(cvsSplitBy);
				if (d[0].equals(tableName)) {

					if (d[1].equals(colName)) {
						colFound = true;
						if (d[4].equals("true")) {
							flag = true;
						} else {
							flag = false;
						}
					}
				}
			}
			br.close();
			if (!colFound) {
				throw new DBAppException("column not found");
			}

		} catch (Exception e) {
			throw new DBAppException("error in checking if column is indexed");
		}
		return flag;
	}

	public static void main(String[] args) throws FileNotFoundException, DBAppException, IOException {

		DBApp dbApp = new DBApp();
//		dbApp.init();
//	    System.out.println(dbApp.maxPageSize);
		String strTableName = "Student";
//**create table**
//		Hashtable<String, String> htblColNameType = new Hashtable();
//////
//		htblColNameType.put("id", "java.lang.Integer");
//////		// htblColNameType.put("adsfs", "java.lang.Long");
//		htblColNameType.put("name", "java.lang.String");
////		htblColNameType.put("date", "java.util.Date");
////////////		htblColNameType.put("gpa", "java.lang.Double");
//    		htblColNameType.put("shape", "java.awt.Polygon");
////////////		htblColNameType.put("grad", "java.lang.Boolean");
//		dbApp.createTable(strTableName, "id", htblColNameType);

//		Table a=(Table)getDeserlaized("data//Student.class");
//		System.out.println(a.colNames[0]);
//		System.out.println(a.colNames[1]);
//		System.out.println(a.colNames[2]);

		// System.out.println(dbApp.maxPageSize);
//** insert tuples**
//		for (int i = 0; i < 210; i++) {
//		Hashtable htblColNameValue = new Hashtable();
////////////
//		htblColNameValue.put("id", new Integer(392));
//		htblColNameValue.put("name", new String("Ab"));
//		htblColNameValue.put("date", new Date(2000, 11, 23));
//////		System.out.println((new Date(2020, 11, 11).getClass()));
//////		System.out.println((new Date(2020, 11, 11)).toString());
////////
////////			htblColNameValue.put("gpa", new Double(2.0));
////////		
//////////			if (i%2==0) {
////////					htblColNameValue.put("grad", true);			
//////////			}
//////////			else			htblColNameValue.put("grad", false);
//		Polygon p = new Polygon();
//		p.addPoint(1,1);
//		p.addPoint(2,2);
//////////		System.out.println("n:"+p.npoints);
//		htblColNameValue.put("shape",  p);
//////////
//		dbApp.insertIntoTable(strTableName, htblColNameValue);
//		}

//		Hashtable htblColNameValue = new Hashtable();
//		htblColNameValue.put("id", new Integer(30));
//		htblColNameValue.put("name", new String("safa"));
////		htblColNameValue.put("date", new Date(2000, 12, 23));
//		Polygon p = new Polygon();
//		p.addPoint(1,3);
//		p.addPoint(2,4);
////////////		System.out.println("n:"+p.npoints);
//		htblColNameValue.put("shape",  p);
//		dbApp.insertIntoTable(strTableName, htblColNameValue);

//**delete tuples**
//		Hashtable<String, Object> htblColNameValue = new Hashtable();
//		htblColNameValue.put("id", 392);
//////	htblColNameValue.put("name", "asdfghj");
////		// htblColNameValue.put("gpa", 2.0);
//		htblColNameValue.put("date", new Date(2000,11,23));
//////		Polygon p = new Polygon();
//////		p.addPoint(1, 1);
//////		p.addPoint(2, 2);
//////		htblColNameValue.put("shape", p);
////
//////		dbApp.insertIntoTable(strTableName, htblColNameValue);
//		dbApp.deleteFromTable(strTableName, htblColNameValue);

//		Page pageToBeDeleteFrom = (Page) (getDeserlaized(
//				"data//Student0.class"));
//		System.out.println(pageToBeDeleteFrom.vtrTuples.toString());

////**update table**
//		Hashtable hash = new Hashtable();
//		hash.put("name", new String("c"));
//////		hash.put("gpa", new Double(0.6));
//////		hash.put("date", new Date(2000-05-23));
		
//		dbApp.updateTable(strTableName, "30", hash);
////////
		displayTableContent(strTableName);
//		displayTableContent("Student");

//	Object [] a=dbApp.getArrayToDelete(htblColNameType, strTableName);
//		for (int i = 0; i < a.length; i++)
//			System.out.println(a[i]);

		// System.out.println(isIndexed("Student", "id"));

	}
}

//throw DBAexception 