package ruiseki.jfmuy.plugins.modsupport;

import net.minecraft.inventory.Container;

import cpw.mods.fml.common.Loader;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferRegistry;
import ruiseki.jfmuy.util.Log;

@JFMUYPlugin
public class ModSupportPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();

        Class<? extends Container> toAttachOutput;

        toAttachOutput = findContainerClass("ProjectE", "moze_intel.projecte.gameObjs.container.PhilosStoneContainer");
        if (toAttachOutput != null) {
            recipeTransferRegistry.overrideOutputSlot(toAttachOutput, VanillaRecipeCategoryUid.CRAFTING, 0);
        }
    }

    private Class<? extends Container> findContainerClass(String modId, String className) {
        if (!Loader.isModLoaded(modId)) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Container> clazz = (Class<? extends Container>) Class.forName(className);
            return clazz;
        } catch (Exception e) {
            Log.get()
                .error("Found '{}' mod but unable to find class '{}'", modId, className, e);
            return null;
        }
    }
}
