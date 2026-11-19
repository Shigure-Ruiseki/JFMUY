package ruiseki.jfmuy.gui.recipes;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.gui.elements.GuiIconButtonSmall;
import ruiseki.jfmuy.transfer.RecipeTransferErrorInternal;
import ruiseki.jfmuy.transfer.RecipeTransferUtil;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;

public class RecipeTransferButton extends GuiIconButtonSmall {

    private final RecipeLayout recipeLayout;
    @Nullable
    private IRecipeTransferError recipeTransferError;

    public RecipeTransferButton(int id, int xPos, int yPos, int width, int height, IDrawable icon,
        RecipeLayout recipeLayout) {
        super(id, xPos, yPos, width, height, icon);
        this.recipeLayout = recipeLayout;
    }

    public void init(@Nullable Container container, EntityPlayer player) {
        if (container != null) {
            GlStateManager.pushMatrix();
            this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(container, recipeLayout, player);
            GlStateManager.popMatrix();
        } else {
            this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
        }

        if (this.recipeTransferError == null) {
            this.enabled = true;
            this.visible = true;
        } else {
            this.enabled = false;
            IRecipeTransferError.Type type = this.recipeTransferError.getType();
            this.visible = (type == IRecipeTransferError.Type.USER_FACING);
        }
    }

    public void drawToolTip(Minecraft mc, int mouseX, int mouseY) {
        if (field_146123_n && visible) {
            if (recipeTransferError != null) {
                recipeTransferError
                    .showError(mc, mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
            } else {
                String tooltipTransfer = Translator.translateToLocal("jfmuy.tooltip.transfer");
                TooltipRenderer.drawHoveringText(mc, tooltipTransfer, mouseX, mouseY);
            }
        }
    }

    public void drawRecipeError(Minecraft minecraft, int mouseX, int mouseY) { // AE2 enables the button for USER_FACING
                                                                               // errors, whose showError expects to run
                                                                               // only on button hover
        if (recipeTransferError != null && recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
            GlStateManager.pushMatrix();
            try {
                recipeTransferError
                    .showError(minecraft, mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
            } finally {
                GlStateManager.popMatrix();
            }
        }
    }

    public void addShortcutTooltips(List<String> tooltip) {
        if (!enabled) {
            return;
        }
        if (KeyBindings.transferRecipeGui.getKeyCode() != 0) {
            tooltip.add(
                Translator.translateToLocalFormatted(
                    "jfmuy.tooltip.transfer.key",
                    KeyBindings.transferRecipeGui.getDisplayName()));
        }
        if (KeyBindings.maxTransferRecipeGui.getKeyCode() != 0) {
            tooltip.add(
                Translator.translateToLocalFormatted(
                    "jfmuy.tooltip.transfer.max.key",
                    KeyBindings.maxTransferRecipeGui.getDisplayName()));
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        if (this.visible && this.recipeTransferError != null
            && this.recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
            drawRect(
                this.xPosition,
                this.yPosition,
                this.xPosition + this.width,
                this.yPosition + this.height,
                this.recipeTransferError.getButtonHighlightColor());
        }
    }
}
