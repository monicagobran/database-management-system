package eminem;

public class BxTree<Key extends Comparable<? super Key>, Value>
{
    /** Pointer to the root node. It may be a leaf or an inner node, but it is never null. */
    private Node root;
    /** the maximum number of keys in the leaf node, M must be > 0 */
    private final int M;
    /** the maximum number of keys in inner node, the number of pointer is N+1, N must be > 2 */
    private final int N;

    /** Create a new empty tree. */
    public BxTree(int n) {
	this(n, n);
    }

    public BxTree(int m, int n) {
        M = m;
        N = n;
        root = new LNode();
    }

    public void insert(Key key, Value value) {
	System.out.println("insert key=" + key);
	Split result = root.insert(key, value);
        if (result != null) {
	    // The old root was split into two parts.
	    // We have to create a new root pointing to them
            INode _root = new INode();
            _root.num = 1;
            _root.keys[0] = result.key;
            _root.children[0] = result.left;
            _root.children[1] = result.right;
            root = _root;
        }
    }

    /** 
     * Looks for the given key. If it is not found, it returns null.
     * If it is found, it returns the associated value.
     */
    public Value find(Key key) {
        Node node = root;
        while (node instanceof INode) { // need to traverse down to the leaf
	    INode inner = (INode) node;
            int idx = inner.getLoc(key);
            node = inner.children[idx];
        }

        //We are @ leaf after while loop
        LNode leaf = (LNode) node;
        int idx = leaf.getLoc(key);
        if (idx < leaf.num && leaf.keys[idx].equals(key)) {
	    return leaf.values[idx];
        } else {
	    return null;
        }
    }

    public void dump() {
	root.dump();
    }

}