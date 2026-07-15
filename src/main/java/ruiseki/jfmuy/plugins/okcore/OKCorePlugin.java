package ruiseki.jfmuy.plugins.okcore;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.plugins.okcore.crafting.ShapedRecipeWrapper;
import ruiseki.jfmuy.plugins.okcore.crafting.ShapelessRecipeWrapper;
import ruiseki.jfmuy.plugins.vanilla.crafting.CraftingRecipeValidator;
import ruiseki.jfmuy.plugins.vanilla.crafting.CraftingRecipeValidatorRegistry;
import ruiseki.okcore.recipe.type.crafting.shaped.ShapedRecipe;
import ruiseki.okcore.recipe.type.crafting.shapless.ShapelessRecipe;

@JFMUYPlugin
public class OKCorePlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IJFMUYHelpers jfmuyHelpers = registry.getJFMUYHelpers();

        CraftingRecipeValidatorRegistry.register(ShapedRecipe.class,
            new CraftingRecipeValidator<ShapedRecipe>(recipe -> new ShapedRecipeWrapper(jfmuyHelpers, recipe)));

        CraftingRecipeValidatorRegistry.register(ShapelessRecipe.class,
            new CraftingRecipeValidator<ShapelessRecipe>(recipe -> new ShapelessRecipeWrapper(jfmuyHelpers, recipe)));
    }
}
