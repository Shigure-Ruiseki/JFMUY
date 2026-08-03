package ruiseki.jfmuy.search;

import ruiseki.jfmuy.api.search.ISearchIndex;
import ruiseki.jfmuy.api.search.ISearchIndexBuilder;
import ruiseki.jfmuy.search.bakedsubstring.BakedSubstringIndex;

public class BakedSubstringIndexBuilder<T> implements ISearchIndexBuilder<T> {

    private final BakedSubstringIndex.Builder<T> builder = BakedSubstringIndex.builder();

    @Override
    public void put(String key, T value) {
        builder.put(key, value);
    }

    @Override
    public ISearchIndex<T> build() {
        return new BakedSubstringIndexSearchIndex<>(builder.build());
    }

}
