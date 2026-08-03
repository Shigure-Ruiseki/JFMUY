package ruiseki.jfmuy.api.search;

/**
 * Builds an index used by JFMUY's ingredient search.
 *
 * <p>
 * JFMUY adds the initial ingredient data before calling {@link #build()}. This lets implementations preprocess or bake
 * the initial index before searching begins. The returned {@link ISearchIndex} is also used for ingredients added at
 * runtime.
 * </p>
 *
 * @param <T> the type of values stored in the search index
 */
public interface ISearchIndexBuilder<T> {

    /**
     * Add a value to the initial search index.
     *
     * @param key   the searchable string
     * @param value the indexed value
     */
    void put(String key, T value);

    /**
     * Create the final search index from the values added to this builder.
     *
     * <p>
     * The returned index must support {@link ISearchIndex#put(String, Object)} for ingredients added after JFMUY's
     * initial
     * index has been built.
     * </p>
     */
    ISearchIndex<T> build();
}
