package ruiseki.jfmuy.api.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helps get ItemStacks from common formats used in recipes.
 */
public interface IStackHelper {

    /**
     * Returns all the subtypes of itemStack if it has a wildcard meta value.
     */
    @NotNull
    List<ItemStack> getSubtypes(@NotNull ItemStack itemStack);

    /**
     * Expands an Iterable, which may contain ItemStacks or more Iterables, and
     * returns all the subtypes of itemStacks if they have wildcard meta value.
     */
    @NotNull
    List<ItemStack> getAllSubtypes(@NotNull Iterable stacks);

    /**
     * Flattens ItemStacks, OreDict Strings, and Iterables into a list of ItemStacks.
     */
    @NotNull
    List<ItemStack> toItemStackList(@Nullable Object stacks);
}
