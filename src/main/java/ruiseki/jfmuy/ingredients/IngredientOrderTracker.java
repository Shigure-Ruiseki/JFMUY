package ruiseki.jfmuy.ingredients;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;

public class IngredientOrderTracker {

    private final Map<String, Integer> wildcardAddedOrder = new Object2IntOpenHashMap<>();
    private int addedIndex = 0;

    public <V> int getOrderIndex(V ingredient, IIngredientHelper<V> ingredientHelper) {
        String uid = ingredientHelper.getWildcardId(ingredient);
        if (wildcardAddedOrder.containsKey(uid)) {
            return wildcardAddedOrder.get(uid);
        } else {
            int index = addedIndex;
            wildcardAddedOrder.put(uid, index);
            addedIndex++;
            return index;
        }
    }
}
