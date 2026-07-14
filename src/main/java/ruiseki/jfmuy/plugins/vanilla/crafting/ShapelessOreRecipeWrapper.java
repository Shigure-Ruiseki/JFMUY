package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.IStackHelper;
import ruiseki.jfmuy.util.BrokenCraftingRecipeException;
import ruiseki.jfmuy.util.ErrorUtil;

public class ShapelessOreRecipeWrapper extends AbstractShapelessRecipeWrapper {

    private final IJFMUYHelpers jfmuyHelpers;
    private final ShapelessOreRecipe recipe;

    public ShapelessOreRecipeWrapper(IJFMUYHelpers jfmuyHelpers, ShapelessOreRecipe recipe) {
        super(jfmuyHelpers.getGuiHelper());
        this.jfmuyHelpers = jfmuyHelpers;
        this.recipe = recipe;
        for (Object input : this.recipe.getInput()) {
            if (input instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) input;
                if (itemStack.stackSize != 1) {
                    itemStack.stackSize = 1;
                }
            }
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        IStackHelper stackHelper = jfmuyHelpers.getStackHelper();
        ItemStack recipeOutput = recipe.getRecipeOutput();

        try {
            List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(recipe.getInput());
            ingredients.setInputLists(ItemStack.class, inputs);

            if (recipeOutput != null) {
                ingredients.setOutput(ItemStack.class, recipeOutput);
            }
        } catch (RuntimeException e) {
            String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.getInput(), recipeOutput);
            throw new BrokenCraftingRecipeException(info, e);
        }
    }
}
