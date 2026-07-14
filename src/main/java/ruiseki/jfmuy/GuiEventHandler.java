package ruiseki.jfmuy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.jfmuy.gui.ItemListOverlay;
import ruiseki.jfmuy.gui.RecipesGui;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.input.InputHandler;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.event.input.KeyboardInputEvent;
import ruiseki.okcore.event.input.MouseInputEvent;

public class GuiEventHandler {

    @Nonnull
    private static final String showRecipesText = Translator.translateToLocal("jfmuy.tooltip.show.recipes");
    @Nullable
    private InputHandler inputHandler;
    @Nullable
    private GuiContainer previousGui = null;

    @SubscribeEvent
    public void onGuiInit(@Nonnull GuiScreenEvent.InitGuiEvent.Post event) {
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime == null) {
            return;
        }
        ItemListOverlay itemListOverlay = runtime.getItemListOverlay();

        GuiScreen gui = event.gui;
        if (gui instanceof GuiContainer) {
            GuiContainer guiContainer = (GuiContainer) gui;
            itemListOverlay.initGui(guiContainer);

            RecipesGui recipesGui = new RecipesGui();
            inputHandler = new InputHandler(recipesGui, itemListOverlay);
        } else if (gui instanceof RecipesGui) {
            if (inputHandler != null) {
                inputHandler.onScreenResized();
            }
        } else {
            inputHandler = null;
        }
    }

    @SubscribeEvent
    public void onGuiOpen(@Nonnull GuiOpenEvent event) {
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime == null) {
            return;
        }
        ItemListOverlay itemListOverlay = runtime.getItemListOverlay();

        GuiScreen gui = event.gui;
        if (gui instanceof GuiContainer) {
            GuiContainer guiContainer = (GuiContainer) gui;
            if (previousGui != guiContainer) {
                previousGui = guiContainer;
                if (itemListOverlay.isOpen()) {
                    itemListOverlay.close();
                }
            }
        } else if (!(gui instanceof RecipesGui)) {
            if (itemListOverlay.isOpen()) {
                itemListOverlay.close();
            }
        }
    }

    @SubscribeEvent
    public void onDrawBackgroundEventPost(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime == null) {
            return;
        }

        ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
        if (itemListOverlay.isOpen()) {
            GuiScreen gui = event.gui;
            itemListOverlay.updateGui(gui);
            itemListOverlay.drawScreen(gui.mc, event.mouseX, event.mouseY);
        }
    }

    @SubscribeEvent
    public void onDrawScreenEventPost(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime == null) {
            return;
        }

        GuiScreen gui = event.gui;
        if (gui instanceof GuiContainer guiContainer) {
            RecipeRegistry recipeRegistry = Internal.getRuntime()
                .getRecipeRegistry();
            if (recipeRegistry.getRecipeClickableArea(
                guiContainer,
                event.mouseX - guiContainer.guiLeft,
                event.mouseY - guiContainer.guiTop) != null) {
                TooltipRenderer.drawHoveringText(guiContainer.mc, showRecipesText, event.mouseX, event.mouseY);
            }
        }

        ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
        if (itemListOverlay.isOpen()) {
            itemListOverlay.drawTooltips(gui.mc, event.mouseX, event.mouseY);
        }
    }

    @SubscribeEvent
    public void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
        JFMUYRuntime runtime = Internal.getRuntime();
        if (runtime == null) {
            return;
        }

        if (event.phase == TickEvent.Phase.END) {
            return;
        }

        ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
        if (itemListOverlay.isOpen()) {
            itemListOverlay.handleTick();
        }
    }

    @SubscribeEvent
    public void onGuiKeyboardEvent(KeyboardInputEvent.Pre event) {
        if (inputHandler != null) {
            if (inputHandler.handleKeyEvent()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onGuiMouseEvent(MouseInputEvent.Pre event) {
        GuiScreen guiScreen = event.gui;
        if (inputHandler != null) {
            int x = Mouse.getEventX() * guiScreen.width / guiScreen.mc.displayWidth;
            int y = guiScreen.height - Mouse.getEventY() * guiScreen.height / guiScreen.mc.displayHeight - 1;
            if (inputHandler.handleMouseEvent(guiScreen, x, y)) {
                event.setCanceled(true);
            }
        }
    }

    // TODO: Add PotionShiftEvent
    // @SubscribeEvent
    // public void onPotionShiftEvent(GuiScreenEvent.PotionShiftEvent event) {
    // if (Config.isOverlayEnabled()) {
    // event.setCanceled(true);
    // }
    // }
}
