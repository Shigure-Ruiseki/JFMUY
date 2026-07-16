package ruiseki.jfmuy.search;

import java.util.Collection;
import java.util.Set;

import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;

public interface IElementSearch {

    void add(IIngredientListElement<?> info);

    Collection<IIngredientListElement<?>> getAllIngredients();

    Set<IIngredientListElement<?>> getSearchResults(TokenInfo tokenInfo);

    @SuppressWarnings("unused") // used for debugging
    void logStatistics();

}
