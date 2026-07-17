package ruiseki.jfmuy.gui.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.startup.ForgeModIdHelper;
import ruiseki.jfmuy.util.LegacyUtil;
import ruiseki.okcore.client.renderer.GlStateManager;

public class RecipeCategoryTab extends RecipeGuiTab {

    private final IRecipeGuiLogic logic;
    private final IRecipeCategory category;

    @Nullable
    private MutableObject<Object> cachedRecipeCatalyst = null;

    public RecipeCategoryTab(IRecipeGuiLogic logic, IRecipeCategory category, int x, int y) {
        super(x, y);
        this.logic = logic;
        this.category = category;
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
        logic.setRecipeCategory(category);
        SoundHandler soundHandler = Minecraft.getMinecraft()
            .getSoundHandler();
        soundHandler.playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
        return true;
    }

    @Override
    public void draw(Minecraft minecraft, boolean selected, int mouseX, int mouseY) {
        super.draw(minecraft, selected, mouseX, mouseY);

        int iconX = x + 4;
        int iconY = y + 4;

        IDrawable icon = category.getIcon();
        if (icon != null) {
            iconX += (16 - icon.getWidth()) / 2;
            iconY += (16 - icon.getHeight()) / 2;
            icon.draw(minecraft, iconX, iconY);
        } else {
            if (this.cachedRecipeCatalyst == null) {
                List<Object> cachedRecipeCatalysts = logic.getRecipeCatalysts(category);
                if (cachedRecipeCatalysts.isEmpty()) {
                    this.cachedRecipeCatalyst = new MutableObject<>(null);
                } else {
                    this.cachedRecipeCatalyst = new MutableObject<>(cachedRecipeCatalysts.getFirst());
                }
            }
            Object ingredient = this.cachedRecipeCatalyst.getValue();
            if (ingredient != null) {
                renderIngredient(minecraft, iconX, iconY, ingredient);
            } else {
                String text = category.getTitle()
                    .substring(0, 2);
                FontRenderer fontRenderer = minecraft.fontRenderer;
                float textCenterX = x + (TAB_WIDTH / 2f);
                float textCenterY = y + (TAB_HEIGHT / 2f) - 3;
                int color = isMouseOver(mouseX, mouseY) ? 16777120 : 14737632;
                fontRenderer.drawStringWithShadow(
                    text,
                    (int) (textCenterX - fontRenderer.getStringWidth(text) / 2f),
                    (int) textCenterY,
                    color);
                GlStateManager.color(1, 1, 1, 1);
            }
        }
    }

    private static <T> void renderIngredient(Minecraft minecraft, int iconX, int iconY, T ingredient) {
        IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
        IIngredientRenderer<T> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
        GlStateManager.enableDepth();
        ingredientRenderer.render(minecraft, iconX, iconY, ingredient);
        GlStateManager.enableAlpha();
        GlStateManager.disableDepth();
    }

    @Override
    public boolean isSelected(IRecipeCategory selectedCategory) {
        return category.getUid()
            .equals(selectedCategory.getUid());
    }

    @Override
    public List<String> getTooltip() {
        List<String> tooltip = new ArrayList<>();
        String title = category.getTitle();
        // noinspection ConstantConditions
        if (title != null) {
            tooltip.add(title);
        }

        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            tooltip.add(EnumChatFormatting.DARK_GRAY + category.getUid());
        }

        String modName = LegacyUtil.getModName(category);
        if (modName != null) {
            modName = ForgeModIdHelper.getInstance()
                .getFormattedModNameForModId(modName);
            if (modName != null) {
                tooltip.add(modName);
            }
        }
        return tooltip;
    }
}
