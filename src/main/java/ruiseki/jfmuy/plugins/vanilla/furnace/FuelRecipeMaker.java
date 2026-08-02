package ruiseki.jfmuy.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.recipe.IStackHelper;

public final class FuelRecipeMaker {

    private FuelRecipeMaker() {}

    public static List<FuelRecipe> getFuelRecipes(IIngredientRegistry ingredientRegistry, IJFMUYHelpers helpers) {
        IStackHelper stackHelper = helpers.getStackHelper();
        IGuiHelper guiHelper = helpers.getGuiHelper();
        List<ItemStack> fuelStacks = ingredientRegistry.getFuels();
        List<FuelRecipe> recipes = new ArrayList<>();

        for (ItemStack fuelStack : fuelStacks) {
            List<ItemStack> subtypes = stackHelper.getSubtypes(fuelStack);

            if (subtypes.isEmpty()) {
                int burnTime = TileEntityFurnace.getItemBurnTime(fuelStack);
                if (burnTime > 0) {
                    recipes.add(new FuelRecipe(guiHelper, fuelStack, burnTime));
                }
            } else {
                for (ItemStack subtype : subtypes) {
                    int burnTime = TileEntityFurnace.getItemBurnTime(subtype);
                    if (burnTime > 0) {
                        recipes.add(new FuelRecipe(guiHelper, subtype, burnTime));
                    }
                }
            }
        }

        recipes.sort(Comparator.comparingInt(FuelRecipe::getBurnTime));

        return recipes;
    }
}
