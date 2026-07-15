package ruiseki.jfmuy.api.recipe;

import java.util.Collection;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;

/**
 * A type of ingredient (i.e. ItemStack, FluidStack, etc) handled by JFMUY.
 * Register new types with
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 *
 * @see VanillaTypes for the built-in vanilla types {@link VanillaTypes#ITEM} and {@link VanillaTypes#FLUID}
 */
public interface IIngredientType<T> {

    /**
     * @return The class of the ingredient for this type.
     */
    Class<? extends T> getIngredientClass();
}
