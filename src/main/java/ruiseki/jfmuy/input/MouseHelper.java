package ruiseki.jfmuy.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class MouseHelper {

    private static class DisplayInfo {

        private final ScaledResolution scaledresolution;
        private final int displayWidth;
        private final int displayHeight;

        public DisplayInfo() {
            Minecraft minecraft = Minecraft.getMinecraft();
            this.displayWidth = minecraft.displayWidth;
            this.displayHeight = minecraft.displayHeight;
            this.scaledresolution = new ScaledResolution(minecraft, this.displayWidth, this.displayHeight);
        }

        public int getX() {
            int i = scaledresolution.getScaledWidth();
            return Mouse.getX() * i / displayWidth;
        }

        public int getY() {
            int j = scaledresolution.getScaledHeight();
            return j - Mouse.getY() * j / displayHeight - 1;
        }

        public boolean isInvalid() {
            Minecraft minecraft = Minecraft.getMinecraft();
            return this.displayWidth != minecraft.displayWidth || this.displayHeight != minecraft.displayHeight;
        }
    }

    private static DisplayInfo INFO = new DisplayInfo();

    public static final MouseHelper INSTANCE = new MouseHelper();

    private MouseHelper() {}

    private static DisplayInfo getInfo() {
        if (INFO.isInvalid()) {
            INFO = new DisplayInfo();
        }
        return INFO;
    }

    public static int getX() {
        return getInfo().getX();
    }

    public static int getY() {
        return getInfo().getY();
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        INFO = new DisplayInfo();
    }
}
