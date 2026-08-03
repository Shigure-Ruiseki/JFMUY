package ruiseki.jfmuy.search;

import ruiseki.jfmuy.search.bakedsubstring.BakedSubstringIndex;

public class BakedSubstringIndexBuilder<T> implements ISearchStorageBuilder<T> {

    private final BakedSubstringIndex.Builder<T> builder = BakedSubstringIndex.builder();

    @Override
    public void put(String key, T value) {
        builder.put(key, value);
    }

    @Override
    public ISearchStorage<T> build() {
        return new BakedSubstringIndexSearchStorage<>(builder.build());
    }

}
