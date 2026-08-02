package ruiseki.jfmuy.plugins.nei;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.api.API;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.recipe.IRecipeCategoryRegistration;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;

@JFMUYPlugin(value = "NotEnoughItems")
public class NEIPlugin implements IModPlugin {

    private final List<RecipeHarvester> harvesters = new ArrayList<>();
    private final Set<String> registeredCategoryUids = new HashSet<>();

    public static boolean isNEILoaded = false;

    public static void hookNEI() {
        if (isNEILoaded) return;
        try {
            NEIClientConfig.bootNEI();
            API.registerNEIGuiHandler(new NEIScreenHandler());
        } catch (Throwable ignored) {}
        isNEILoaded = true;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        hookNEI();
        harvesters.clear();
        registeredCategoryUids.clear();

        Set<Class<?>> processedClasses = new HashSet<>();

        List<Object> rawHandlers = new ArrayList<>();
        if (GuiCraftingRecipe.craftinghandlers != null) {
            rawHandlers.addAll(GuiCraftingRecipe.craftinghandlers);
        }
        if (GuiUsageRecipe.usagehandlers != null) {
            rawHandlers.addAll(GuiUsageRecipe.usagehandlers);
        }

        for (Object handler : rawHandlers) {
            if (handler instanceof TemplateRecipeHandler templateHandler) {
                if (RecipeHarvester.getBlacklistedClasses()
                    .contains(
                        templateHandler.getClass()
                            .getName())) {
                    continue;
                }

                if (processedClasses.add(templateHandler.getClass())) {
                    RecipeHarvester harvester = new RecipeHarvester(templateHandler);
                    Set<String> ids = harvester.extractRecipeIds();

                    if (!ids.isEmpty()) {
                        harvester.register(registry, ids, registeredCategoryUids);
                        harvesters.add(harvester);
                    }
                }
            }
        }
    }

    @Override
    public void register(IModRegistry registry) {
        Set<String> globalHarvestedCatalystUids = new HashSet<>();

        for (RecipeHarvester harvester : harvesters) {
            harvester.initialize(registry, globalHarvestedCatalystUids);
        }

        if (registeredCategoryUids.contains("nei.crafting")) {
            try {
                registry.getRecipeTransferRegistry()
                    .copyRecipeTransferHandlers(VanillaRecipeCategoryUid.CRAFTING, "nei.crafting");
            } catch (Throwable ignored) {}
        }

        registeredCategoryUids.clear();
        harvesters.clear();
        RecipeHarvester.resetBlacklistToDefaults();
    }
}
