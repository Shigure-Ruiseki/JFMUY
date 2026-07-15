package ruiseki.jfmuy.api.ingredients;

import net.minecraftforge.oredict.OreDictionary;

import ruiseki.jfmuy.api.IJFMUYHelpers;

/**
 * The Ingredient Blacklist allows mods to hide ingredients from JFMUY's ingredient list.
 *
 * Ingredients can only be blacklisted during the loading phase.
 *
 * Get the instance from {@link IJFMUYHelpers#getIngredientBlacklist()}.
 */
public interface IIngredientBlacklist {

    /**
     * Stop JFMUY from displaying a specific ingredient in the ingredient list.
     * Use {@link OreDictionary#WILDCARD_VALUE} meta for wildcard.
     * Ingredients blacklisted with this API can't be seen in the config or in hide ingredients mode.
     */
    <V> void addIngredientToBlacklist(V ingredient);

    /**
     * Undo blacklisting an ingredient.
     * This is for mods that hide ingredients initially and reveal them when certain conditions are met.
     * Ingredients blacklisted by the user in the config will remain hidden.
     */
    <V> void removeIngredientFromBlacklist(V ingredient);

    /**
     * Returns true if the ingredient is blacklisted and will not be displayed in the ingredient list.
     */
    <V> boolean isIngredientBlacklisted(V ingredient);
}
