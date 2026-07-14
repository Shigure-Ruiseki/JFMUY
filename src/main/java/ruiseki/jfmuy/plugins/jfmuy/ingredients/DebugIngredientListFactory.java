package ruiseki.jfmuy.plugins.jfmuy.ingredients;

import java.util.ArrayList;
import java.util.List;

public class DebugIngredientListFactory {

    public static List<DebugIngredient> create() {
        List<DebugIngredient> ingredients = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DebugIngredient debugIngredient = new DebugIngredient(i);
            ingredients.add(debugIngredient);
        }
        return ingredients;
    }
}
