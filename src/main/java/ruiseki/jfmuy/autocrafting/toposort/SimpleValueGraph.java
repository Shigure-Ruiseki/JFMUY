package ruiseki.jfmuy.autocrafting.toposort;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class SimpleValueGraph<N, V> {

    private final boolean isDirected;
    private final boolean allowsSelfLoops;
    private final Map<N, Map<N, V>> successorMap = new LinkedHashMap<>();
    private final Map<N, Map<N, V>> predecessorMap = new LinkedHashMap<>();

    private SimpleValueGraph(boolean isDirected, boolean allowsSelfLoops) {
        this.isDirected = isDirected;
        this.allowsSelfLoops = allowsSelfLoops;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public boolean allowsSelfLoops() {
        return allowsSelfLoops;
    }

    public Set<N> nodes() {
        return Collections.unmodifiableSet(successorMap.keySet());
    }

    public boolean addNode(N node) {
        if (successorMap.containsKey(node)) {
            return false;
        }
        successorMap.put(node, new LinkedHashMap<>());
        predecessorMap.put(node, new LinkedHashMap<>());
        return true;
    }

    public V putEdgeValue(N nodeU, N nodeV, V value) {
        if (!allowsSelfLoops && Objects.equals(nodeU, nodeV)) {
            throw new IllegalArgumentException("Self-loops are not allowed!");
        }
        addNode(nodeU);
        addNode(nodeV);

        V previousValue = successorMap.get(nodeU)
            .put(nodeV, value);
        predecessorMap.get(nodeV)
            .put(nodeU, value);
        return previousValue;
    }

    public Set<N> successors(N node) {
        Map<N, V> successors = successorMap.get(node);
        return successors != null ? Collections.unmodifiableSet(successors.keySet()) : Collections.emptySet();
    }

    public Set<N> predecessors(N node) {
        Map<N, V> predecessors = predecessorMap.get(node);
        return predecessors != null ? Collections.unmodifiableSet(predecessors.keySet()) : Collections.emptySet();
    }

    public Optional<V> edgeValue(N nodeU, N nodeV) {
        Map<N, V> successors = successorMap.get(nodeU);
        if (successors != null) {
            V value = successors.get(nodeV);
            if (value != null) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    // Phương thức mới được thêm vào ở đây
    public V edgeValueOrDefault(N nodeU, N nodeV, V defaultValue) {
        Map<N, V> successors = successorMap.get(nodeU);
        if (successors != null) {
            V value = successors.get(nodeV);
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
    }

    public int inDegree(N node) {
        Map<N, V> predecessors = predecessorMap.get(node);
        return predecessors != null ? predecessors.size() : 0;
    }

    public boolean removeNode(N node) {
        if (!successorMap.containsKey(node)) {
            return false;
        }
        for (N successor : successors(node)) {
            predecessorMap.get(successor)
                .remove(node);
        }
        successorMap.remove(node);

        for (N predecessor : predecessors(node)) {
            successorMap.get(predecessor)
                .remove(node);
        }
        predecessorMap.remove(node);

        return true;
    }

    public static Builder directed() {
        return new Builder(true);
    }

    public static class Builder {

        private final boolean directed;
        private boolean allowsSelfLoops = false;

        Builder(boolean directed) {
            this.directed = directed;
        }

        public Builder allowsSelfLoops(boolean allowsSelfLoops) {
            this.allowsSelfLoops = allowsSelfLoops;
            return this;
        }

        public Builder nodeOrder(Object unused) {
            return this;
        }

        public Builder expectedNodeCount(int unused) {
            return this;
        }

        public <N, V> SimpleValueGraph<N, V> build() {
            return new SimpleValueGraph<>(directed, allowsSelfLoops);
        }
    }
}
