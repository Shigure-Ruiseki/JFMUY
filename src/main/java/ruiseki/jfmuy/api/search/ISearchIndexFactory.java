package ruiseki.jfmuy.api.search;

/**
 * Creates live indices for JFMUY's ingredient search.
 */
@FunctionalInterface
public interface ISearchIndexFactory {

    /**
     * Create a new empty search index.
     *
     * @param <T> the type of values stored in the search index
     */
    <T> ISearchIndex<T> createSearchIndex();
}
