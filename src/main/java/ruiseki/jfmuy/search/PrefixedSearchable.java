package ruiseki.jfmuy.search;

import java.util.Collection;
import java.util.Set;

import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;

public class PrefixedSearchable implements ISearchable<IIngredientListElement<?>> {

    private final ISearchStorage<IIngredientListElement<?>> searchStorage;
    private final PrefixInfo prefixInfo;

    public PrefixedSearchable(ISearchStorage<IIngredientListElement<?>> searchStorage, PrefixInfo prefixInfo) {
        this.searchStorage = searchStorage;
        this.prefixInfo = prefixInfo;
    }

    public ISearchStorage<IIngredientListElement<?>> getSearchStorage() {
        return searchStorage;
    }

    public Collection<String> getStrings(IIngredientListElement<?> element) {
        return prefixInfo.getStrings(element);
    }

    @Override
    public Config.SearchMode getMode() {
        return prefixInfo.getMode();
    }

    @Override
    public void getSearchResults(String token, Set<IIngredientListElement<?>> results) {
        searchStorage.getSearchResults(token, results);
    }

    @Override
    public void getAllElements(Set<IIngredientListElement<?>> results) {
        searchStorage.getAllElements(results);
    }
}
