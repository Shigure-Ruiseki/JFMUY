package ruiseki.jfmuy.startup;

import java.util.ArrayList;
import java.util.List;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.config.Config;

public abstract class AbstractModIdHelper implements IModIdHelper {

    @Override
    public <T> String getModNameForIngredient(T ingredient, IIngredientHelper<T> ingredientHelper) {
        String modId = ingredientHelper.getModId(ingredient);
        return getModNameForModId(modId);
    }

    @Override
    public <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient,
        IIngredientHelper<T> ingredientHelper) {
        String modNameFormat = Config.getModNameFormat();
        if (modNameFormat.isEmpty()) {
            return tooltip;
        }

        String modId = ingredientHelper.getDisplayModId(ingredient);
        String modName = getFormattedModNameForModId(modId);
        if (modName == null) {
            return tooltip;
        }
        List<String> tooltipCopy = new ArrayList<>(tooltip);
        tooltipCopy.add(modName);
        return tooltipCopy;
    }
}
