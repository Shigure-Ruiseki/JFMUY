package ruiseki.jfmuy.runtime;

import javax.annotation.Nullable;

import ruiseki.jfmuy.api.IAdvancedSearchRegistry;
import ruiseki.jfmuy.api.search.ISearchIndexBuilder;
import ruiseki.jfmuy.api.search.ISearchIndexBuilderFactory;
import ruiseki.jfmuy.api.search.ISearchIndexFactory;
import ruiseki.jfmuy.search.SearchIndexBuilder;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public class AdvancedSearchRegistry implements IAdvancedSearchRegistry {

    private final ISearchIndexBuilderFactory defaultIndexBuilderFactory;

    @Nullable
    private ISearchIndexBuilderFactory indexBuilderFactoryOverride;

    public AdvancedSearchRegistry(ISearchIndexBuilderFactory defaultIndexBuilderFactory) {
        this.defaultIndexBuilderFactory = ErrorUtil
            .checkNotNull(defaultIndexBuilderFactory, "defaultIndexBuilderFactory");
    }

    @Override
    public void replaceIndex(ISearchIndexFactory searchIndexFactory) {
        ErrorUtil.checkNotNull(searchIndexFactory, "searchIndexFactory");

        Log.get()
            .info("Replaced search index factory: {}", searchIndexFactory);
        this.indexBuilderFactoryOverride = new ISearchIndexBuilderFactory() {

            @Override
            public <T> ISearchIndexBuilder<T> create() {
                return new SearchIndexBuilder<>(searchIndexFactory.createSearchIndex());
            }
        };
    }

    @Override
    public void replaceIndexBuilder(ISearchIndexBuilderFactory searchIndexBuilderFactory) {
        ErrorUtil.checkNotNull(searchIndexBuilderFactory, "searchIndexBuilderFactory");

        Log.get()
            .info("Replaced search index builder factory: {}", searchIndexBuilderFactory);
        this.indexBuilderFactoryOverride = searchIndexBuilderFactory;
    }

    public ISearchIndexBuilderFactory getSearchIndexBuilderFactory() {
        return indexBuilderFactoryOverride == null ? defaultIndexBuilderFactory : indexBuilderFactoryOverride;
    }
}
