package ruiseki.jfmuy.gui.ingredients;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.gui.Focus;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {

    @Override
    public Collection<ItemStack> expandSubtypes(Collection<ItemStack> contained) {
        return Internal.getStackHelper()
            .getAllSubtypes(contained);
    }

    @Override
    public ItemStack getMatch(Iterable<ItemStack> ingredients, @Nonnull Focus toMatch) {
        return Internal.getStackHelper()
            .containsStack(ingredients, toMatch.getStack());
    }

    @Nonnull
    @Override
    public Focus createFocus(@Nonnull ItemStack ingredient) {
        return new Focus(ingredient);
    }
}
