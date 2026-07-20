package ruiseki.jfmuy.gui.overlay;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.JFMUY;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.gui.ingredients.GuiItemStackGroup;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.ingredients.group.CollapsedGroupIngredient;
import ruiseki.jfmuy.input.ClickedIngredient;
import ruiseki.jfmuy.input.IClickedIngredient;
import ruiseki.jfmuy.input.IShowsRecipeFocuses;
import ruiseki.jfmuy.input.MouseHelper;
import ruiseki.jfmuy.network.PacketDeletePlayerItem;
import ruiseki.jfmuy.render.CollapsedGroupRenderer;
import ruiseki.jfmuy.render.IngredientListBatchRenderer;
import ruiseki.jfmuy.render.IngredientListSlot;
import ruiseki.jfmuy.render.IngredientRenderer;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.util.GiveMode;
import ruiseki.jfmuy.util.MathUtil;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.client.renderer.GlStateManager;
import ruiseki.okcore.helper.ItemHandlerHelpers;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 */
public class IngredientGrid implements IShowsRecipeFocuses {

    public static final int INGREDIENT_PADDING = 1;
    public static final int INGREDIENT_WIDTH = GuiItemStackGroup.getWidth(INGREDIENT_PADDING);
    public static final int INGREDIENT_HEIGHT = GuiItemStackGroup.getHeight(INGREDIENT_PADDING);
    private final GridAlignment alignment;

    private Rectangle area = new Rectangle();
    protected final IngredientListBatchRenderer guiIngredientSlots;
    protected final IngredientGridHistoryProvider historyProvider;

    public IngredientGrid(IngredientListBatchRenderer guiIngredientSlots, GridAlignment alignment, boolean enableHistory) {
        this.alignment = alignment;
        this.guiIngredientSlots = guiIngredientSlots;
        this.historyProvider = new IngredientGridHistoryProvider(enableHistory);
    }

    public IngredientGrid(GridAlignment alignment) { // Left in for compatibility with JFMUY Utilities
        this(new IngredientListBatchRenderer(), alignment, false);
    }

    public int size() {
        return this.guiIngredientSlots.getMaxSize();
    }

    public boolean updateBounds(Rectangle availableArea, int minWidth, Collection<Rectangle> exclusionAreas) {
        final int columns = Math.min(availableArea.width / INGREDIENT_WIDTH, Config.getMaxColumns());
        final int rows = availableArea.height / INGREDIENT_HEIGHT;

        final int ingredientsWidth = columns * INGREDIENT_WIDTH;
        final int width = Math.max(ingredientsWidth, minWidth);
        final int height = rows * INGREDIENT_HEIGHT;
        final int x;
        if (alignment == GridAlignment.LEFT) {
            x = availableArea.x + (availableArea.width - width);
        } else {
            x = availableArea.x;
        }
        final int y = availableArea.y + (availableArea.height - height) / 2;
        final int xOffset = x + Math.max(0, (width - ingredientsWidth) / 2);

        this.area = new Rectangle(x, y, width, height);
        this.guiIngredientSlots.clear();

        if (rows == 0 || columns < Config.smallestNumColumns) {
            return false;
        }

        if (!historyProvider.updateBoundsExtra(
            columns,
            rows,
            y,
            xOffset,
            exclusionAreas,
            this.guiIngredientSlots)) {

            for (int row = 0; row < rows; row++) {
                List<IngredientListSlot> ingredientRow = new ArrayList<>();
                int y1 = y + (row * INGREDIENT_HEIGHT);
                for (int column = 0; column < columns; column++) {
                    int x1 = xOffset + (column * INGREDIENT_WIDTH);
                    IngredientListSlot ingredientListSlot = new IngredientListSlot(x1, y1, INGREDIENT_PADDING);
                    Rectangle stackArea = ingredientListSlot.getArea();
                    final boolean blocked = MathUtil.intersects(exclusionAreas, stackArea);
                    ingredientListSlot.setBlocked(blocked);
                    ingredientRow.add(ingredientListSlot);
                }
                this.guiIngredientSlots.add(ingredientRow);
            }
        }
        return true;
    }

    public void invalidateBuffer() {
        this.guiIngredientSlots.invalidateBuffer();
    }

    public Rectangle getArea() {
        return area;
    }

    public void draw(Minecraft minecraft, int mouseX, int mouseY) {
        GlStateManager.disableBlend();

        guiIngredientSlots.render(minecraft);
        guiIngredientSlots.renderExpandedGroupOutlines();

        if (historyProvider.isEnabled()) {
            historyProvider.drawExtra(minecraft);
        }

        if (!shouldDeleteItemOnClick(minecraft, mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
            CollapsedGroupRenderer collapsedHovered = guiIngredientSlots.getHoveredCollapsed(mouseX, mouseY);
            if (collapsedHovered != null) {
                collapsedHovered.drawHighlight();
            } else {
                IngredientRenderer<?> hovered = guiIngredientSlots.getHovered(mouseX, mouseY);
                if (hovered != null) {
                    hovered.drawHighlight();
                }
            }
        }

        GlStateManager.enableAlpha();
    }

    public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            if (shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
                String deleteItem = Translator.translateToLocal("jfmuy.tooltip.delete.item");
                TooltipRenderer.drawHoveringText(minecraft, deleteItem, mouseX, mouseY);
            } else {
                CollapsedGroupRenderer collapsedHovered = guiIngredientSlots.getHoveredCollapsed(mouseX, mouseY);
                if (collapsedHovered != null) {
                    collapsedHovered.drawTooltip(minecraft, mouseX, mouseY);
                } else {
                    IngredientRenderer<?> hovered = guiIngredientSlots.getHovered(mouseX, mouseY);
                    if (hovered != null) {
                        CollapsedGroupIngredient expandedGroup = guiIngredientSlots
                            .getExpandedCollapsedGroupAt(mouseX, mouseY);
                        if (expandedGroup != null) {
                            String hint = EnumChatFormatting.YELLOW + Translator.translateToLocal("jfmuy.tooltip.collapsed.collapse");
                            hovered.drawTooltip(minecraft, mouseX, mouseY, Collections.singletonList(hint));
                        } else {
                            hovered.drawTooltip(minecraft, mouseX, mouseY);
                        }
                    }

                    if (historyProvider.isEnabled()) {
                        historyProvider.drawTooltipsExtra(minecraft, mouseX, mouseY);
                    }
                }
            }
        }
    }

    private boolean shouldDeleteItemOnClick(Minecraft minecraft, int mouseX, int mouseY) {
        if (Config.isDeleteItemsInCheatModeActive()) {
            EntityPlayer player = minecraft.thePlayer;
            if (player != null) {
                ItemStack itemStack = player.inventory.getItemStack();
                if (itemStack != null) {
                    JFMUYRuntime runtime = Internal.getRuntime();
                    if (runtime == null || !runtime.getRecipesGui()
                        .isOpen()) {
                        GiveMode giveMode = Config.getGiveMode();
                        if (giveMode == GiveMode.MOUSE_PICKUP) {
                            IClickedIngredient<?> ingredientUnderMouse = getIngredientUnderMouse(mouseX, mouseY);
                            if (ingredientUnderMouse != null) {
                                if (ingredientUnderMouse.getValue() instanceof ItemStack) {
                                    ItemStack value = (ItemStack) ingredientUnderMouse.getValue();
                                    if (ItemHandlerHelpers.canItemStacksStack(itemStack, value)) {
                                        return false;
                                    }
                                }
                                return ingredientUnderMouse.replaceWithCheatItemStack(itemStack) == null;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return area.contains(mouseX, mouseY);
    }

    public boolean handleMouseClicked(int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
                EntityPlayerSP player = minecraft.thePlayer;
                if (player != null) {
                    ItemStack itemStack = player.inventory.getItemStack();
                    if (itemStack != null) {
                        player.inventory.setItemStack(null);
                        PacketDeletePlayerItem packet = new PacketDeletePlayerItem(itemStack);
                        JFMUY.instance.getPacketHandler()
                            .sendToServer(packet);
                        return true;
                    }
                }
            }
            return handleCollapsedGroupClicked(mouseX, mouseY);
        }
        return false;
    }

    protected boolean handleCollapsedGroupClicked(int mouseX, int mouseY) {
        return Internal.getCollapsedGroupRegistry()
            .handleMouseClicked(guiIngredientSlots, mouseX, mouseY);
    }

    @Nullable
    public IIngredientListElement<?> getElementUnderMouse() {
        IngredientRenderer<?> hovered = guiIngredientSlots.getHovered(MouseHelper.getX(), MouseHelper.getY());
        if (hovered != null) {
            return hovered.getElement();
        }
        return null;
    }

    @Override
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        IClickedIngredient<?> result;
        if (isMouseOver(mouseX, mouseY)) {
            ClickedIngredient<?> clicked = guiIngredientSlots.getIngredientUnderMouse(mouseX, mouseY);
            if (clicked != null) {
                clicked.setAllowsCheating();
            }
            result = clicked;
        } else {
            result = null;
        }

        if (historyProvider.isEnabled()) {
            result = historyProvider.getIngredientUnderMouseExtra(result, mouseX, mouseY);
        }

        return result;
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return true;
    }
}
