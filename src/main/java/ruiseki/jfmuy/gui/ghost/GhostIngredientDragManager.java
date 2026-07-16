package ruiseki.jfmuy.gui.ghost;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IGhostIngredientHandler;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.bookmarks.DefaultGhostIngredientHandler;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.KeyBindings;
import ruiseki.jfmuy.gui.GuiScreenHelper;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.input.IClickedIngredient;
import ruiseki.okcore.client.renderer.GlStateManager;

public class GhostIngredientDragManager {

    private final GuiScreenHelper guiScreenHelper;
    private final IngredientRegistry ingredientRegistry;
    private final List<GhostIngredientReturning> ghostIngredientsReturning = new ArrayList<>();
    @Nullable
    private GhostIngredientDrag<?> ghostIngredientDrag;
    @Nullable
    private Object hoveredIngredient;
    @Nullable
    private List<IGhostIngredientHandler.Target<Object>> hoveredIngredientTargets;
    private final DefaultGhostIngredientHandler defaultHandler = new DefaultGhostIngredientHandler();
    @Nullable
    private IGhostIngredientHandler<?> hoverHandler;

    public GhostIngredientDragManager(GuiScreenHelper guiScreenHelper, IngredientRegistry ingredientRegistry) {
        this.guiScreenHelper = guiScreenHelper;
        this.ingredientRegistry = ingredientRegistry;
    }

    public void updateScreen(GuiScreen gui, boolean forceUpdate) {
        if (gui == null) {
            this.stopDrag();
        }
    }

    public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
        if (!(minecraft.currentScreen instanceof GuiContainer)) { // guiContainer uses drawOnForeground
            drawGhostIngredientHighlights(minecraft, mouseX, mouseY);
        }
        if (ghostIngredientDrag != null) {
            ghostIngredientDrag.drawItem(minecraft, mouseX, mouseY);
        }
        ghostIngredientsReturning.forEach(returning -> returning.drawItem(minecraft));
        ghostIngredientsReturning.removeIf(GhostIngredientReturning::isComplete);
    }

    public void drawOnForeground(Minecraft minecraft, GuiContainer gui, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-gui.guiLeft, -gui.guiTop, 0);
        drawGhostIngredientHighlights(minecraft, mouseX, mouseY);
        GlStateManager.popMatrix();
    }

    private void drawGhostIngredientHighlights(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.ghostIngredientDrag != null) {
            this.ghostIngredientDrag.drawTargets(mouseX, mouseY);
        } else {
            IIngredientListElement elementUnderMouse = Internal.getInputHandler()
                .getElementUnderMouse();
            Object hovered = elementUnderMouse == null ? null : elementUnderMouse.getIngredient();
            boolean showHighlight = true;
            if (!Objects.equals(hovered, this.hoveredIngredient)) {
                this.hoveredIngredient = hovered;
                this.hoveredIngredientTargets = null;
                GuiScreen currentScreen = minecraft.currentScreen;
                if (currentScreen != null && hovered != null) {
                    IGhostIngredientHandler<GuiScreen> handler = guiScreenHelper
                        .getGhostIngredientHandler(currentScreen);
                    if (handler != null && handler.shouldHighlightTargets()) {
                        this.hoveredIngredientTargets = handler.getTargets(currentScreen, hovered, false);
                        hoverHandler = handler;
                    } else if (handler == null) {
                        this.hoveredIngredientTargets = defaultHandler.getTargets(currentScreen, hovered, false);
                        hoverHandler = defaultHandler;
                    }
                }
            }
            if (hoverHandler == defaultHandler) {
                showHighlight = false;
            }
            if (this.hoveredIngredientTargets != null && !Config.isCheatItemsEnabled() && showHighlight) {
                GhostIngredientDrag.drawTargets(mouseX, mouseY, this.hoveredIngredientTargets);
            }
        }
    }

    public boolean handleMouseClicked(Minecraft minecraft, GuiScreen currentScreen, IClickedIngredient<?> clicked,
        IIngredientListElement<?> listElement, int mouseX, int mouseY) {
        if (this.ghostIngredientDrag != null) {
            boolean success = this.ghostIngredientDrag.onClick(mouseX, mouseY);
            if (!success) {
                GhostIngredientReturning<?> returning = GhostIngredientReturning
                    .create(this.ghostIngredientDrag, mouseX, mouseY);
                this.ghostIngredientsReturning.add(returning);
            }
            this.ghostIngredientDrag = null;
            return success;
        }
        EntityPlayerSP player = minecraft.thePlayer;
        if (player != null && listElement != null) {
            ItemStack mouseItem = player.inventory.getItemStack();
            if (mouseItem == null && this.handleClickGhostIngredient(currentScreen, clicked)) {
                return true;
            }
        }
        return false;
    }

    public void stopDrag() {
        if (this.ghostIngredientDrag != null) {
            this.ghostIngredientDrag.stop();
            this.ghostIngredientDrag = null;
        }
    }

    public <T extends GuiScreen, V> boolean handleClickGhostIngredient(T currentScreen, IClickedIngredient<V> clicked) {
        IGhostIngredientHandler<T> handler = guiScreenHelper.getGhostIngredientHandler(currentScreen);
        if (handler == null) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                return handleClickGhostIngredient(defaultHandler, currentScreen, clicked);
            }
            return false;
        }
        return handleClickGhostIngredient(handler, currentScreen, clicked);
    }

    public <T extends GuiScreen, V> boolean handleClickGhostIngredient(IGhostIngredientHandler<T> handler,
        T currentScreen, IClickedIngredient<V> clicked) {
        V ingredient = clicked.getValue();
        List<IGhostIngredientHandler.Target<V>> targets = handler.getTargets(currentScreen, ingredient, true);
        if (!targets.isEmpty()) {
            IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
            Rectangle clickedArea = clicked.getArea();
            this.ghostIngredientDrag = new GhostIngredientDrag<>(
                handler,
                targets,
                ingredientRenderer,
                ingredient,
                clickedArea);
            clicked.onClickHandled();
            return true;
        }
        return false;
    }

    public boolean handleKeyDown(int eventKey) {
        if (KeyBindings.isInventoryCloseKey(eventKey) || KeyBindings.isEnterKey(eventKey)) {
            stopDrag();
            return true;
        }
        return false;
    }
}
