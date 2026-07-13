package ruiseki.jfmuy.gui.ingredients;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.gui.Focus;
import ruiseki.jfmuy.util.StackUtil;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {

    @Override
    public Collection<ItemStack> expandSubtypes(Collection<ItemStack> contained) {
        return StackUtil.getAllSubtypes(contained);
    }

    @Override
    public ItemStack getMatch(Iterable<ItemStack> contained, @Nonnull Focus toMatch) {
        return StackUtil.containsStack(contained, toMatch.getStack());
    }
}
