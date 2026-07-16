package ruiseki.jfmuy.search;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntSet;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;

public interface IElementSearch {

    <V> void add(IIngredientListElement<V> info);

    <V> IIngredientListElement<V> get(int index);

    <V> int indexOf(IIngredientListElement<V> ingredient);

    int size();

    List<IIngredientListElement<?>> getAllIngredients();

    @Nullable
    IntSet getSearchResults(String token, PrefixInfo prefixInfo);

    void registerPrefix(PrefixInfo prefixInfo);

    void start();

}
