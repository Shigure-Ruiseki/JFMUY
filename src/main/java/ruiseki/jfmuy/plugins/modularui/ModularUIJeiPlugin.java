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
public class ModularUIJeiPlugin implements IModPlugin {

    @Override
    public void register(@NotNull IModRegistry registry) {
        ModularScreenJEIHandler.register(GuiContainerWrapper.class, registry);
        ModularScreenJEIHandler.register(GuiScreenWrapper.class, registry);
        ModularContainerJEIHandler.register(ModularContainer.class, registry);
        ModularContainerJEIHandler.register(CraftingModularContainer.class, registry);
    }

}
