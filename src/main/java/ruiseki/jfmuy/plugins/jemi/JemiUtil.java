package ruiseki.jfmuy.plugins.jemi;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.collect.Sets;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.screen.RecipeScreen;
import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.ITypedIngredient;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.gui.elements.GuiIconButton;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JemiUtil {

    public static EmiIngredient getIngredient(List<ITypedIngredient<?>> ingredients) {
        if (ingredients.isEmpty()) {
            return EmiStack.EMPTY;
        }
        return EmiIngredient.of(
            ingredients.stream()
                .map(JemiUtil::getStack)
                .filter(i -> !i.isEmpty())
                .collect(Collectors.toList()));
    }

    public static EmiStack getStack(Object ingredient) {
        if (JemiPlugin.ingredientRegistry != null) {
            try {
                IIngredientType type = JemiPlugin.ingredientRegistry.getIngredientType(ingredient);
                if (type != null) {
                    return getStack(type, ingredient);
                }
            } catch (IllegalArgumentException e) {}
        }
        return EmiStack.EMPTY;
    }

    public static EmiStack getStack(ITypedIngredient<?> ingredient) {
        return getStack(ingredient.type(), ingredient.ingredient());
    }

    public static EmiStack getStack(IIngredientType<?> type, Object ingredient) {
        if (ingredient == null || (type != null && !type.getIngredientClass()
            .isInstance(ingredient))) {
            return EmiStack.EMPTY;
        }
        if (type == VanillaTypes.ITEM && ingredient instanceof ItemStack) {
            return EmiStack.of((ItemStack) ingredient);
        } else if (type == getFluidType() || ingredient instanceof FluidStack) {
            return EmiAgnos.createFluidStack(ingredient);
        } else {
            IIngredientRegistry im = JemiPlugin.ingredientRegistry;
            IIngredientHelper helper = im.getIngredientHelper(type);
            if (helper.isValidIngredient(ingredient)) {
                return new JemiStack(type, helper, im.getIngredientRenderer(type), ingredient);
            }
        }
        return EmiStack.EMPTY;
    }

    public static Optional<ITypedIngredient<?>> getTyped(EmiStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        } else if (stack.getKey() instanceof Fluid f) {
            return createTypedIngredient(
                VanillaTypes.FLUID,
                new FluidStack(f, (int) (stack.getAmount() == 0 ? 1000 : stack.getAmount()), stack.getNbt()));
        } else if (stack instanceof JemiStack js) {
            return createTypedIngredient(js.type, js.ingredient);
        }
        return createTypedIngredient(VanillaTypes.ITEM, stack.getItemStack());
    }

    public static <T> Optional<ITypedIngredient<?>> createTypedIngredient(IIngredientType<T> type, T ingredient) {
        return Optional.of(new ITypedIngredient(type, ingredient));
    }

    public static IIngredientType<?> getFluidType() {
        return VanillaTypes.FLUID;
    }

    public static Set<String> getHandledMods() {
        Set<String> set = Sets.newHashSet();
        for (String mod : EmiAgnos.getModsWithPlugins()) {
            set.add(mod);
        }
        return set;
    }

    public static GuiIconButton getConfigButton(IJFMUYRuntime runtime) {
        return runtime.getIngredientListOverlay()
            .getConfigButton()
            .getInternalButton();
    }

    public static GuiIconButton getBookmarkButton(IJFMUYRuntime runtime) {
        return runtime.getBookmarkOverlay()
            .getBookmarkButton()
            .getInternalButton();
    }

    public static int getConfigButtonWidth(IJFMUYRuntime runtime) {
        try {
            if (Minecraft.getMinecraft().currentScreen instanceof RecipeScreen) return 0;
            int width = getConfigButton(runtime).getButtonWidth();
            return width > 0 ? width : 20;
        } catch (Exception ignored) {
            return 0;
        }
    }
}
