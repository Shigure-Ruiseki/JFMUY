package ruiseki.jfmuy.plugins.modularui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;

import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.api.gui.IGuiProperties;
import ruiseki.jfmuy.api.gui.IGuiScreenHandler;

public class ModularScreenHandler<T extends GuiScreen & IMuiScreen> implements IGuiScreenHandler<T> {

    public static <T extends GuiScreen & IMuiScreen, T2 extends GuiContainer & IMuiScreen> void register(Class<T> clz,
        IModRegistry registry) {
        if (GuiContainer.class.isAssignableFrom(clz)) {
            new ContainerScreen<>((Class<T2>) clz).register(registry);
        } else {
            new ModularScreenHandler<>(clz).register(registry);
        }
    }

    private final Class<T> clazz;

    private ModularScreenHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void register(IModRegistry registry) {
        registry.addGuiScreenHandler(this.clazz, this);
    }

    public @NotNull Class<T> getGuiContainerClass() {
        return clazz;
    }

    @Nullable
    @Override
    public IGuiProperties apply(@NotNull T guiScreen) {
        return guiScreen.getScreen()
            .getContext()
            .getRecipeViewerSettings()
            .isEnabled(guiScreen.getScreen()) ? new ModularUIProperties(guiScreen) : null;
    }

    public static class ContainerScreen<T extends GuiContainer & IMuiScreen> extends ModularScreenHandler<T>
        implements IAdvancedGuiHandler<T> {

        private ContainerScreen(Class<T> clazz) {
            super(clazz);
        }

        @Override
        public void register(IModRegistry registry) {
            super.register(registry);
            registry.addAdvancedGuiHandlers(this);
        }

        @Nullable
        @Override
        public List<Rectangle> getGuiExtraAreas(@NotNull T guiContainer) {
            Iterable<Rectangle> areas = guiContainer.getScreen()
                .getContext()
                .getRecipeViewerSettings()
                .getAllRecipeViewerExclusionAreas();
            if (areas == null) return Collections.emptyList();

            List<Rectangle> list = new ArrayList<>();
            for (Rectangle rect : areas) {
                list.add(rect);
            }
            return list;
        }

        @Nullable
        @Override
        public Object getIngredientUnderMouse(@NotNull T guiContainer, int mouseX, int mouseY) {
            IWidget hovered = guiContainer.getScreen()
                .getContext()
                .getTopHovered();
            return hovered instanceof RecipeViewerIngredientProvider jip ? jip.getStackForRecipeViewer() : null;
        }
    }
}
