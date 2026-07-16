package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IStackHelper;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import ruiseki.jfmuy.recipes.BrokenCraftingRecipeException;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.okcore.helper.ItemStackHelpers;

public class ShapedOreRecipeWrapper implements IShapedCraftingRecipeWrapper {

    private final IJFMUYHelpers jfmuyHelpers;
    private final ShapedOreRecipe recipe;

    public ShapedOreRecipeWrapper(IJFMUYHelpers jfmuyHelpers, ShapedOreRecipe recipe) {
        this.jfmuyHelpers = jfmuyHelpers;
        this.recipe = recipe;
    }

    @Override
    public int getWidth() {
        return recipe.width;
    }

    @Override
    public int getHeight() {
        return recipe.height;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack recipeOutput = recipe.getRecipeOutput();
        IStackHelper stackHelper = jfmuyHelpers.getStackHelper();

        List<Object> rawInputs = new ArrayList<>();
        Object[] inputs = recipe.getInput();

        if (inputs != null) {
            for (Object item : inputs) {
                if (item instanceof ItemStack stack) {
                    rawInputs.add(ItemStackHelpers.copyWithSize(stack, 1));
                } else if (item instanceof List<?>oreList) {
                    List<ItemStack> cleanedOreList = new ArrayList<>();

                    for (Object oreItem : oreList) {
                        if (oreItem instanceof ItemStack stack) {
                            cleanedOreList.add(ItemStackHelpers.copyWithSize(stack, 1));
                        }
                    }
                    rawInputs.add(cleanedOreList);
                } else {
                    rawInputs.add(item);
                }
            }
        }

        try {
            List<List<ItemStack>> inputLists = stackHelper.expandRecipeItemStackInputs(rawInputs);
            ingredients.setInputLists(VanillaTypes.ITEM, inputLists);
            ingredients.setOutput(VanillaTypes.ITEM, recipeOutput);
        } catch (RuntimeException e) {
            String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, rawInputs, recipeOutput);
            throw new BrokenCraftingRecipeException(info, e);
        }
    }
}
