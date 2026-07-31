package ruiseki.jfmuy.plugins.modularui;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.test.CraftingModularContainer;

import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;

@JFMUYPlugin(value = "modularui2")
public class ModularUIPlugin implements IModPlugin {

    @Override
    public void register(@NotNull IModRegistry registry) {
        ModularScreenHandler.register(GuiContainerWrapper.class, registry);
        ModularScreenHandler.register(GuiScreenWrapper.class, registry);
        ModularContainerHandler.register(ModularContainer.class, registry);
        ModularContainerHandler.register(CraftingModularContainer.class, registry);
    }

}
