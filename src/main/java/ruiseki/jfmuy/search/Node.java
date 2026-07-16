package ruiseki.jfmuy.search;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;

/**
 * Represents a node of the generalized suffix tree graph
 *
 * @see GeneralizedSuffixTree
 *      <p>
 *      Edited by mezz:
 *      - Use Java 6 features
 *      - improve performance of search by passing a set around instead of creating new ones and using addAll
 *      - only allow full searches
 *      - add nullable/nonnull annotations
 *      - formatting
 */
class Node {

    /**
     * The payload array used to store the data (indexes) associated with this node.
     * In this case, it is used to store all property indexes.
     */
    private int[] data;

    /**
     * The set of edges starting from this node
     */
    private Char2ObjectMap<Edge> edges;

    /**
     * The suffix link as described in Ukkonen's paper.
     * if str is the string denoted by the path from the root to this, this.suffix
     * is the node denoted by the path that corresponds to str without the first char.
     */
    @Nullable
    private Node suffix;

    /**
     * Creates a new Node
     */
    Node() {
        this.data = IntArrays.EMPTY_ARRAY;
        this.edges = Char2ObjectMaps.emptyMap();
        this.suffix = null;
    }

    /**
     * Gets data from the payload of both this node and its children, the string representation
     * of the path to this node is a substring of the one of the children nodes.
     */
    void getData(final IntSet ret) {
        for (int id : data) {
            ret.add(id);
        }

        for (Edge e : edges.values()) {
            e.getDest()
                .getData(ret);
        }
    }

    /**
     * Adds the given <tt>index</tt> to the set of indexes associated with <tt>this</tt>
     * returns false if this node already contains the ref
     */
    boolean addRef(int index) {
        if (contains(index)) {
            return false;
        }

        addIndex(index);

        // add this reference to all the suffixes as well
        Node iter = this.suffix;
        while (iter != null) {
            if (!iter.contains(index)) {
                iter.addIndex(index);
                iter = iter.suffix;
            } else {
                break;
            }
        }

        return true;
    }

    /**
     * Tests whether a node contains a reference to the given index.
     *
     * @param index the index to look for
     * @return true <tt>this</tt> contains a reference to index
     */
    private boolean contains(int index) {
        for (int id : data) {
            if (id == index) {
                return true;
            }
        }
        return false;
    }

    void addEdge(char ch, Edge e) {
        if (this.edges == Char2ObjectMaps.EMPTY_MAP) {
            this.edges = Char2ObjectMaps.singleton(ch, e);
        } else if (this.edges instanceof Char2ObjectMaps.Singleton) {
            Char2ObjectMap.Entry<Edge> existingEdge = edges.char2ObjectEntrySet()
                .iterator()
                .next();
            this.edges = new Char2ObjectArrayMap<>(2);
            this.edges.put(existingEdge.getCharKey(), existingEdge.getValue());
            this.edges.put(ch, e);
        } else {
            this.edges.put(ch, e);
        }
    }

    @Nullable
    Edge getEdge(char ch) {
        return edges.get(ch);
    }

    @Nullable
    Node getSuffix() {
        return suffix;
    }

    void setSuffix(Node suffix) {
        this.suffix = suffix;
    }

    private void addIndex(int index) {
        this.data = ArrayUtils.add(this.data, index);
    }

    @Override
    public String toString() {
        return "Node: size:" + data.length + " Edges: " + edges;
    }

    ObjectCollection<Edge> edges() {
        return edges.values();
    }
}
