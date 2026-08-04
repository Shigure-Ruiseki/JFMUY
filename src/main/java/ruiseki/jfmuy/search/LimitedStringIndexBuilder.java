package ruiseki.jfmuy.search;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import ruiseki.jfmuy.api.search.ISearchIndex;
import ruiseki.jfmuy.api.search.ISearchIndexBuilder;
import ruiseki.jfmuy.api.search.ISearchIndexBuilderFactory;
import ruiseki.jfmuy.collect.SetMultiMap;

public class LimitedStringIndexBuilder<T> implements ISearchIndexBuilder<T> {

    private final SetMultiMap<String, T> multiMap = new SetMultiMap<>(ReferenceOpenHashSet::new);
    private final ISearchIndexBuilder<Set<T>> backingIndexBuilder;

    public LimitedStringIndexBuilder(ISearchIndexBuilderFactory factory, String id) {
        this.backingIndexBuilder = factory.create(id);
    }

    @Override
    public void put(String key, T value) {
        boolean isNewKey = !multiMap.containsKey(key);
        multiMap.put(key, value);
        if (isNewKey) {
            backingIndexBuilder.put(key, multiMap.get(key));
        }
    }

    @Override
    public ISearchIndex<T> build() {
        return new LimitedStringIndex<>(backingIndexBuilder.build(), multiMap);
    }
}
