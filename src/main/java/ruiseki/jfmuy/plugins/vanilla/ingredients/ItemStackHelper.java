package ruiseki.jfmuy.plugins.vanilla.ingredients;

import java.awt.Color;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.StackHelper;
import ruiseki.jfmuy.util.color.ColorGetter;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {

    private final StackHelper stackHelper;

    public ItemStackHelper(StackHelper stackHelper) {
        this.stackHelper = stackHelper;
    }

    @Override
    public List<ItemStack> expandSubtypes(List<ItemStack> contained) {
        return stackHelper.getAllSubtypes(contained);
    }

    @Override
    @Nullable
    public ItemStack getMatch(Iterable<ItemStack> ingredients, ItemStack toMatch) {
        return stackHelper.containsStack(ingredients, toMatch);
    }

    @Override
    public String getDisplayName(ItemStack ingredient) {
        String displayName = ingredient.getDisplayName();
        if (displayName == null) {
            String ingredientInfo = getErrorInfo(ingredient);
            throw new NullPointerException("No display name for itemStack. " + ingredientInfo);
        }
        return displayName;
    }

    @Override
    public String getUniqueId(ItemStack ingredient) {
        return stackHelper.getUniqueIdentifierForStack(ingredient);
    }

    @Override
    public String getWildcardId(ItemStack ingredient) {
        return stackHelper.getUniqueIdentifierForStack(ingredient, StackHelper.UidMode.WILDCARD);
    }

    @Override
    public String getModId(ItemStack ingredient) {
        Item item = ingredient.getItem();
        if (item == null) {
            throw new NullPointerException("Null item in ItemStack");
        }

        ResourceLocation itemName = new ResourceLocation(
            GameData.getItemRegistry()
                .getNameForObject(item));
        if (itemName == null) {
            String stackInfo = getErrorInfo(ingredient);
            throw new NullPointerException("item.getRegistryName() returned null for: " + stackInfo);
        }

        return itemName.getResourceDomain();
    }

    @Override
    public Iterable<Color> getColors(ItemStack ingredient) {
        return ColorGetter.getColors(ingredient, 2);
    }

    @Override
    public String getErrorInfo(ItemStack ingredient) {
        return ErrorUtil.getItemStackInfo(ingredient);
    }
}
