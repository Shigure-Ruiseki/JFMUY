package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.IStackHelper;

public final class SmeltingRecipeMaker {

    private SmeltingRecipeMaker() {}

    public static List<SmeltingRecipe> getFurnaceRecipes(IJFMUYHelpers helpers) {
        IStackHelper stackHelper = helpers.getStackHelper();
        FurnaceRecipes furnaceRecipes = FurnaceRecipes.smelting();
        Map<ItemStack, ItemStack> smeltingMap = furnaceRecipes.getSmeltingList();

        Map<ItemStack, List<ItemStack>> outputMap = new Object2ObjectOpenCustomHashMap<>(
            smeltingMap.size(),
            new Hash.Strategy<ItemStack>() {

                @Override
                public int hashCode(ItemStack o) {
                    if (o == null || o.getItem() == null) return 0;
                    int result = o.getItem()
                        .hashCode();
                    result = 31 * result + o.getItemDamage();
                    result = 31 * result + o.stackSize;
                    return result;
                }

                @Override
                public boolean equals(ItemStack a, ItemStack b) {
                    if (a == b) return true;
                    if (a == null || b == null) return false;
                    return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
                        && a.stackSize == b.stackSize;
                }
            });
        List<SmeltingRecipe> recipes = new ArrayList<>(smeltingMap.size());

        for (Map.Entry<ItemStack, ItemStack> entry : smeltingMap.entrySet()) {
            ItemStack input = entry.getKey();
            ItemStack output = entry.getValue();
            outputMap.computeIfAbsent(output, k -> new ArrayList<>())
                .addAll(stackHelper.getSubtypes(input));
        }

        for (Map.Entry<ItemStack, List<ItemStack>> entry : outputMap.entrySet()) {
            recipes.add(new SmeltingRecipe(entry.getValue(), entry.getKey()));
        }

        return recipes;
    }

}
