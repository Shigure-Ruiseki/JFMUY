package ruiseki.jfmuy.search;

import java.util.Collection;

import it.unimi.dsi.fastutil.ints.IntSet;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;

public class PrefixedSearchable<T extends ISearchable> implements ISearchable {

    private final T searchable;
    private final PrefixInfo prefixInfo;

    public PrefixedSearchable(T searchable, PrefixInfo prefixInfo) {
        this.searchable = searchable;
        this.prefixInfo = prefixInfo;
    }

    public T getSearchable() {
        return searchable;
    }

    public Collection<String> getStrings(IIngredientListElement<?> element) {
        return prefixInfo.getStrings(element);
    }

    @Override
    public Config.SearchMode getMode() {
        return prefixInfo.getMode();
    }

    @Override
    public IntSet search(String word) {
        return searchable.search(word);
    }
}
