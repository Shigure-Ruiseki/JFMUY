package ruiseki.jfmuy.api.recipe.transfer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.gui.IRecipeLayout;

public interface IRecipeCraftingHandler<C extends Container> extends IRecipeTransferHandler<C> {

    /**
     * Implementations of this method must lead to {@link IAutocraftingHandler#stepFinished} being called at some point!
     * 
     * @param container    the container to act on
     * @param recipeLayout the layout of the recipe, with information about the ingredients
     * @param player       the player, to do the slot manipulation
     * @param amount       number of sets of items to transfer
     * @param doTransfer   if true, do the transfer. if false, check for errors but do not actually transfer the items
     * @return a recipe transfer error if the recipe can't be transferred. Return null on success.
     */
    @Nullable
    IRecipeTransferError craft(C container, IRecipeLayout recipeLayout, EntityPlayer player, int amount,
        boolean doTransfer);
}
