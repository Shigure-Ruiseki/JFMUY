package ruiseki.jfmuy.plugins.okcore.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.wrapper.ICraftingRecipeWrapper;
import ruiseki.okcore.json.item.CompoundItemMaterial;
import ruiseki.okcore.recipe.type.crafting.shapless.ShapelessRecipe;

public class ShapelessRecipeWrapper implements ICraftingRecipeWrapper {

    private final ShapelessRecipe recipe;
    private final List<List<ItemStack>> inputs;

    public ShapelessRecipeWrapper(ShapelessRecipe recipe) {
        this.recipe = recipe;
        this.inputs = new ArrayList<>();

        for (CompoundItemMaterial ingredient : recipe.getIngredients()) {
            if (ingredient != null && !ingredient.isEmpty()) {
                inputs.add(ingredient.toStacks());
            }
        }
    }

    public ShapelessRecipe getRawRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(ruiseki.jfmuy.api.ingredients.IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
    }

    @Override
    public @Nullable ResourceLocation getRegistryName() {
        return recipe.getId();
    }
}
