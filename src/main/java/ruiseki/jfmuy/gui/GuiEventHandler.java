package ruiseki.jfmuy.gui;

import java.time.Duration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.OverlayToggleEvent;
import ruiseki.jfmuy.gui.ghost.GhostIngredientDragManager;
import ruiseki.jfmuy.gui.overlay.IngredientListOverlay;
import ruiseki.jfmuy.gui.overlay.bookmarks.LeftAreaDispatcher;
import ruiseki.jfmuy.recipes.RecipeRegistry;
import ruiseki.jfmuy.util.LimitedLogger;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.event.gui.BackgroundDrawnEvent;
import ruiseki.okcore.event.gui.GuiContainerEvent;
import ruiseki.okcore.event.gui.PotionShiftEvent;

public class GuiEventHandler {

    private final IngredientListOverlay ingredientListOverlay;
    private final GuiScreenHelper guiScreenHelper;
    private final LeftAreaDispatcher leftAreaDispatcher;
    private final RecipeRegistry recipeRegistry;
    private final LimitedLogger missingBackgroundLogger = new LimitedLogger(Log.get(), Duration.ofHours(1));
    private final GhostIngredientDragManager ghostIngredientDragManager;
    private boolean drawnOnBackground = false;

    public GuiEventHandler(GuiScreenHelper guiScreenHelper, LeftAreaDispatcher leftAreaDispatcher,
        IngredientListOverlay ingredientListOverlay, RecipeRegistry recipeRegistry,
        GhostIngredientDragManager ghostIngredientDragManager) {
        this.guiScreenHelper = guiScreenHelper;
        this.leftAreaDispatcher = leftAreaDispatcher;
        this.ingredientListOverlay = ingredientListOverlay;
        this.recipeRegistry = recipeRegistry;
        this.ghostIngredientDragManager = ghostIngredientDragManager;
    }

    @SubscribeEvent
    public void onOverlayToggle(OverlayToggleEvent event) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        ingredientListOverlay.updateScreen(currentScreen, true);
        leftAreaDispatcher.updateScreen(currentScreen, false);
        ghostIngredientDragManager.updateScreen(currentScreen, false);
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.gui;
        ingredientListOverlay.updateScreen(gui, false);
        leftAreaDispatcher.updateScreen(gui, false);
        ghostIngredientDragManager.updateScreen(gui, false);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.gui;
        ingredientListOverlay.updateScreen(gui, false);
        leftAreaDispatcher.updateScreen(gui, false);
        ghostIngredientDragManager.updateScreen(gui, false);
    }

    @SubscribeEvent
    public void onDrawBackgroundEventPost(BackgroundDrawnEvent event) {
        GuiScreen gui = event.gui;
        Minecraft minecraft = gui.mc;
        if (minecraft == null) {
            return;
        }
        boolean exclusionAreasChanged = guiScreenHelper.updateGuiExclusionAreas();
        ingredientListOverlay.updateScreen(gui, exclusionAreasChanged);
        leftAreaDispatcher.updateScreen(gui, exclusionAreasChanged);
        ghostIngredientDragManager.updateScreen(gui, false);

        drawnOnBackground = true;
        ingredientListOverlay.drawScreen(minecraft, event.getMouseX(), event.getMouseY());
        leftAreaDispatcher.drawScreen(minecraft, event.getMouseX(), event.getMouseY());
    }

    /**
     * Draws above most GuiContainer elements, but below the tooltips.
     */
    @SubscribeEvent
    public void onDrawForegroundEvent(GuiContainerEvent.DrawForeground event) {
        GuiContainer gui = event.getGuiContainer();
        Minecraft minecraft = gui.mc;
        if (minecraft == null) {
            return;
        }
        ingredientListOverlay.drawOnForeground(minecraft, gui, event.getMouseX(), event.getMouseY());
        leftAreaDispatcher.drawOnForeground(gui, event.getMouseX(), event.getMouseY());
        ghostIngredientDragManager.drawOnForeground(minecraft, gui, event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiScreen gui = event.gui;
        Minecraft minecraft = gui.mc;
        if (minecraft == null) {
            return;
        }

        ingredientListOverlay.updateScreen(gui, false);
        leftAreaDispatcher.updateScreen(gui, false);
        ghostIngredientDragManager.updateScreen(gui, false);

        if (!drawnOnBackground) {
            if (gui instanceof GuiContainer) {
                String guiName = gui.getClass()
                    .getName();
                missingBackgroundLogger.log(
                    Level.WARN,
                    guiName,
                    "GUI did not draw the dark background layer behind itself, this may result in display issues: {}",
                    guiName);
            }
            ingredientListOverlay.drawScreen(minecraft, event.mouseX, event.mouseY);
            leftAreaDispatcher.drawScreen(minecraft, event.mouseX, event.mouseY);
        }
        drawnOnBackground = false;

        if (gui instanceof GuiContainer) {
            GuiContainer guiContainer = (GuiContainer) gui;
            if (recipeRegistry.getRecipeClickableArea(
                guiContainer,
                event.mouseX - guiContainer.guiLeft,
                event.mouseY - guiContainer.guiTop) != null) {
                String showRecipesText = Translator.translateToLocal("jfmuy.tooltip.show.recipes");
                TooltipRenderer.drawHoveringText(minecraft, showRecipesText, event.mouseX, event.mouseY);
            }
        }

        ingredientListOverlay.drawTooltips(minecraft, event.mouseX, event.mouseY);
        leftAreaDispatcher.drawTooltips(minecraft, event.mouseX, event.mouseY);
        ghostIngredientDragManager.drawTooltips(minecraft, event.mouseX, event.mouseY);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }

        ingredientListOverlay.handleTick();
    }

    @SubscribeEvent
    public void onPotionShiftEvent(PotionShiftEvent event) {
        if (Config.isOverlayEnabled()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBookmarkUpdateEvent(BookmarkUpdateEvent event) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        leftAreaDispatcher.updateScreen(currentScreen, true);
    }
}
