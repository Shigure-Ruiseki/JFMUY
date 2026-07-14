package ruiseki.jfmuy.gui.ingredients;

import java.util.Collection;

import javax.annotation.Nonnull;

import ruiseki.jfmuy.gui.Focus;

public interface IIngredientHelper<T> {

    Collection<T> expandSubtypes(Collection<T> contained);

    T getMatch(Iterable<T> ingredients, @Nonnull Focus toMatch);

    @Nonnull
    Focus createFocus(@Nonnull T ingredient);
}
