package eminem;

import java.awt.Polygon;

public class Key implements Comparable {
	int k;
	
	 public Key(Object obj) {
		 k = modifyKey(obj);
	 }
	 
		public Integer modifyKey(Object key) {
			Integer modifiedKey = null;
			if (key instanceof String) {
				modifiedKey = decodeString(key.toString());
			} else if (key instanceof Integer) {
				modifiedKey = ((Integer) key).intValue();
//			} else if (key instanceof Character) {
//				modifiedKey = Character.getNumericValue((Character) key);
			} else if (key instanceof Boolean) {
				modifiedKey = ((Boolean) key) == Boolean.TRUE ? 1 : 0;
			}
			//double, polygon,date
			return modifiedKey;
		}

		public static Integer decodeString(String str) {
			int hash = 7;
			int mod = 100000007;
			for (int i = 0; i < str.length(); i++) {
				hash = (((hash * 31) % mod) + str.charAt(i)) % mod;
			}
			return hash;
		}
		public int compareTo(Key key) {
			return ((Integer)k).compareTo(key.k);
		}
		

}
