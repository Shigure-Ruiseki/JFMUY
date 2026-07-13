package ruiseki.jfmuy.plugins.jfmuy;

import java.util.Arrays;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IItemRegistry;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.IRecipeRegistry;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipe;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipeCategory;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipeHandler;
import ruiseki.jfmuy.plugins.jfmuy.description.ItemDescriptionRecipeCategory;
import ruiseki.jfmuy.plugins.jfmuy.description.ItemDescriptionRecipeHandler;

@ruiseki.jfmuy.api.JFMUYPlugin
public class JFMUYPlugin implements IModPlugin {

    private IJFMUYHelpers jfmuyHelpers;

    @Override
    public void onJFMUYHelpersAvailable(IJFMUYHelpers jfmuyHelpers) {
        this.jfmuyHelpers = jfmuyHelpers;
    }

    @Override
    public void onItemRegistryAvailable(IItemRegistry itemRegistry) {

    }

    @Override
    public void register(IModRegistry registry) {
        IGuiHelper guiHelper = jfmuyHelpers.getGuiHelper();

        registry.addRecipeCategories(new ItemDescriptionRecipeCategory(guiHelper));

        registry.addRecipeHandlers(new ItemDescriptionRecipeHandler());

        if (Config.isDebugModeEnabled()) {
            registry.addDescription(
                Arrays.asList(new ItemStack(Items.wooden_door)),
                "description.jfmuy.wooden.door.1", // actually 2 lines
                "description.jfmuy.wooden.door.2",
                "description.jfmuy.wooden.door.3");

            registry.addRecipeCategories(new DebugRecipeCategory(guiHelper));
            registry.addRecipeHandlers(new DebugRecipeHandler());
            registry.addRecipes(Arrays.asList(new DebugRecipe(), new DebugRecipe()));
        }
    }

    @Override
    public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry) {

    }
}
