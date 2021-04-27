package eminem;

abstract class Node {
	protected int num; //number of keys
	protected Key[] keys;

	abstract public int getLoc(Key key);
	// returns null if no split, otherwise returns split info

	abstract public void dump();
	abstract public Split insert(Key key, Value value);
    }
