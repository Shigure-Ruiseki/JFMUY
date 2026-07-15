package ruiseki.jfmuy.plugins.okcore.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import ruiseki.okcore.json.item.CompoundItemMaterial;
import ruiseki.okcore.recipe.type.crafting.shaped.ShapedRecipe;

public class ShapedRecipeWrapper implements IShapedCraftingRecipeWrapper {

    private final ShapedRecipe recipe;
    private final List<List<ItemStack>> inputs;

    public ShapedRecipeWrapper(IJFMUYHelpers jeiHelpers, ShapedRecipe recipe) {
        this.recipe = recipe;
        this.inputs = new ArrayList<>();

        int width = recipe.getRecipeWidth();
        int height = recipe.getRecipeHeight();
        String[] pattern = recipe.getPattern();
        Map<Character, CompoundItemMaterial> keyMap = recipe.getKeyMap();

        for (int i = 0; i < 9; i++) {
            inputs.add(new ArrayList<>());
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < pattern[i].length(); j++) {
                char chr = pattern[i].charAt(j);
                CompoundItemMaterial materials = keyMap.get(chr);
                if (materials != null && !materials.isEmpty()) {
                    int slotIndex = j + i * 3;
                    if (slotIndex < 9) {
                        inputs.set(slotIndex, materials.toStacks());
                    }
                }
            }
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
    }

    @Override
    public int getWidth() {
        return recipe.getRecipeWidth();
    }

    @Override
    public int getHeight() {
        return recipe.getRecipeHeight();
    }
}
