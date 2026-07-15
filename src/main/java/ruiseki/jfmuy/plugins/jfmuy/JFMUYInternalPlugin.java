package ruiseki.jfmuy.plugins.jfmuy;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IRecipeCategoryRegistration;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.gui.GuiProperties;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugGhostIngredientHandler;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipe;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipeCategory;
import ruiseki.jfmuy.plugins.jfmuy.info.IngredientInfoRecipeCategory;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredient;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredientHelper;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredientListFactory;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredientRenderer;
import ruiseki.jfmuy.runtime.JFMUYHelpers;

@JFMUYPlugin
public class JFMUYInternalPlugin implements IModPlugin {

    @Nullable
    public static IIngredientRegistry ingredientRegistry;
    @Nullable
    public static IJFMUYRuntime jfmuyRuntime;

    @Override
    public void registerIngredients(IModIngredientRegistration ingredientRegistration) {
        if (Config.isDebugModeEnabled()) {
            DebugIngredientHelper ingredientHelper = new DebugIngredientHelper();
            DebugIngredientRenderer ingredientRenderer = new DebugIngredientRenderer(ingredientHelper);
            ingredientRegistration
                .register(DebugIngredient.TYPE, Collections.emptyList(), ingredientHelper, ingredientRenderer);
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        JFMUYHelpers jeiHelpers = Internal.getHelpers();
        GuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registry.addRecipeCategories(new IngredientInfoRecipeCategory(guiHelper));

        if (Config.isDebugModeEnabled()) {
            registry.addRecipeCategories(new DebugRecipeCategory(guiHelper));
        }
    }

    @Override
    public void register(IModRegistry registry) {
        ingredientRegistry = registry.getIngredientRegistry();
        registry.addGuiScreenHandler(GuiContainer.class, GuiProperties::create);
        registry.addGuiScreenHandler(RecipesGui.class, GuiProperties::create);

        if (Config.isDebugModeEnabled()) {
            registry.addIngredientInfo(
                List.of(new ItemStack(Items.wooden_door)),
                VanillaTypes.ITEM,
                "description.jfmuy.wooden.door.1", // actually 2 lines
                "description.jfmuy.wooden.door.2",
                "description.jfmuy.wooden.door.3");

            registry.addIngredientInfo(
                new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME),
                VanillaTypes.FLUID,
                "water");

            registry.addRecipes(Arrays.asList(new DebugRecipe(), new DebugRecipe()), "debug");

            registry.addRecipeCatalyst(new DebugIngredient(7), "debug");
            registry
                .addRecipeCatalyst(new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME), "debug");
            registry.addRecipeCatalyst(new ItemStack(Items.stick), "debug");
            int i = 0;
            for (Object itemObj : GameData.getBlockRegistry()) {
                if (itemObj instanceof Item item) {
                    ItemStack catalystIngredient = new ItemStack(item);

                    if (catalystIngredient.getItem() != null) {
                        registry.addRecipeCatalyst(catalystIngredient, "debug");

                        i++;
                        if (i >= 30) {
                            break;
                        }
                    }
                }
            }

            registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiBrewingStand>() {

                @Override
                public Class<GuiBrewingStand> getGuiContainerClass() {
                    return GuiBrewingStand.class;
                }

                @Override
                public List<Rectangle> getGuiExtraAreas(GuiBrewingStand guiContainer) {
                    int widthMovement = (int) ((System.currentTimeMillis() / 100) % 100);
                    int size = 25 + widthMovement;
                    return Collections.singletonList(
                        new Rectangle(guiContainer.guiLeft + guiContainer.xSize, guiContainer.guiTop + 40, size, size));
                }

                @Nullable
                @Override
                public Object getIngredientUnderMouse(GuiBrewingStand guiContainer, int mouseX, int mouseY) {
                    if (mouseX < 10 && mouseY < 10) {
                        return new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME);
                    }
                    return null;
                }
            });

            registry.addGhostIngredientHandler(GuiBrewingStand.class, new DebugGhostIngredientHandler<>());
        }
    }

    @Override
    public void onRuntimeAvailable(IJFMUYRuntime JFMUYRuntime) {
        JFMUYInternalPlugin.jfmuyRuntime = JFMUYRuntime;

        if (Config.isDebugModeEnabled()) {
            if (ingredientRegistry != null) {
                ingredientRegistry.addIngredientsAtRuntime(DebugIngredient.TYPE, DebugIngredientListFactory.create());
            }
        }
    }
}
