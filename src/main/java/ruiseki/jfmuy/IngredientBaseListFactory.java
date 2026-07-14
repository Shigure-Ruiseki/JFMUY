package ruiseki.jfmuy;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableList;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.util.IngredientListElement;
import ruiseki.jfmuy.util.Log;

public class IngredientBaseListFactory {

    private IngredientBaseListFactory() {

    }

    public static ImmutableList<IIngredientListElement> create() {
        Log.info("Building item filter...");
        long start_time = System.currentTimeMillis();

        IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
        JFMUYHelpers jfmuyHelpers = Internal.getHelpers();
        IngredientChecker ingredientChecker = new IngredientChecker(jfmuyHelpers);

        List<IIngredientListElement> ingredientListElements = new LinkedList<>();

        for (Class ingredientClass : ingredientRegistry.getRegisteredIngredientClasses()) {
            addToBaseList(ingredientListElements, ingredientRegistry, ingredientChecker, ingredientClass);
        }

        sortIngredientListElements(ingredientListElements);
        ImmutableList<IIngredientListElement> immutableElements = ImmutableList.copyOf(ingredientListElements);

        Log.info("Built	item filter in {} ms", System.currentTimeMillis() - start_time);
        return immutableElements;
    }

    private static <V> void addToBaseList(List<IIngredientListElement> baseList, IIngredientRegistry ingredientRegistry,
        IngredientChecker ingredientChecker, Class<V> ingredientClass) {
        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
        IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientClass);

        ImmutableList<V> ingredients = ingredientRegistry.getIngredients(ingredientClass);
        for (V ingredient : ingredients) {
            if (ingredient != null) {
                if (!ingredientChecker.isIngredientHidden(ingredient, ingredientHelper)) {
                    IIngredientListElement<V> ingredientListElement = IngredientListElement
                        .create(ingredient, ingredientHelper, ingredientRenderer);
                    if (ingredientListElement != null) {
                        baseList.add(ingredientListElement);
                    }
                }
            }
        }
    }

    private static void sortIngredientListElements(List<IIngredientListElement> ingredientListElements) {
        int index = 0;
        final Map<String, Integer> itemAddedOrder = new HashMap<>();
        for (IIngredientListElement ingredientListElement : ingredientListElements) {
            String uid = getWildcardUid(ingredientListElement);
            if (!itemAddedOrder.containsKey(uid)) {
                itemAddedOrder.put(uid, index);
                index++;
            }
        }

        Collections.sort(ingredientListElements, new Comparator<IIngredientListElement>() {

            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
                final String modName1 = getModName(o1);
                final String modName2 = getModName(o2);

                if (modName1.equals(modName2)) {
                    boolean isItemStack1 = (o1.getIngredient() instanceof ItemStack);
                    boolean isItemStack2 = (o2.getIngredient() instanceof ItemStack);
                    if (isItemStack1 && !isItemStack2) {
                        return -1;
                    } else if (!isItemStack1 && isItemStack2) {
                        return 1;
                    }

                    final String uid1 = getWildcardUid(o1);
                    final String uid2 = getWildcardUid(o2);

                    final int orderIndex1 = itemAddedOrder.get(uid1);
                    final int orderIndex2 = itemAddedOrder.get(uid2);
                    return Integer.compare(orderIndex1, orderIndex2);
                } else if (modName1.equals(Reference.MINECRAFT_MOD_NAME)) {
                    return -1;
                } else if (modName2.equals(Reference.MINECRAFT_MOD_NAME)) {
                    return 1;
                } else {
                    return modName1.compareTo(modName2);
                }
            }
        });
    }

    private static <V> String getModName(IIngredientListElement<V> ingredientListElement) {
        V ingredient = ingredientListElement.getIngredient();
        IIngredientHelper<V> ingredientHelper = ingredientListElement.getIngredientHelper();
        String modId = ingredientHelper.getModId(ingredient);
        return Internal.getModIdUtil()
            .getModNameForModId(modId);
    }

    private static <V> String getWildcardUid(IIngredientListElement<V> ingredientListElement) {
        V ingredient = ingredientListElement.getIngredient();
        IIngredientHelper<V> ingredientHelper = ingredientListElement.getIngredientHelper();
        return ingredientHelper.getWildcardId(ingredient);
    }

    private static class IngredientChecker {

        private final IngredientBlacklist ingredientBlacklist;

        public IngredientChecker(JFMUYHelpers jfmuyHelpers) {
            ingredientBlacklist = jfmuyHelpers.getIngredientBlacklist();
        }

        public <V> boolean isIngredientHidden(V ingredient, IIngredientHelper<V> ingredientHelper) {
            try {
                if (ingredientBlacklist.isIngredientBlacklistedByApi(ingredient)) {
                    return true;
                }

                if (!Config.isEditModeEnabled() && Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
                    return true;
                }
            } catch (RuntimeException e) {
                String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
                Log.error("Could not check blacklist for ingredient {}", ingredientInfo, e);
                return true;
            }

            return false;
        }
    }
}
