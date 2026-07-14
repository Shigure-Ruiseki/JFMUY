package ruiseki.jfmuy.gui.ingredients;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;

public class GuiIngredientFast {

    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation(
        "textures/misc/enchanted_item_glint.png");
    private static final int blacklistItemColor = Color.yellow.getRGB();
    private static final int blacklistWildColor = Color.red.getRGB();
    private static final int blacklistModColor = Color.blue.getRGB();

    private final Rectangle area;
    private final int padding;
    private static final RenderItem itemRender = RenderItem.getInstance();

    @Nullable
    private Object ingredient;

    public GuiIngredientFast(int xPosition, int yPosition, int padding) {
        this.padding = padding;
        final int size = 16 + (2 * padding);
        this.area = new Rectangle(xPosition, yPosition, size, size);
    }

    public Rectangle getArea() {
        return area;
    }

    public void setIngredient(Object ingredient) {
        this.ingredient = ingredient;
    }

    @Nullable
    public Object getIngredient() {
        return ingredient;
    }

    public void clear() {
        this.ingredient = null;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return (ingredient != null) && area.contains(mouseX, mouseY);
    }

    public void renderItemAndEffectIntoGUI() {
        if (ingredient == null) {
            return;
        }

        if (!(ingredient instanceof ItemStack)) {
            return;
        }

        final ItemStack itemStack = (ItemStack) ingredient;

        try {
            renderItemAndEffectIntoGUI(itemStack);
        } catch (RuntimeException e) {
            throw createRenderIngredientException(e, itemStack);
        } catch (LinkageError e) {
            throw createRenderIngredientException(e, itemStack);
        }
    }

    private void renderItemAndEffectIntoGUI(ItemStack itemStack) {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (Config.isEditModeEnabled()) {
            renderEditMode(itemStack, area, padding);
            GL11.glEnable(GL11.GL_BLEND);
        }

        int x = area.x + padding;
        int y = area.y + padding;
        itemRender.renderItemAndEffectIntoGUI(minecraft.fontRenderer, minecraft.getTextureManager(), itemStack, x, y);
    }

    public void renderSlow() {
        if (ingredient != null) {
            if (Config.isEditModeEnabled()) {
                renderEditMode(ingredient, area, padding);
            }

            try {
                renderSlow(ingredient, area, padding);
            } catch (RuntimeException e) {
                throw createRenderIngredientException(e, ingredient);
            } catch (LinkageError e) {
                throw createRenderIngredientException(e, ingredient);
            }
        }
    }

    private static <T> void renderSlow(T ingredient, Rectangle area, int padding) {
        IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
        IIngredientRenderer<T> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
        ingredientRenderer.render(Minecraft.getMinecraft(), area.x + padding, area.y + padding, ingredient);
    }

    public void renderOverlay(Minecraft minecraft) {
        if (ingredient == null) {
            return;
        }

        if (!(ingredient instanceof ItemStack)) {
            return;
        }

        ItemStack itemStack = (ItemStack) ingredient;
        try {
            renderOverlay(minecraft, itemStack);
        } catch (RuntimeException e) {
            throw createRenderIngredientException(e, itemStack);
        } catch (LinkageError e) {
            throw createRenderIngredientException(e, itemStack);
        }
    }

    private void renderOverlay(Minecraft minecraft, ItemStack itemStack) {
        FontRenderer font = getFontRenderer(minecraft, itemStack);
        itemRender.renderItemOverlayIntoGUI(
            font,
            minecraft.getTextureManager(),
            itemStack,
            area.x + padding,
            area.y + padding,
            null);
    }

    private static <V> void renderEditMode(V ingredient, Rectangle area, int padding) {
        IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
        IIngredientHelper ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

        if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.ITEM, ingredientHelper)) {
            GuiScreen.drawRect(
                area.x + padding,
                area.y + padding,
                area.x + 8 + padding,
                area.y + 16 + padding,
                blacklistItemColor);
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
        if (Config
            .isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.WILDCARD, ingredientHelper)) {
            GuiScreen.drawRect(
                area.x + 8 + padding,
                area.y + padding,
                area.x + 16 + padding,
                area.y + 16 + padding,
                blacklistWildColor);
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
        if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.MOD_ID, ingredientHelper)) {
            GuiScreen.drawRect(
                area.x + padding,
                area.y + 8 + padding,
                area.x + 16 + padding,
                area.y + 16 + padding,
                blacklistModColor);
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
    }

    public static FontRenderer getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
        Item item = itemStack.getItem();
        FontRenderer fontRenderer = item.getFontRenderer(itemStack);
        if (fontRenderer == null) {
            fontRenderer = minecraft.fontRenderer;
        }
        return fontRenderer;
    }

    public void drawHovered(Minecraft minecraft) {
        if (ingredient == null) {
            return;
        }

        renderSlow();
        renderOverlay(minecraft);
        drawHighlight();
    }

    public void drawHighlight() {
        if (ingredient == null) {
            return;
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColorMask(true, true, true, false);

        GuiScreen.drawRect(area.x, area.y, area.x + area.width, area.y + area.height, 0x80FFFFFF);

        GL11.glColorMask(true, true, true, true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public void drawTooltip(Minecraft minecraft, int mouseX, int mouseY) {
        if (ingredient == null) {
            return;
        }

        drawTooltip(minecraft, ingredient, mouseX, mouseY);
    }

    private static <V> void drawTooltip(Minecraft minecraft, V ingredient, int mouseX, int mouseY) {
        IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
        IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
        IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
        List<String> tooltip = getTooltip(minecraft, ingredient, ingredientRenderer, ingredientHelper);
        FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, ingredient);

        TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
    }

    private static <V> List<String> getTooltip(Minecraft minecraft, V ingredient,
        IIngredientRenderer<V> ingredientRenderer, IIngredientHelper<V> ingredientHelper) {
        List<String> list;
        try {
            list = ingredientRenderer.getTooltip(minecraft, ingredient);
        } catch (RuntimeException e) {
            Log.error("Tooltip crashed.", e);
            list = new ArrayList<String>();
            list.add(EnumChatFormatting.RED + Translator.translateToLocal("jfmuy.tooltip.error.crash"));
        } catch (LinkageError e) {
            Log.error("Tooltip crashed.", e);
            list = new ArrayList<String>();
            list.add(EnumChatFormatting.RED + Translator.translateToLocal("jfmuy.tooltip.error.crash"));
        }

        list = Internal.getModIdUtil()
            .addModNameToIngredientTooltip(list, ingredient, ingredientHelper);

        int maxWidth = Reference.MAX_TOOLTIP_WIDTH;
        for (String tooltipLine : list) {
            int width = minecraft.fontRenderer.getStringWidth(tooltipLine);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        if (Config.isEditModeEnabled()) {
            list.add("");
            list.add(EnumChatFormatting.ITALIC + Translator.translateToLocal("gui.jfmuy.editMode.description"));

            String controlKeyLocalization = Translator.translateToLocal("key.jfmuy.ctrl");

            if (Config
                .isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.ITEM, ingredientHelper)) {
                String message = Translator.translateToLocal("gui.jfmuy.editMode.description.show")
                    .replace("%CTRL", controlKeyLocalization);
                String description = EnumChatFormatting.YELLOW + message;
                list.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
            } else {
                String message = Translator.translateToLocal("gui.jfmuy.editMode.description.hide")
                    .replace("%CTRL", controlKeyLocalization);
                String description = EnumChatFormatting.YELLOW + message;
                list.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
            }
        }

        return list;
    }

    private static <T> ReportedException createRenderIngredientException(Throwable throwable, final T ingredient) {
        final IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry()
            .getIngredientHelper(ingredient);
        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering ingredient");
        CrashReportCategory crashreportcategory = crashreport.makeCategory("Ingredient being rendered");

        crashreportcategory.addCrashSectionCallable("Ingredient Mod", new Callable<>() {

            @Override
            public String call() throws Exception {
                return Internal.getModIdUtil()
                    .getModNameForIngredient(ingredient, ingredientHelper);
            }
        });
        crashreportcategory.addCrashSectionCallable("Ingredient Info", new Callable<>() {

            @Override
            public String call() throws Exception {
                return ingredientHelper.getErrorInfo(ingredient);
            }
        });
        throw new ReportedException(crashreport);
    }
}
