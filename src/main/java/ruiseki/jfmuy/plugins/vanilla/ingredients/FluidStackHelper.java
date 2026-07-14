package ruiseki.jfmuy.plugins.vanilla.ingredients;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Objects;

import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.util.color.ColorGetter;

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
        if (ingredient.tag != null) {
            return "fluid:" + ingredient.getFluid()
                .getName() + ":" + ingredient.tag;
        }
        return "fluid:" + ingredient.getFluid()
            .getName();
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
    public String getErrorInfo(FluidStack ingredient) {
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
