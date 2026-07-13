package ruiseki.jfmuy.api.recipe;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;

/**
 * A type of ingredient (i.e. ItemStack, FluidStack, etc) handled by JEI.
 * Register new types with
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 *
 * @see VanillaTypes for the built-in vanilla types {@link VanillaTypes#ITEM} and {@link VanillaTypes#FLUID}
 * @since JEI 4.12.0
 */
public interface IIngredientType<T> {

    /**
     * @return The class of the ingredient for this type.
     */
    Class<? extends T> getIngredientClass();
}
