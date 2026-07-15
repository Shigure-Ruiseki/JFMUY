package ruiseki.jfmuy.color;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.util.MathUtil;

public class ColorGetter {

    private static final String[] defaultColors = new String[] { "White:EEEEEE", "LightBlue:7492cc", "Cyan:00EEEE",
        "Blue:2222dd", "LapisBlue:25418b", "Teal:008080", "Yellow:cacb58", "GoldenYellow:EED700", "Orange:d97634",
        "Pink:D1899D", "HotPink:FC0FC0", "Magenta:b24bbb", "Purple:813eb9", "JadedPurple:43324f", "EvilPurple:2e1649",
        "Lavender:B57EDC", "Indigo:480082", "Sand:dbd3a0", "Tan:bb9b63", "LightBrown:A0522D", "Brown:634b33",
        "DarkBrown:3a2d13", "LimeGreen:43b239", "SlimeGreen:83cb73", "Green:008000", "DarkGreen:224d22",
        "GrassGreen:548049", "Red:963430", "BrickRed:b0604b", "NetherBrick:2a1516", "Redstone:ce3e36", "Black:181515",
        "CharcoalGray:464646", "IronGray:646464", "Gray:808080", "Silver:C0C0C0" };

    private static Field framesTextureDataField = null;

    static {
        try {
            try {
                framesTextureDataField = TextureAtlasSprite.class.getDeclaredField("framesTextureData");
            } catch (NoSuchFieldException e) {
                framesTextureDataField = TextureAtlasSprite.class.getDeclaredField("field_110976_a");
            }
            framesTextureDataField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ColorGetter() {

    }

    @NotNull
    public static String[] getColorDefaults() {
        return defaultColors;
    }

    public static List<Color> getColors(ItemStack itemStack, int colorCount) {
        try {
            return unsafeGetColors(itemStack, colorCount);
        } catch (RuntimeException | LinkageError ignored) {
            return Collections.emptyList();
        }
    }

    @NotNull
    public static List<Color> unsafeGetColors(@NotNull ItemStack itemStack, int colorCount) {
        final Item item = itemStack.getItem();
        if (item == null) {
            return Collections.emptyList();
        } else if (item instanceof ItemBlock) {
            final Block block = Block.getBlockFromItem(item);
            if (block == null) {
                return Collections.emptyList();
            }
            return getBlockColors(itemStack, block, colorCount);
        } else {
            return getItemColors(itemStack, item, colorCount);
        }
    }

    @NotNull
    private static List<Color> getItemColors(@NotNull ItemStack itemStack, @NotNull Item item, int colorCount) {
        final int renderColor = item.getColorFromItemStack(itemStack, 0);

        final IIcon icon = item.getIconIndex(itemStack);
        final TextureAtlasSprite textureAtlasSprite = getTextureAtlasSprite(icon);
        if (textureAtlasSprite == null) {
            return Collections.emptyList();
        }
        return getColors(textureAtlasSprite, renderColor, colorCount);
    }

    @NotNull
    private static List<Color> getBlockColors(@NotNull ItemStack itemStack, @NotNull Block block, int colorCount) {
        final int meta = itemStack.getItemDamage();

        final int renderColor = block.getRenderColor(meta);

        final IIcon icon = block.getIcon(2, meta);
        final TextureAtlasSprite textureAtlasSprite = getTextureAtlasSprite(icon);
        if (textureAtlasSprite == null) {
            return Collections.emptyList();
        }
        return getColors(textureAtlasSprite, renderColor, colorCount);
    }

    @NotNull
    public static List<Color> getColors(@NotNull TextureAtlasSprite textureAtlasSprite, int renderColor,
        int colorCount) {
        final BufferedImage bufferedImage = getBufferedImage(textureAtlasSprite);
        if (bufferedImage == null) {
            return Collections.emptyList();
        }
        final List<Color> colors = new ArrayList<>(colorCount);
        final int[][] palette = ColorThief.getPalette(bufferedImage, colorCount);
        if (palette != null) {
            for (int[] colorInt : palette) {
                int red = (int) ((colorInt[0] - 1) * (float) (renderColor >> 16 & 255) / 255.0F);
                int green = (int) ((colorInt[1] - 1) * (float) (renderColor >> 8 & 255) / 255.0F);
                int blue = (int) ((colorInt[2] - 1) * (float) (renderColor & 255) / 255.0F);
                red = MathUtil.clamp(red, 0, 255);
                green = MathUtil.clamp(green, 0, 255);
                blue = MathUtil.clamp(blue, 0, 255);
                Color color = new Color(red, green, blue);
                colors.add(color);
            }
        }
        return colors;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static BufferedImage getBufferedImage(@NotNull TextureAtlasSprite textureAtlasSprite) {
        final int iconWidth = textureAtlasSprite.getIconWidth();
        final int iconHeight = textureAtlasSprite.getIconHeight();

        List<int[][]> framesTextureData = null;
        if (framesTextureDataField != null) {
            try {
                framesTextureData = (List<int[][]>) framesTextureDataField.get(textureAtlasSprite);
            } catch (Exception ignored) {}
        }

        if (framesTextureData == null || framesTextureData.isEmpty()) {
            return null;
        }

        final int frameCount = framesTextureData.size();
        if (iconWidth <= 0 || iconHeight <= 0) {
            return null;
        }

        BufferedImage bufferedImage = new BufferedImage(
            iconWidth,
            iconHeight * frameCount,
            BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < frameCount; i++) {
            int[][] frameTextureData = framesTextureData.get(i);
            int[] largestMipMapTextureData = frameTextureData[0];
            bufferedImage.setRGB(0, i * iconHeight, iconWidth, iconHeight, largestMipMapTextureData, 0, iconWidth);
        }

        return bufferedImage;
    }

    @Nullable
    private static TextureAtlasSprite getTextureAtlasSprite(IIcon icon) {
        if (icon instanceof TextureAtlasSprite sprite) {
            if ("missingno".equals(sprite.getIconName())) {
                return null;
            }
            return sprite;
        }
        return null;
    }
}
