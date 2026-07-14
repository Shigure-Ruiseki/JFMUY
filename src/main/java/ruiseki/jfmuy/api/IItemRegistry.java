package ruiseki.jfmuy.api;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableList;

/**
 * The IItemRegistry is provided by JFMUY and has some useful functions related to items.
 */
public interface IItemRegistry {

    /** Returns a list of all the Items registered. */
    @Nonnull
    ImmutableList<ItemStack> getItemList();

    /** Returns a list of all the Items that can be used as fuel in a vanilla furnace. */
    @Nonnull
    ImmutableList<ItemStack> getFuels();

    /** Returns a list of all the ItemStacks that return true to isPotionIngredient. */
    @Nonnull
    ImmutableList<ItemStack> getPotionIngredients();

    /** Returns a mod name for the given item. */
    @Nonnull
    String getModNameForItem(@Nonnull Item item);

    @Nonnull
    ImmutableList<ItemStack> getItemListForModId(@Nonnull String modId);
}
