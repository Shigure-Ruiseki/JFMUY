package ruiseki.jfmuy.search;

import java.util.Collection;
import java.util.Set;

import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.okcore.datastructure.NonNullList;

public interface IElementSearch {

    void add(IIngredientListElement<?> ingredient);

    void addAll(NonNullList<IIngredientListElement> ingredients);

    Collection<IIngredientListElement<?>> getAllIngredients();

    Set<IIngredientListElement<?>> getSearchResults(TokenInfo tokenInfo);

    @SuppressWarnings("unused") // used for debugging
    void logStatistics();

}
