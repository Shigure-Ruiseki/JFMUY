package ruiseki.jfmuy.search;

/**
 * Represents an Edge in the Suffix Tree.
 * It has a label and a destination Node
 * <p>
 * Edited by mezz:
 * - formatting
 */
class Edge {

    private String label;
    private final Node dest;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Node getDest() {
        return dest;
    }

    public Edge(String label, Node dest) {
        this.label = label;
        this.dest = dest;
    }

    @Override
    public String toString() {
        return "Edge: " + label;
    }
}
