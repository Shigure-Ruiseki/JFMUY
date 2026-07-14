package ruiseki.jfmuy.plugins.jfmuy.ingredients;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;

public class DebugIngredientHelper implements IIngredientHelper<DebugIngredient> {

    @Override
    public List<DebugIngredient> expandSubtypes(List<DebugIngredient> ingredients) {
        return ingredients;
    }

    @Nullable
    @Override
    public DebugIngredient getMatch(Iterable<DebugIngredient> ingredients, DebugIngredient ingredientToMatch) {
        for (DebugIngredient debugIngredient : ingredients) {
            if (debugIngredient.getNumber() == ingredientToMatch.getNumber()) {
                return debugIngredient;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(DebugIngredient ingredient) {
        return "JFMUY Debug Item #" + ingredient.getNumber();
    }

    @Override
    public String getUniqueId(DebugIngredient ingredient) {
        return "JFMUY_debug_" + ingredient.getNumber();
    }

    @Override
    public String getWildcardId(DebugIngredient ingredient) {
        return getUniqueId(ingredient);
    }

    @Override
    public String getModId(DebugIngredient ingredient) {
        return Reference.MOD_ID;
    }

    @Override
    public Iterable<Color> getColors(DebugIngredient ingredient) {
        return Collections.emptyList();
    }

    @Override
    public String getErrorInfo(DebugIngredient ingredient) {
        return getDisplayName(ingredient);
    }
}
