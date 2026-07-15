package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.IStackHelper;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

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
            // noinspection ConstantConditions
            if (input == null || output == null || input.getItem() == null || output.getItem() == null) {
                Log.get()
                    .error(
                        "Found invalid smelting recipe: ({} -> {})",
                        ErrorUtil.getItemStackInfo(input),
                        ErrorUtil.getItemStackInfo(output));
            } else {
                List<ItemStack> inputs = stackHelper.getSubtypes(input);
                SmeltingRecipe recipe = new SmeltingRecipe(inputs, output);
                recipes.add(recipe);
            }
        }

        return recipes;
    }

}
