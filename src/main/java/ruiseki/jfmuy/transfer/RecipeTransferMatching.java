package ruiseki.jfmuy.transfer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.gui.ingredients.GuiIngredient;
import ruiseki.jfmuy.util.StackUtil;

public final class RecipeTransferMatching {

    private RecipeTransferMatching() {}

    public static class MatchingItemsResult {

        @Nonnull
        public final Map<Integer, Integer> matchingItems = new HashMap<>();
        @Nonnull
        public final java.util.List<Integer> missingItems = new java.util.ArrayList<>();
    }

    @Nonnull
    public static MatchingItemsResult getMatchingItems(@Nonnull Map<Integer, ItemStack> availableItemStacks,
        @Nonnull Map<Integer, GuiIngredient<ItemStack>> ingredientsMap) {
        MatchingItemsResult matchingItemResult = new MatchingItemsResult();

        int recipeSlotNumber = -1;
        SortedSet<Integer> keys = new TreeSet<>(ingredientsMap.keySet());
        for (Integer key : keys) {
            GuiIngredient<ItemStack> ingredient = ingredientsMap.get(key);
            if (!ingredient.isInput()) {
                continue;
            }
            recipeSlotNumber++;

            List<ItemStack> requiredStacks = ingredient.getAll();
            if (requiredStacks.isEmpty()) {
                continue;
            }

            Integer matching = containsAnyStackIndexed(availableItemStacks, requiredStacks);
            if (matching == null) {
                matchingItemResult.missingItems.add(key);
            } else {
                ItemStack matchingStack = availableItemStacks.get(matching);
                matchingStack.stackSize--;
                if (matchingStack.stackSize <= 0) {
                    availableItemStacks.remove(matching);
                }
                matchingItemResult.matchingItems.put(recipeSlotNumber, matching);
            }
        }

        return matchingItemResult;
    }

    @Nullable
    private static Integer containsAnyStackIndexed(@Nonnull Map<Integer, ItemStack> stacks,
        @Nonnull Iterable<ItemStack> contains) {
        for (ItemStack containStack : contains) {
            for (Map.Entry<Integer, ItemStack> entry : stacks.entrySet()) {
                if (StackUtil.isIdentical(containStack, entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
}
