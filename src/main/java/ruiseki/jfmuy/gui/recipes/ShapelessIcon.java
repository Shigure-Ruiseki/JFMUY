package ruiseki.jfmuy.gui.recipes;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.client.config.HoverChecker;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.plugins.vanilla.crafting.CraftingRecipeCategory;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;

public class ShapelessIcon {

    private static final int scale = 4;
    private final IDrawable icon;
    private final HoverChecker hoverChecker;

    public ShapelessIcon() {
        this.icon = Internal.getHelpers()
            .getGuiHelper()
            .getShapelessIcon();
        int iconBottom = icon.getHeight() / scale;
        int iconLeft = CraftingRecipeCategory.width - (icon.getWidth() / scale);
        int iconRight = iconLeft + icon.getWidth() / scale;
        this.hoverChecker = new HoverChecker(0, iconBottom, iconLeft, iconRight, 0);
    }

    public void draw(Minecraft minecraft, int recipeWidth) {
        int shapelessIconX = recipeWidth - (icon.getWidth() / scale);

        GlStateManager.pushMatrix();
        GlStateManager.translate(shapelessIconX, 0, 0);
        GlStateManager.scale(1.0 / scale, 1.0 / scale, 1.0);
        icon.draw(minecraft);
        GlStateManager.popMatrix();
    }

    @Nullable
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (hoverChecker.checkHover(mouseX, mouseY)) {
            return Collections.singletonList(Translator.translateToLocal("jei.tooltip.shapeless.recipe"));
        }
        return null;
    }
}
