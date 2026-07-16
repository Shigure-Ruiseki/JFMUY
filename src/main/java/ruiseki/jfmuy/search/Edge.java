package ruiseki.jfmuy.search;

import ruiseki.jfmuy.util.Substring;

/**
 * Represents an Edge in the Suffix Tree.
 * It has a label and a destination Node
 * <p>
 * Edited by mezz:
 * - optimized with SubString
 */
public class Edge<T> extends Substring {

    private final Node<T> dest;

    public Edge(Substring subString, Node<T> dest) {
        super(subString);
        this.dest = dest;
    }

    public Node<T> getDest() {
        return dest;
    }
}
