package ruiseki.jfmuy.search;

import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.okcore.datastructure.NonNullList;

public interface IBuildable {

    void start();

    void stop();

    /**
     * Bakes everything submitted so far into a searchable storage. Must be idempotent.
     */
    void build();

    void submit(IIngredientListElement<?> ingredient);

    void submitAll(NonNullList<IIngredientListElement> ingredients);

}
