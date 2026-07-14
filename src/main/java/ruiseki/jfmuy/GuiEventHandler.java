package ruiseki.jfmuy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.config.OverlayToggleEvent;
import ruiseki.jfmuy.gui.ItemListOverlay;
import ruiseki.jfmuy.gui.ItemListOverlayInternal;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.input.InputHandler;
import ruiseki.jfmuy.util.Translator;
import ruiseki.okcore.event.gui.BackgroundDrawnEvent;
import ruiseki.okcore.event.gui.PotionShiftEvent;
import ruiseki.okcore.event.input.KeyboardInputEvent;
import ruiseki.okcore.event.input.MouseInputEvent;

public class GuiEventHandler {

    @Nonnull
    private static final String showRecipesText = Translator.translateToLocal("jfmuy.tooltip.show.recipes");
    private final JFMUYRuntime runtime;
    @Nullable
    private InputHandler inputHandler;
    @Nullable
    private GuiContainer previousGui = null;

    public GuiEventHandler(JFMUYRuntime runtime) {
        this.runtime = runtime;
    }

    @SubscribeEvent
    public void onOverlayToggle(OverlayToggleEvent event) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        onNewScreen(currentScreen);
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.gui;
        onNewScreen(gui);
    }

    private void onNewScreen(@Nullable GuiScreen screen) {
        if (screen instanceof GuiContainer || screen instanceof RecipesGui) {
            ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
            ItemListOverlayInternal itemListOverlayInternal = itemListOverlay.create(screen);
            inputHandler = new InputHandler(runtime, itemListOverlayInternal);
        } else {
            inputHandler = null;
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
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
                inputHandler = null;
            }
        }
    }

    @SubscribeEvent
    public void onDrawBackgroundEventPost(@Nonnull BackgroundDrawnEvent event) {
        ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
        ItemListOverlayInternal itemListOverlayInternal = itemListOverlay.getInternal();
        if (itemListOverlayInternal != null) {
            GuiScreen gui = event.gui;
            if (itemListOverlayInternal.hasScreenChanged(gui)) {
                itemListOverlayInternal = itemListOverlay.create(gui);
                inputHandler = new InputHandler(runtime, itemListOverlayInternal);
            }

            if (itemListOverlayInternal != null) {
                itemListOverlayInternal.drawScreen(gui.mc, event.getMouseX(), event.getMouseY());
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiScreen gui = event.gui;
        if (gui instanceof GuiContainer) {
            GuiContainer guiContainer = (GuiContainer) gui;
            RecipeRegistry recipeRegistry = runtime.getRecipeRegistry();
            if (recipeRegistry.getRecipeClickableArea(
                guiContainer,
                event.mouseX - guiContainer.guiLeft,
                event.mouseY - guiContainer.guiTop) != null) {
                TooltipRenderer.drawHoveringText(guiContainer.mc, showRecipesText, event.mouseX, event.mouseY);
            }
        }

        ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
        ItemListOverlayInternal itemListOverlayInternal = itemListOverlay.getInternal();
        if (itemListOverlayInternal != null) {
            itemListOverlayInternal.drawTooltips(gui.mc, event.mouseX, event.mouseY);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }

        ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
        ItemListOverlayInternal itemListOverlayInternal = itemListOverlay.getInternal();
        if (itemListOverlayInternal != null) {
            itemListOverlayInternal.handleTick();
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

    @SubscribeEvent
    public void onPotionShiftEvent(PotionShiftEvent event) {
        if (Config.isOverlayEnabled()) {
            event.setCanceled(true);
        }
    }
}
