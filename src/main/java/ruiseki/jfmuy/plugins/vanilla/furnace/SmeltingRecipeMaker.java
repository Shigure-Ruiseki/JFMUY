package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.IStackHelper;

public final class SmeltingRecipeMaker {

    private SmeltingRecipeMaker() {}

    public static List<SmeltingRecipe> getFurnaceRecipes(IJFMUYHelpers helpers) {
        IStackHelper stackHelper = helpers.getStackHelper();
        FurnaceRecipes furnaceRecipes = FurnaceRecipes.smelting();
        Map<ItemStack, ItemStack> smeltingMap = furnaceRecipes.getSmeltingList();

        List<SmeltingRecipe> recipes = new ArrayList<>();

        for (Map.Entry<ItemStack, ItemStack> entry : smeltingMap.entrySet()) {
            ItemStack input = entry.getKey();
            ItemStack output = entry.getValue();

            List<ItemStack> inputs = stackHelper.getSubtypes(input);

            if (inputs.isEmpty()) {
                recipes.add(new SmeltingRecipe(input, output));
            } else {
                for (ItemStack subInput : inputs) {
                    recipes.add(new SmeltingRecipe(subInput, output));
                }
            }
        }

        return recipes;
    }
}
