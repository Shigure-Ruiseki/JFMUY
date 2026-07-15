package ruiseki.jfmuy.plugins.jfmuy;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.ISubtypeRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IModIngredientRegistration;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipe;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipeCategory;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipeHandler;
import ruiseki.jfmuy.plugins.jfmuy.description.ItemDescriptionRecipeCategory;
import ruiseki.jfmuy.plugins.jfmuy.description.ItemDescriptionRecipeHandler;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredient;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredientHelper;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredientListFactory;
import ruiseki.jfmuy.plugins.jfmuy.ingredients.DebugIngredientRenderer;

@JFMUYPlugin
public class JFMUYInternalPlugin extends BlankModPlugin {

    @Nullable
    public static IIngredientRegistry ingredientRegistry;
    @Nullable
    public static IJFMUYRuntime jfmuyRuntime;

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        if (Config.isDebugModeEnabled()) {
            registry.register(
                DebugIngredient.class,
                Collections.<DebugIngredient>emptyList(),
                new DebugIngredientHelper(),
                new DebugIngredientRenderer());
        }
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        IJFMUYHelpers jfmuyHelpers = registry.getJFMUYHelpers();
        IGuiHelper guiHelper = jfmuyHelpers.getGuiHelper();

        registry.addRecipeCategories(new ItemDescriptionRecipeCategory(guiHelper));

        registry.addRecipeHandlers(new ItemDescriptionRecipeHandler());

        if (Config.isDebugModeEnabled()) {
            Arrays.asList(
                new ItemStack(Items.wooden_door),
                "description.jfmuy.wooden.door.1", // actually 2 lines
                "description.jfmuy.wooden.door.2",
                "description.jfmuy.wooden.door.3");

            registry.addRecipeCategories(new DebugRecipeCategory(guiHelper));
            registry.addRecipeHandlers(new DebugRecipeHandler());
            registry.addRecipes(Arrays.asList(new DebugRecipe(), new DebugRecipe()));

            registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiBrewingStand>() {

                @NotNull
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

                @Override
                public @Nullable Object getIngredientUnderMouse(GuiBrewingStand guiContainer, int mouseX, int mouseY) {
                    if (mouseX < 10 && mouseY < 10) {
                        return new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME);
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public void onRuntimeAvailable(IJFMUYRuntime jfmuyRuntime) {
        JFMUYInternalPlugin.jfmuyRuntime = jfmuyRuntime;

        if (Config.isDebugModeEnabled()) {
            jfmuyRuntime.getItemListOverlay()
                .highlightStacks(Collections.singleton(new ItemStack(Items.stick)));
            if (ingredientRegistry != null) {
                ingredientRegistry.addIngredientsAtRuntime(DebugIngredient.class, DebugIngredientListFactory.create());
            }
        }
    }
}
