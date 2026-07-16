package ruiseki.jfmuy.plugins.vanilla.ingredients.fluid;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Objects;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.color.ColorGetter;
import ruiseki.jfmuy.config.Config;
import ruiseki.okcore.fluid.capability.CapabilityFluidHandler;
import ruiseki.okcore.fluid.handler.IFluidHandlerItem;
import ruiseki.okcore.helper.CapabilityHelpers;

public class FluidStackHelper implements IIngredientHelper<FluidStack> {

    @Override
    public List<FluidStack> expandSubtypes(List<FluidStack> contained) {
        return contained;
    }

    @Override
    @Nullable
    public FluidStack getMatch(Iterable<FluidStack> ingredients, FluidStack toMatch) {
        for (FluidStack fluidStack : ingredients) {
            if (toMatch.getFluid() == fluidStack.getFluid()) {
                return fluidStack;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(FluidStack ingredient) {
        return ingredient.getLocalizedName();
    }

    @Override
    public String getUniqueId(FluidStack ingredient) {
        StringBuilder uniqueId = new StringBuilder("fluid:");
        uniqueId.append(
            ingredient.getFluid()
                .getName());
        String subtype = Internal.getSubtypeRegistry()
            .getSubtypeInfo(ingredient);
        if (subtype != null && !subtype.isEmpty()) {
            uniqueId.append(":");
            uniqueId.append(subtype);
        }
        return uniqueId.toString();
    }

    @Override
    public String getWildcardId(FluidStack ingredient) {
        return getUniqueId(ingredient);
    }

    @Override
    public String getModId(FluidStack ingredient) {
        String defaultFluidName = FluidRegistry.getDefaultFluidName(ingredient.getFluid());
        if (defaultFluidName == null) {
            return "";
        }
        ResourceLocation fluidResourceName = new ResourceLocation(defaultFluidName);
        return fluidResourceName.getResourceDomain();
    }

    @Override
    public Iterable<Color> getColors(FluidStack ingredient) {
        Fluid fluid = ingredient.getFluid();

        IIcon fluidIcon = fluid.getIcon(ingredient);

        if (fluidIcon != null) {
            int renderColor = fluid.getColor(ingredient);
            return ColorGetter.getColors((TextureAtlasSprite) fluidIcon, renderColor, 1);
        }

        return Collections.emptyList();
    }

    @Override
    public String getResourceId(FluidStack ingredient) {
        String defaultFluidName = FluidRegistry.getDefaultFluidName(ingredient.getFluid());
        if (defaultFluidName == null) {
            return "";
        }
        ResourceLocation fluidResourceName = new ResourceLocation(defaultFluidName);
        return fluidResourceName.getResourcePath();
    }

    @Override
    @Nullable
    public ItemStack getCheatItemStack(FluidStack ingredient) {
        final FluidStack ingredientCopy = ingredient.copy();
        ingredientCopy.amount = Integer.MAX_VALUE;

        return CapabilityHelpers
            .getCapability(Config.getDefaultFluidContainerItem(), CapabilityFluidHandler.FLUID_HANDLER_ITEM)
            .map(handler -> {
                handler.fill(ingredientCopy, true);
                return handler.getContainer();
            })
            .orElse(null);
    }

    @Override
    @Nullable
    public ItemStack replaceWithCheatItemStack(FluidStack ingredient, ItemStack clickedWith) {
        IFluidHandlerItem lazyHandler = CapabilityHelpers
            .getCapability(clickedWith, CapabilityFluidHandler.FLUID_HANDLER_ITEM)
            .getOrNull();

        if (lazyHandler != null) {
            ItemStack clickedWithCopy = clickedWith.copy();
            clickedWithCopy.stackSize = 1;

            final FluidStack ingredientCopy = ingredient.copy();
            ingredientCopy.amount = Integer.MAX_VALUE;

            IFluidHandlerItem handler = CapabilityHelpers
                .getCapability(clickedWithCopy, CapabilityFluidHandler.FLUID_HANDLER_ITEM)
                .getOrNull();
            if (handler != null) {
                ingredient = ingredient.copy();
                ingredient.amount = Integer.MAX_VALUE;
                if (handler.fill(ingredient, true) > 0) {
                    return handler.getContainer();
                }
            }
        }
        return null;
    }

    @Override
    public FluidStack copyIngredient(FluidStack ingredient) {
        return ingredient.copy();
    }

    @Override
    public String getErrorInfo(FluidStack ingredient) {
        if (ingredient == null) {
            return "null";
        }
        Objects.ToStringHelper toStringHelper = Objects.toStringHelper(FluidStack.class);

        Fluid fluid = ingredient.getFluid();
        if (fluid != null) {
            toStringHelper.add("Fluid", fluid.getLocalizedName(ingredient));
        } else {
            toStringHelper.add("Fluid", "null");
        }

        toStringHelper.add("Amount", ingredient.amount);

        if (ingredient.tag != null) {
            toStringHelper.add("Tag", ingredient.tag);
        }

        return toStringHelper.toString();
    }
}
