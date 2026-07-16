package ruiseki.jfmuy.api.ingredients;

import java.util.function.Function;

/**
 * A subtype interpreter tells JFMUY how to create unique ids for ingredients.
 *
 * For example, an ItemStack may have some NBT that is used to create many subtypes,
 * and other NBT that is used for electric charge that can be ignored.
 * You can tell JFMUY how to interpret these differences by implementing an
 * {@link IIngredientSubtypeInterpreter} and registering it with JFMUY in
 * {@link ruiseki.jfmuy.api.ISubtypeRegistry}
 */
@FunctionalInterface
public interface IIngredientSubtypeInterpreter<T> extends Function<T, String> {

    String NONE = "";

    /**
     * Get the data from an ingredient that is relevant to telling subtypes apart in the given context.
     * This should account for nbt, and anything else that's relevant.
     *
     * Return {@link #NONE} if there is no data used for subtypes.
     */
    String apply(T ingredient);
}
