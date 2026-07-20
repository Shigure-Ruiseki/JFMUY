package ruiseki.jfmuy.plugins.vanilla.ingredients.enchant;

import java.awt.Color;

import javax.annotation.Nullable;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;

import com.google.common.base.Objects;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.recipe.IFocus;

public class EnchantDataHelper implements IIngredientHelper<EnchantmentData> {

    private final EnchantedBookCache cache;
    private final IIngredientHelper<ItemStack> itemStackHelper;

    public EnchantDataHelper(EnchantedBookCache cache, IIngredientHelper<ItemStack> itemStackHelper) {
        this.cache = cache;
        this.itemStackHelper = itemStackHelper;
    }

    @Override
    @Nullable
    public EnchantmentData getMatch(Iterable<EnchantmentData> ingredients, EnchantmentData toMatch) {
        for (EnchantmentData enchantData : ingredients) {
            if (enchantData.enchantmentobj != null && toMatch.enchantmentobj != null
                && enchantData.enchantmentobj.effectId == toMatch.enchantmentobj.effectId
                && enchantData.enchantmentLevel == toMatch.enchantmentLevel) {
                return enchantData;
            }
        }
        return null;
    }

    @Override
    public IFocus<?> translateFocus(IFocus<EnchantmentData> focus, IFocusFactory focusFactory) {
        EnchantmentData enchantData = focus.getValue();
        ItemStack itemStack = cache.getEnchantedBook(enchantData);
        return focusFactory.createFocus(focus.getMode(), itemStack);
    }

    @Override
    public String getDisplayName(EnchantmentData ingredient) {
        return ingredient.enchantmentobj.getTranslatedName(ingredient.enchantmentLevel);
    }

    @Override
    public String getUniqueId(EnchantmentData ingredient) {
        return "enchantment:" + ingredient.enchantmentobj.getName() + ".lvl" + ingredient.enchantmentLevel;
    }

    @Override
    public String getWildcardId(EnchantmentData ingredient) {
        return getUniqueId(ingredient);
    }

    @Override
    public String getModId(EnchantmentData ingredient) {
        String name = ingredient.enchantmentobj.getName();
        if (name != null && name.startsWith("enchantment.")) {
            String subName = name.substring("enchantment.".length());
            if (subName.contains(".")) {
                return subName.substring(0, subName.indexOf("."));
            }
        }
        return "minecraft";
    }

    @Override
    public String getDisplayModId(EnchantmentData ingredient) {
        ItemStack enchantedBook = cache.getEnchantedBook(ingredient);
        return this.itemStackHelper.getDisplayModId(enchantedBook);
    }

    @Override
    public Iterable<Color> getColors(EnchantmentData ingredient) {
        ItemStack enchantedBook = cache.getEnchantedBook(ingredient);
        return this.itemStackHelper.getColors(enchantedBook);
    }

    @Override
    public String getResourceId(EnchantmentData ingredient) {
        String name = ingredient.enchantmentobj.getName();
        if (name != null && name.startsWith("enchantment.")) {
            return name.substring("enchantment.".length());
        }
        return name != null ? name : "unknown";
    }

    @Override
    public ItemStack getCheatItemStack(EnchantmentData ingredient) {
        return cache.getEnchantedBook(ingredient);
    }

    @Override
    public EnchantmentData copyIngredient(EnchantmentData ingredient) {
        return new EnchantmentData(ingredient.enchantmentobj, ingredient.enchantmentLevel);
    }

    @Override
    public boolean isIngredientOnServer(EnchantmentData ingredient) {
        ItemStack enchantedBook = cache.getEnchantedBook(ingredient);
        return this.itemStackHelper.isIngredientOnServer(enchantedBook);
    }

    @Override
    public String getErrorInfo(@Nullable EnchantmentData ingredient) {
        if (ingredient == null) {
            return "null";
        }
        return Objects.toStringHelper(EnchantmentData.class)
            .add("Enchantment", ingredient.enchantmentobj != null ? ingredient.enchantmentobj.getName() : "null")
            .add("Level", ingredient.enchantmentLevel)
            .toString();
    }
}
