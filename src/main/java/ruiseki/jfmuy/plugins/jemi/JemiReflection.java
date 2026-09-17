package ruiseki.jfmuy.plugins.jemi;

import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.relauncher.ReflectionHelper;
import ruiseki.jfmuy.api.ISubtypeRegistry;

public class JemiReflection {

    static boolean hasFluidSubtype(ISubtypeRegistry subtypeRegistry, FluidStack stack) {
        try {
            return (Boolean) ReflectionHelper
                .findMethod(
                    ISubtypeRegistry.class,
                    subtypeRegistry,
                    new String[] { "hasSubtypeInterpreter" },
                    FluidStack.class)
                .invoke(subtypeRegistry, stack);
        } catch (Exception ignored) {}
        return false;
    }

    static String getFluidSubtype(ISubtypeRegistry subtypeRegistry, FluidStack stack) {
        try {
            return (String) ReflectionHelper
                .findMethod(
                    ISubtypeRegistry.class,
                    subtypeRegistry,
                    new String[] { "getSubtypeInfo" },
                    FluidStack.class)
                .invoke(subtypeRegistry, stack);
        } catch (Exception ignored) {}
        return null;
    }
}
