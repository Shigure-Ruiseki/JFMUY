package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.IStackHelper;

public class SmeltingRecipeMaker {

    @Nonnull
    public static List<SmeltingRecipe> getFurnaceRecipes(IJFMUYHelpers helpers) {
        IStackHelper stackHelper = helpers.getStackHelper();
        FurnaceRecipes furnaceRecipes = FurnaceRecipes.smelting();
        Map<ItemStack, ItemStack> smeltingMap = furnaceRecipes.getSmeltingList();

        List<SmeltingRecipe> recipes = new ArrayList<>();

        for (Map.Entry<ItemStack, ItemStack> itemStackItemStackEntry : smeltingMap.entrySet()) {
            ItemStack input = itemStackItemStackEntry.getKey();
            ItemStack output = itemStackItemStackEntry.getValue();

            float experience = furnaceRecipes.func_151398_b(output);

            List<ItemStack> inputs = stackHelper.getSubtypes(input);
            SmeltingRecipe recipe = new SmeltingRecipe(inputs, output, experience);
            recipes.add(recipe);
        }

        return recipes;
    }
}
