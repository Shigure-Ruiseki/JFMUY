package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.plugins.okcore.crafting.ShapedRecipeWrapper;
import ruiseki.jfmuy.plugins.okcore.crafting.ShapelessRecipeWrapper;
import ruiseki.okcore.recipe.type.crafting.shaped.ShapedRecipe;
import ruiseki.okcore.recipe.type.crafting.shapless.ShapelessRecipe;

public final class CraftingRecipeValidatorRegistry {

    private static final Map<Class<? extends IRecipe>, ICraftingRecipeValidator> VALIDATORS = new LinkedHashMap<>();

    private CraftingRecipeValidatorRegistry() {}

    public static <T extends IRecipe> void register(Class<T> recipeClass, ICraftingRecipeValidator validator) {
        if (!VALIDATORS.containsKey(recipeClass)) {
            VALIDATORS.put(recipeClass, validator);
        }
    }

    public static @Nullable ICraftingRecipeValidator getValidatorFor(IRecipe recipe) {
        ICraftingRecipeValidator validator = VALIDATORS.get(recipe.getClass());
        if (validator != null) {
            return validator;
        }

        for (Map.Entry<Class<? extends IRecipe>, ICraftingRecipeValidator> entry : VALIDATORS.entrySet()) {
            if (entry.getKey()
                .isInstance(recipe)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static void init() {
        CraftingRecipeValidatorRegistry.register(
            ShapedOreRecipe.class,
            new CraftingRecipeValidator<ShapedOreRecipe>(
                recipe -> new ShapedOreRecipeWrapper(Internal.getHelpers(), recipe)));

        CraftingRecipeValidatorRegistry.register(
            ShapedRecipes.class,
            new CraftingRecipeValidator<ShapedRecipes>(
                recipe -> new ShapedRecipesWrapper(Internal.getHelpers(), recipe)));

        CraftingRecipeValidatorRegistry.register(
            ShapelessOreRecipe.class,
            new CraftingRecipeValidator<ShapelessOreRecipe>(
                recipe -> new ShapelessRecipesWrapper<>(Internal.getHelpers(), recipe)));

        CraftingRecipeValidatorRegistry.register(
            ShapelessRecipes.class,
            new CraftingRecipeValidator<ShapelessRecipes>(
                recipe -> new ShapelessRecipesWrapper<>(Internal.getHelpers(), recipe)));

        CraftingRecipeValidatorRegistry.register(
            ShapedRecipe.class,
            new CraftingRecipeValidator<ShapedRecipe>(
                recipe -> new ShapedRecipeWrapper(Internal.getHelpers(), recipe)));

        CraftingRecipeValidatorRegistry.register(
            ShapelessRecipe.class,
            new CraftingRecipeValidator<ShapelessRecipe>(
                recipe -> new ShapelessRecipeWrapper(Internal.getHelpers(), recipe)));
    }
}
