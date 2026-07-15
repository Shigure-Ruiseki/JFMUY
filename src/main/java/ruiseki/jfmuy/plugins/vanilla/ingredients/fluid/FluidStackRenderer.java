package ruiseki.jfmuy.plugins.vanilla.ingredients.fluid;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;

public class FluidStackRenderer implements IIngredientRenderer<FluidStack> {

    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;
    private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

    private final int capacityMb;
    private final TooltipMode tooltipMode;
    private final int width;
    private final int height;
    @Nullable
    private final IDrawable overlay;

    enum TooltipMode {
        SHOW_AMOUNT,
        SHOW_AMOUNT_AND_CAPACITY,
        ITEM_LIST
    }

    public FluidStackRenderer() {
        this(FluidContainerRegistry.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEX_WIDTH, TEX_HEIGHT, null);
    }

    public FluidStackRenderer(int capacityMb, boolean showCapacity, int width, int height,
        @Nullable IDrawable overlay) {
        this(
            capacityMb,
            showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT,
            width,
            height,
            overlay);
    }

    public FluidStackRenderer(int capacityMb, TooltipMode tooltipMode, int width, int height,
        @Nullable IDrawable overlay) {
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
        this.overlay = overlay;
    }

    @Override
    public void render(Minecraft minecraft, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        drawFluid(minecraft, xPosition, yPosition, fluidStack);

        GlStateManager.color(1, 1, 1, 1);

        if (overlay != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 200);
            overlay.draw(minecraft, xPosition, yPosition);
            GlStateManager.popMatrix();
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    private void drawFluid(Minecraft minecraft, final int xPosition, final int yPosition,
        @Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            return;
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return;
        }

        IIcon fluidStillIcon = getStillFluidIcon(fluidStack, fluid);

        int fluidColor = fluid.getColor(fluidStack);

        int scaledAmount = (fluidStack.amount * height) / capacityMb;
        if (fluidStack.amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
            scaledAmount = MIN_FLUID_HEIGHT;
        }
        if (scaledAmount > height) {
            scaledAmount = height;
        }

        minecraft.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        setGLColorFromInt(fluidColor);

        final int xTileCount = width / TEX_WIDTH;
        final int xRemainder = width - (xTileCount * TEX_WIDTH);
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);

        final int yStart = yPosition + height;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int drawWidth = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
                int drawHeight = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
                int x = xPosition + (xTile * TEX_WIDTH);
                int y = yStart - ((yTile + 1) * TEX_HEIGHT);

                if (drawWidth > 0 && drawHeight > 0) {
                    int maskTop = TEX_HEIGHT - drawHeight;
                    int maskRight = TEX_WIDTH - drawWidth;

                    drawFluidTexture(x, y, fluidStillIcon, maskTop, maskRight, 100);
                }
            }
        }
    }

    private static IIcon getStillFluidIcon(FluidStack fluidStack, Fluid fluid) {
        IIcon icon = fluid.getIcon(fluidStack);
        if (icon == null) {
            icon = fluid.getIcon();
        }
        if (icon == null) {
            icon = Blocks.water.getIcon(0, 0);
        }
        return icon;
    }

    private static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GL11.glColor4f(red, green, blue, 1.0F);
    }

    private static void drawFluidTexture(double xCoord, double yCoord, IIcon icon, int maskTop, int maskRight,
        double zLevel) {
        double uMin = (double) icon.getMinU();
        double uMax = (double) icon.getMaxU();
        double vMin = (double) icon.getMinV();
        double vMax = (double) icon.getMaxV();
        uMax = uMax - (maskRight / 16.0 * (uMax - uMin));
        vMax = vMax - (maskTop / 16.0 * (vMax - vMin));

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(xCoord, yCoord + 16, zLevel, uMin, vMax);
        tessellator.addVertexWithUV(xCoord + 16 - maskRight, yCoord + 16, zLevel, uMax, vMax);
        tessellator.addVertexWithUV(xCoord + 16 - maskRight, yCoord + maskTop, zLevel, uMax, vMin);
        tessellator.addVertexWithUV(xCoord, yCoord + maskTop, zLevel, uMin, vMin);
        tessellator.draw();
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, FluidStack fluidStack, boolean tooltipFlag) {
        List<String> tooltip = new ArrayList<>();
        Fluid fluidType = fluidStack.getFluid();
        if (fluidType == null) {
            return tooltip;
        }

        String fluidName = fluidType.getLocalizedName(fluidStack);
        tooltip.add(fluidName);

        if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
            String amount = Translator
                .translateToLocalFormatted("jfmuy.tooltip.liquid.amount.with.capacity", fluidStack.amount, capacityMb);
            tooltip.add(EnumChatFormatting.GRAY + amount);
        } else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
            String amount = Translator.translateToLocalFormatted("jfmuy.tooltip.liquid.amount", fluidStack.amount);
            tooltip.add(EnumChatFormatting.GRAY + amount);
        }

        return tooltip;
    }
}
