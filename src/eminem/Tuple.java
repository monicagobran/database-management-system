package eminem;

import java.awt.Polygon;
import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

public class Tuple extends Vector implements Serializable, Comparable<Tuple> {

	public int index;
	public Vector<Object> vtrTupleObj;

	public Tuple(Vector<Object> columns, int index) {
		this.index = index;
		vtrTupleObj = new Vector<Object>();

		for (int i = 0; i < columns.size(); i++) {
			this.vtrTupleObj.add(columns.get(i));

		}

	}

	public String toString() {
		String s = "";
		for (int i = 0; i < vtrTupleObj.size(); i++) {
			if (vtrTupleObj.get(i) instanceof Polygon) {
//				for (int j = 0; j < ((Polygon) (vtrTupleObj.get(i))).npoints; j++) {
//					s += "(" + ((Polygon) (vtrTupleObj.get(i))).xpoints[j] + ","
//							+ ((Polygon) (vtrTupleObj.get(i))).ypoints[j] + "),";
//				}
				myPolygon p = new myPolygon((Polygon) vtrTupleObj.get(i));
				s += p + ",";
			} else
				s += vtrTupleObj.get(i) + ",";
		}
		return s.substring(0, s.length() - 1);
	}

	public int compareTo(Tuple t2) {
		Object O1 = this.vtrTupleObj.get(index);
		Object O2 = t2.vtrTupleObj.get(index);
		int x = 0;
		try {
			x = compareToHelper(O1, O2);
			return x;
		} catch (DBAppException e) {
			System.out.println(e.getMessage());
			return x;
		}

	}

	public static int compareToHelper(Object one, Object two) throws DBAppException {
		// Object O1 = this.vtrTupleObj.get(index);
		Object O1 = one;
//		Object O2 = t2.vtrTupleObj.get(index);
		Object O2 = two;
		if (O1 instanceof java.lang.String) {

			String s1 = (String) O1;
			String s2 = (String) O2;
			// System.out.println("string");
			return s1.compareTo(s2);

		} else if (O1 instanceof java.lang.Integer) {
			int i1 = (int) O1;
			int i2 = (int) O2;
			// System.out.println(i1 + " int" + i2);
			return i1 - i2;

		} else if (O1 instanceof java.lang.Double) {
			Double do1 = (Double) O1;
			Double do2 = (Double) O2;
			// System.out.println("doyble");
			return do1.compareTo(do2);

		} else if (O1 instanceof java.util.Date) {
			java.util.Date da1 = (java.util.Date) O1;
			java.util.Date da2 = (java.util.Date) O2;
			// System.out.println("date");
			return da1.compareTo(da2);

		} else if (O1 instanceof java.lang.Boolean) {
			Boolean da1 = (Boolean) O1;
			Boolean da2 = (Boolean) O2;
			// System.out.println("date");
			return da1.compareTo(da2);		
		} else if (O1 instanceof Polygon) {
			myPolygon p1 = new myPolygon((Polygon) O1);
			myPolygon p2 = new myPolygon((Polygon) O2);
			return p1.compareTo(p2);
		} else {
			throw new DBAppException("Type entered is not one of the allowed");
//			// System.out.println("I am not an int");
//			return 0;
		}
	}
	// public static void main(String []args){
	//
	// Vector<Object> vtrTupleObj1 = new Vector<Object>();
	// Date a = new Date(2014, 02, 11);
	// Date b = new Date(2014, 02, 11);
	// vtrTupleObj1.add(a);
	// vtrTupleObj1.add(b) ;
	// Vector<Object> vtrTupleObj2 = new Vector<Object>();
	// Date a1 = new Date(2014, 02, 11);
	// Date b1 = new Date(2010, 02, 11);
	// vtrTupleObj2.add(a1);
	// vtrTupleObj2.add(b1);
	// int index = 1;
	// Tuple t1 = new Tuple (vtrTupleObj1 , index) ;
	// Tuple t2 = new Tuple (vtrTupleObj2 , index) ;
	// System.out.print(t1.compareTo(t2));
	//
	//
	//
	//
	//
	// }
}