package ruiseki.jfmuy.api.recipe.transfer;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import ruiseki.jfmuy.gui.RecipeLayout;

/**
 * A recipe transfer handler moves items into a crafting area, based on the items in a recipe.
 *
 * This interface gives full control over the recipe transfer process.
 * Mods that use a regular slotted inventory can use IRecipeTransferInfo instead, which is much simpler.
 */
public interface IRecipeTransferHandler {

    Class<? extends Container> getContainerClass();

    String getRecipeCategoryUid();

    /**
     * @param maxTransfer if true, transfer as many complete sets as possible (shift-click)
     * @param doTransfer  if false, only validate and return errors without moving items
     */
    IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout,
        @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer);
}
