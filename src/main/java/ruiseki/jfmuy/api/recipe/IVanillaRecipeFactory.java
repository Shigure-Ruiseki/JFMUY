package ruiseki.jfmuy.api.recipe;

import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModRegistry;

/**
 * Allows creation of vanilla recipes.
 * Get the instance from {@link IJFMUYHelpers#getStackHelper()}.
 * <p>
 * Use {@link IModRegistry#addRecipes(Collection, String)} to add the recipe.
 */
public interface IVanillaRecipeFactory {

    /**
     * Create an anvil recipe for the given inputs and output.
     *
     * @param leftInput   The itemStack placed on the left slot.
     * @param rightInputs The itemStack(s) placed on the right slot.
     * @param outputs     The resulting itemStack(s).
     * @return the {@link IRecipeWrapper} for this recipe.
     */
    IRecipeWrapper createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs);

    /**
     * Create an anvil recipe for the given inputs and output.
     * The number of inputs in the left and right side must match.
     *
     * @param leftInputs  The itemStack(s) placed on the left slot.
     * @param rightInputs The itemStack(s) placed on the right slot.
     * @param outputs     The resulting itemStack(s).
     * @return the {@link IRecipeWrapper} for this recipe.
     */
    IRecipeWrapper createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs);

    /**
     * Create a new smelting recipe.
     * By default, all smelting recipes from {@link FurnaceRecipes#getSmeltingList()} are already added by JFMUY.
     *
     * @param inputs the list of possible inputs to rotate through
     * @param output the output
     * @return the {@link IRecipeWrapper} for this recipe.
     */
    IRecipeWrapper createSmeltingRecipe(List<ItemStack> inputs, ItemStack output);

    /**
     * Create a new brewing recipe.
     * By default, all brewing recipes are already detected and added by JFMUY.
     *
     * @param ingredients  the ingredients added to a potion to create a new one.
     *                     Normally one ingredient, but a list will display several in rotation.
     * @param potionInput  the input potion for the brewing recipe.
     * @param potionOutput the output potion for the brewing recipe.
     * @return the {@link IRecipeWrapper} for this recipe.
     */
    IRecipeWrapper createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput);
}
