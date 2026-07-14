package ruiseki.jfmuy.gui.ingredients;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;

public interface IIngredientListElement<V> {

    V getIngredient();

    IIngredientHelper<V> getIngredientHelper();

    String getSearchString();

    String getModNameString();

    String getTooltipString();

    String getOreDictString();

    String getCreativeTabsString();

    String getColorString();
}
