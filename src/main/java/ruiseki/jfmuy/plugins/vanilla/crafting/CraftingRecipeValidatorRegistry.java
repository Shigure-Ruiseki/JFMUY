package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.crafting.IRecipe;

import org.jetbrains.annotations.Nullable;

public final class CraftingRecipeValidatorRegistry {

    private static final Map<Class<? extends IRecipe>, ICraftingRecipeValidator> VALIDATORS = new LinkedHashMap<>();

    private CraftingRecipeValidatorRegistry() {}

    public static <T extends IRecipe> void register(Class<T> recipeClass, ICraftingRecipeValidator validator) {
        VALIDATORS.put(recipeClass, validator);
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
}
