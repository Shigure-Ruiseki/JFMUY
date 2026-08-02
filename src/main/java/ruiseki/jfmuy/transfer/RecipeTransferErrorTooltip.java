package ruiseki.jfmuy.transfer;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.jfmuy.api.gui.IRecipeLayout;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferError;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.util.Translator;

public class RecipeTransferErrorTooltip implements IRecipeTransferError {

    private final List<String> message = new ArrayList<>();
    private final String reason;

    public RecipeTransferErrorTooltip(String message) {
        this.reason = message;
        this.message.add(Translator.translateToLocal("jfmuy.tooltip.transfer"));
        this.message.add(EnumChatFormatting.RED + message);
    }

    /**
     * The bare reason, without the heading this error draws for itself, for callers folding it into a
     * tooltip of their own — the bookmark group tooltip says why a chain cannot be crafted where the
     * player is standing.
     */
    public String getReason() {
        return reason;
    }

    @Nullable
    @Override
    public String getSimpleReason() {
        return getReason();
    }

    @Override
    public Type getType() {
        return Type.USER_FACING;
    }

    @Override
    public void showError(Minecraft minecraft, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX,
        int recipeY) {
        TooltipRenderer.drawHoveringText(minecraft, message, mouseX, mouseY, 150);
    }
}
