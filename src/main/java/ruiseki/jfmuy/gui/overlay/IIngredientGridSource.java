package ruiseki.jfmuy.gui.overlay;

import java.util.List;

import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;

public interface IIngredientGridSource {

    List<IIngredientListElement> getIngredientList();

    int size();

    void addListener(Listener listener);

    interface Listener {

        void onChange();
    }
}
