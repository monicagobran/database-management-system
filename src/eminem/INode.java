package eminem;


class INode extends Node {
	private static final int N = 15;
	final Node[] children = new Node[N+1];
	{ keys = (Key[]) new Comparable[N]; }

	/**
	 * Returns the position where 'key' should be inserted in an inner node
	 * that has the given keys.
	 */
	public int getLoc(Key key) {
	    // Simple linear search. Faster for small values of N or M
	    for (int i = 0; i < num; i++) {
		if (keys[i].compareTo(key) > 0) {
		    return i;
		}
	    }
	    return num;
	    // Binary search is faster when N or M is big,
	}

	public Split insert(Key key, Value value) {
	    /* Early split if node is full.
	     * This is not the canonical algorithm for B+ trees,
	     * but it is simpler and it does break the definition
	     * which might result in immature split, which might not be desired in database
	     * because additional split lead to tree's height increase by 1, thus the number of disk read
	     * so first search to the leaf, and split from bottom up is the correct approach.
	     */

	    if (this.num == N) { // Split
		int mid = (N+1)/2;
		int sNum = this.num - mid;
		INode sibling = new INode();
		sibling.num = sNum;
		System.arraycopy(this.keys, mid, sibling.keys, 0, sNum);
		System.arraycopy(this.children, mid, sibling.children, 0, sNum+1);

		this.num = mid-1;//this is important, so the middle one elevate to next depth(height), inner node's key don't repeat itself

		// Set up the return variable
		Split result = new Split(this.keys[mid-1],
					 this,
					 sibling);

		// Now insert in the appropriate sibling
		if (key.compareTo(result.key) < 0) {
		    this.insertNonfull(key, value);
		} else {
		    sibling.insertNonfull(key, value);
		}
		return result;

	    } else {// No split
		this.insertNonfull(key, value);
		return null;
	    }
	}

	private void insertNonfull(Key key, Value value) {
	    // Simple linear search
	    int idx = getLoc(key);
	    Split result = children[idx].insert(key, value);

	    if (result != null) {
		if (idx == num) {
		    // Insertion at the rightmost key
		    keys[idx] = result.key;
		    children[idx] = result.left;
		    children[idx+1] = result.right;
		    num++;
		} else {
		    // Insertion not at the rightmost key
		    //shift i>idx to the right
		    System.arraycopy(keys, idx, keys, idx+1, num-idx);
		    System.arraycopy(children, idx, children, idx+1, num-idx+1);

		    children[idx] = result.left;
		    children[idx+1] = result.right;
		    keys[idx] = result.key;
		    num++;
		}
	    } // else the current node is not affected
	}

	/**
	 * This one only dump integer key
	 */
	public void dump() {
	    System.out.println("iNode h==?");
	    for (int i = 0; i < num; i++){
		children[i].dump();
		System.out.print('>');
		System.out.println(keys[i]);
	    }
	    children[num].dump();
	}
    }