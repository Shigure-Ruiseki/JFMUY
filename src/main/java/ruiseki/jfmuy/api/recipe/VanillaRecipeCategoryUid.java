package ruiseki.jfmuy.api.recipe;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import ruiseki.jfmuy.api.recipe.wrapper.IShapedCraftingRecipeWrapper;

/**
 * List of built-in recipe category UIDs, so that plugins with their own recipe handlers can use them.
 */
public final class VanillaRecipeCategoryUid {

    /**
     * The crafting recipe category.
     * <p>
     * Automatically includes all {@link ShapedRecipes}, {@link ShapelessRecipes}, {@link ShapedOreRecipe}, and
     * {@link ShapelessOreRecipe}.
     * <p>
     * To add a shaped recipe wrapper to this category, it must implement {@link IShapedCraftingRecipeWrapper}.
     * <p>
     * To override the normal behavior of the crafting recipe category, you can implement
     * {@link ICustomCraftingRecipeWrapper}
     */
    public static final String CRAFTING = "minecraft.crafting";

    /**
     * The smelting recipe category.
     * <p>
     * Automatically includes everything from {@link FurnaceRecipes#getSmeltingList()}.
     */
    public static final String SMELTING = "minecraft.smelting";

    /**
     * The fuel recipe category.
     * <p>
     * Automatically includes everything that returns a value from {@link TileEntityFurnace#getItemBurnTime(ItemStack)}.
     */
    public static final String FUEL = "minecraft.fuel";

    /**
     * The brewing recipe category.
     * <p>
     * Automatically tries to generate all potion variations from the basic ingredients, determined by
     * {@link Item#isPotionIngredient(ItemStack)}.
     * You can get the list of known potion reagents from {@link IIngredientRegistry#getPotionIngredients()}.
     * <p>
     * TODO:Add OKCore BrewingRecipeRegistry
     * Also automatically adds modded potions from {@link BrewingRecipeRegistry#getRecipes()}.
     * JFMUY can only understand modded potion recipes that are {@link BrewingRecipe} or {@link BrewingOreRecipe}.
     */
    public static final String BREWING = "minecraft.brewing";

    /**
     * The anvil recipe category.
     * <p>
     * This is a built-in category, you can create new recipes with
     * {@link IVanillaRecipeFactory#createAnvilRecipe(List, List, List)}
     */
    public static final String ANVIL = "minecraft.anvil";

    /**
     * The JFMUY info recipe category shows extra information about ingredients.
     * <p>
     * This is a built-in category, you can add new recipes with
     * {@link IModRegistry#addIngredientInfo(Object, IIngredientType, String...)} or
     * {@link IModRegistry#addIngredientInfo(List, IIngredientType, String...)}
     */
    public static final String INFORMATION = "JFMUY.information";

    private VanillaRecipeCategoryUid() {

    }
}
