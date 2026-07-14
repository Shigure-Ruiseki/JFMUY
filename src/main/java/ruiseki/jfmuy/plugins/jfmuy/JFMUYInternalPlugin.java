package ruiseki.jfmuy.plugins.jfmuy;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.jfmuy.api.BlankModPlugin;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipe;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipeCategory;
import ruiseki.jfmuy.plugins.jfmuy.debug.DebugRecipeHandler;
import ruiseki.jfmuy.plugins.jfmuy.description.ItemDescriptionRecipeCategory;
import ruiseki.jfmuy.plugins.jfmuy.description.ItemDescriptionRecipeHandler;

@JFMUYPlugin
public class JFMUYInternalPlugin extends BlankModPlugin {

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
            });
        }
    }
}
