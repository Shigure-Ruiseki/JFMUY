package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import ruiseki.jfmuy.util.StackUtil;

public class SmeltingRecipeMaker {

    @Nonnull
    public static List<SmeltingRecipe> getFurnaceRecipes() {
        Map<ItemStack, ItemStack> smeltingMap = getSmeltingMap();

        List<SmeltingRecipe> recipes = new ArrayList<>();

        for (Map.Entry<ItemStack, ItemStack> itemStackItemStackEntry : smeltingMap.entrySet()) {
            ItemStack input = itemStackItemStackEntry.getKey();
            ItemStack output = itemStackItemStackEntry.getValue();

            float experience = FurnaceRecipes.smelting()
                .func_151398_b(output);

            List<ItemStack> inputs = StackUtil.getSubtypes(input);
            SmeltingRecipe recipe = new SmeltingRecipe(inputs, output, experience);
            recipes.add(recipe);
        }

        return recipes;
    }

    @SuppressWarnings("unchecked")
    private static Map<ItemStack, ItemStack> getSmeltingMap() {
        return FurnaceRecipes.smelting()
            .getSmeltingList();
    }
}
