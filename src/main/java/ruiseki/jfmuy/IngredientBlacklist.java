package ruiseki.jfmuy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

import ruiseki.jfmuy.api.ingredients.IIngredientBlacklist;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.util.IngredientUtil;

public class IngredientBlacklist implements IIngredientBlacklist {

    private final IIngredientRegistry ingredientRegistry;
    private final Set<String> ingredientBlacklist = new HashSet<>();

    public IngredientBlacklist(IIngredientRegistry ingredientRegistry) {
        this.ingredientRegistry = ingredientRegistry;
    }

    @Override
    public <V> void addIngredientToBlacklist(V ingredient) {
        Preconditions.checkNotNull(ingredient, "Null ingredient");

        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
        String uniqueName = ingredientHelper.getUniqueId(ingredient);
        ingredientBlacklist.add(uniqueName);
    }

    @Override
    public <V> void removeIngredientFromBlacklist(V ingredient) {
        Preconditions.checkNotNull(ingredient, "Null ingredient");

        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
        String uniqueName = ingredientHelper.getUniqueId(ingredient);
        ingredientBlacklist.remove(uniqueName);
    }

    @Override
    public <V> boolean isIngredientBlacklisted(V ingredient) {
        Preconditions.checkNotNull(ingredient, "Null ingredient");

        if (isIngredientBlacklistedByApi(ingredient)) {
            return true;
        }

        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
        return Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper);
    }

    public <V> boolean isIngredientBlacklistedByApi(V ingredient) {
        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
        List<String> uids = IngredientUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient);

        for (String uid : uids) {
            if (ingredientBlacklist.contains(uid)) {
                return true;
            }
        }

        return false;
    }
}
