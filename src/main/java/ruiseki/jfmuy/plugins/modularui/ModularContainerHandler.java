package ruiseki.jfmuy.plugins.modularui;

import net.minecraft.entity.player.EntityPlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;

import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferHandler;

public class ModularContainerHandler<T extends ModularContainer> implements IRecipeTransferHandler<T> {

    public static <T extends ModularContainer> void register(Class<T> clz, IModRegistry registry) {
        new ModularContainerHandler<>(clz).register(registry);
    }

    private final Class<T> clazz;

    private ModularContainerHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    private void register(IModRegistry registry) {
        registry.getRecipeTransferRegistry()
            .addUniversalRecipeTransferHandler(this);
    }

    @Override
    public @NotNull Class<T> getContainerClass() {
        return clazz;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@NotNull ModularContainer container, @NotNull IRecipeLayout recipeLayout,
        @NotNull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        ModularScreen screen = container.getScreen();
        if (screen instanceof RecipeViewerRecipeTransferHandler recipeTransferHandler) {
            return recipeTransferHandler.transferRecipe(recipeLayout, maxTransfer, !doTransfer);
        }
        return null;
    }
}
