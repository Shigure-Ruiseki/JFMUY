package ruiseki.jfmuy.startup;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;

public interface IModIdHelper {

    String getModNameForModId(String modId);

    @Nullable
    String getFormattedModNameForModId(String modId);

    <T> String getModNameForIngredient(T ingredient, IIngredientHelper<T> ingredientHelper);

    <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient,
        IIngredientHelper<T> ingredientHelper);

    @Nullable
    String getModNameTooltipFormatting();
}
