package ruiseki.jfmuy.api;

import ruiseki.jfmuy.api.search.ISearchIndex;
import ruiseki.jfmuy.api.search.ISearchIndexBuilder;
import ruiseki.jfmuy.api.search.ISearchIndexBuilderFactory;
import ruiseki.jfmuy.api.search.ISearchIndexFactory;

/**
 * The advanced search registry is passed to mod plugins in
 * {@link IModPlugin#registerAdvancedSearch(IAdvancedSearchRegistry)}.
 */
public interface IAdvancedSearchRegistry {

    /**
     * Replace JFMUY's default ingredient search index with a custom live index implementation.
     *
     * <p>
     * The factory must create a new empty index each time it is called. JFMUY uses the replacement for every indexed
     * ingredient field, including backing indices used by the limited-string index. The index receives both the
     * initial ingredient data and ingredients added at runtime through
     * {@link ISearchIndex#put(String, Object)}.
     * If multiple plugins replace the index, the last replacement is used.
     * </p>
     */
    void replaceIndex(ISearchIndexFactory searchIndexFactory);

    /**
     * Replace JFMUY's default ingredient search index with a custom builder implementation.
     *
     * <p>
     * This is an advanced hook for implementations that need different indexing or matching behavior than JFMUY's
     * default substring search. For example, a custom index can support language-aware matching where users type
     * phonetic, transliterated, or otherwise normalized search terms that should match localized ingredient names.
     * </p>
     *
     * <p>
     * The factory must create a new empty builder each time it is called. JFMUY uses the replacement for every indexed
     * ingredient field, including backing indices used by the limited-string index. Builders are single-use: JFMUY adds
     * the initial data, calls {@link ISearchIndexBuilder#build()}, and then uses the returned index for searches and
     * runtime additions. If multiple plugins replace the index, the last replacement is used.
     * </p>
     */
    void replaceIndexBuilder(ISearchIndexBuilderFactory searchIndexBuilderFactory);
}
