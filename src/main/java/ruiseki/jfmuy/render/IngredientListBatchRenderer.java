package ruiseki.jfmuy.render;

import static ruiseki.jfmuy.gui.overlay.IngredientGrid.INGREDIENT_HEIGHT;
import static ruiseki.jfmuy.gui.overlay.IngredientGrid.INGREDIENT_WIDTH;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.input.ClickedIngredient;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.okcore.client.renderer.GlStateManager;

public class IngredientListBatchRenderer {

    protected final List<List<IngredientListSlot>> slots = new ObjectArrayList<>();

    protected final List<ItemStackFastRenderer> renderItems2d = new ArrayList<>();
    protected final List<ItemStackFastRenderer> renderItems3d = new ArrayList<>();
    protected final List<IngredientRenderer> renderOther = new ArrayList<>();

    @Nullable
    private Framebuffer framebuffer = null;
    private boolean refreshBuffer = true;
    protected int size = 0;
    private int width;
    private int maxWidth;
    private int height;

    public void clear() {
        slots.clear();

        renderItems2d.clear();
        renderItems3d.clear();
        renderOther.clear();
        size = 0;

        width = 0;
        maxWidth = 0;
        height = 0;
    }

    public int size() {
        return size;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void add(List<IngredientListSlot> ingredientListSlot) {
        slots.add(ingredientListSlot);
    }

    public List<IngredientListSlot> getAllGuiIngredientSlots() {
        return slots.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    public void set(final int startIndex, List<IIngredientListElement> ingredientList) {
        renderItems2d.clear();
        renderItems3d.clear();
        renderOther.clear();
        size = 0;

        // We need to clear all of them anyway.
        for (List<IngredientListSlot> row : slots) {
            for (IngredientListSlot slot : row) {
                slot.clear();
            }
        }

        int i = startIndex;
        for (List<IngredientListSlot> row : slots) {
            for (int column = 0; column < row.size(); column++) {
                if (i >= ingredientList.size()) {
                    break;
                }
                IIngredientListElement<?> element = ingredientList.get(i);
                IngredientListSlot ingredientListSlot = row.get(column);
                if (ingredientListSlot.isBlocked()) {
                    continue;
                }
                set(ingredientListSlot, element);
                size++;
                i++;
            }
        }

        invalidateBuffer();
    }

    public void invalidateBuffer() {
        refreshBuffer = true;
    }

    protected <V> void set(IngredientListSlot ingredientListSlot, IIngredientListElement<V> element) {
        ingredientListSlot.clear();

        V ingredient = element.getIngredient();
        if (ingredient instanceof ItemStack) {
            // noinspection unchecked
            IIngredientListElement<ItemStack> itemStackElement = (IIngredientListElement<ItemStack>) element;
            ItemStack itemStack = itemStackElement.getIngredient();

            if (itemStack == null || itemStack.getItem() == null) {
                return;
            }

            boolean is3d = false;
            try {
                Item item = itemStack.getItem();
                if (item instanceof ItemBlock) {
                    Block block = Block.getBlockFromItem(item);
                    if (block != null) {
                        int renderType = block.getRenderType();
                        if (RenderBlocks.renderItemIn3d(renderType)) {
                            is3d = true;
                        }
                    }
                }
            } catch (Throwable throwable) {
                String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
                Log.get()
                    .error("ItemStack crashed during 1.7.10 3D block check. {}", stackInfo, throwable);
                return;
            }

            ItemStackFastRenderer renderer = new ItemStackFastRenderer(itemStackElement);
            ingredientListSlot.setIngredientRenderer(renderer);
            if (is3d) {
                renderItems3d.add(renderer);
            } else {
                renderItems2d.add(renderer);
            }
            return;
        }

        IngredientRenderer<V> renderer = new IngredientRenderer<>(element);
        ingredientListSlot.setIngredientRenderer(renderer);
        renderOther.add(renderer);
    }

    /**
     * Moves the slots around to fit the given width. Used for tooltip rendering, which can have width resizing.
     *
     * @param maxWidth The maximum width allowed for the grid.
     */
    public void moveSlotsToFit(int maxWidth) {
        if (this.maxWidth / INGREDIENT_WIDTH == maxWidth / INGREDIENT_WIDTH) {
            return;
        }
        int xPos = 0;
        int yPos = 0;
        this.maxWidth = maxWidth;
        width = 0;
        for (List<IngredientListSlot> row : slots) {
            for (IngredientListSlot slot : row) {
                if (xPos >= maxWidth) {
                    xPos = 0;
                    yPos += INGREDIENT_HEIGHT;
                }
                slot.getArea()
                    .setLocation(xPos, yPos);
                xPos += INGREDIENT_WIDTH;
                if (xPos > width) {
                    width = xPos;
                }
            }
            xPos = 0;
            yPos += INGREDIENT_HEIGHT;
        }
        this.height = yPos;
    }

    @Nullable
    public ClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        IngredientRenderer hovered = getHovered(mouseX, mouseY);
        if (hovered != null) {
            IIngredientListElement element = hovered.getElement();
            return ClickedIngredient.create(element.getIngredient(), hovered.getArea());
        }
        return null;
    }

    @Nullable
    public IngredientRenderer getHovered(int mouseX, int mouseY) {
        for (List<IngredientListSlot> row : slots) for (IngredientListSlot slot : row)
            if (slot.isMouseOver(mouseX, mouseY)) return slot.getIngredientRenderer();
        return null;
    }

    public void render(Minecraft minecraft) {
        if (!Config.isEditModeEnabled() && Config.bufferIngredientRenders() && OpenGlHelper.framebufferSupported) {
            if (framebuffer == null) {
                framebuffer = new Framebuffer(minecraft.displayWidth, minecraft.displayHeight, true);
                framebuffer.framebufferColor[0] = 0.0F;
                framebuffer.framebufferColor[1] = 0.0F;
                framebuffer.framebufferColor[2] = 0.0F;
            }
            if (refreshBuffer) {
                framebuffer.createBindFramebuffer(minecraft.displayWidth, minecraft.displayHeight);
                framebuffer.framebufferClear();
                framebuffer.bindFramebuffer(false);
                GlStateManager.disableBlend();
                GlStateManager.tryBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE,
                    GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(
                    GL11.GL_ONE,
                    GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE,
                    GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                framebuffer.bindFramebufferTexture();
                GlStateManager.enableTexture2D();
                ScaledResolution res = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
                double width = res.getScaledWidth();
                double height = res.getScaledHeight();
                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV(0, height, 0.0, 0, 0);
                tessellator.addVertexWithUV(width, height, 0.0, 1, 0);
                tessellator.addVertexWithUV(width, 0, 0.0, 1, 1);
                tessellator.addVertexWithUV(0, 0, 0.0, 0, 1);
                tessellator.draw();
                GlStateManager
                    .tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                return;
            }
        }

        renderImpl(minecraft);

        if (!Config.isEditModeEnabled() && Config.bufferIngredientRenders()
            && refreshBuffer
            && OpenGlHelper.isFramebufferEnabled()) {
            refreshBuffer = false;
            minecraft.getFramebuffer()
                .bindFramebuffer(false);
            // ensure that we actually render the new items
            render(minecraft);
        }
    }

    /**
     * renders all ItemStacks
     */
    private void renderImpl(Minecraft minecraft) {
        RenderHelper.enableGUIStandardItemLighting();

        RenderItem renderItem = RenderItem.getInstance();
        TextureManager textureManager = minecraft.getTextureManager();
        renderItem.zLevel += 50.0F;

        textureManager.bindTexture(TextureMap.locationBlocksTexture);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableLighting();
        for (ItemStackFastRenderer slot : renderItems3d) {
            slot.renderItemAndEffectIntoGUI();
        }

        GlStateManager.disableLighting();
        for (ItemStackFastRenderer slot : renderItems2d) {
            slot.renderItemAndEffectIntoGUI();
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();

        textureManager.bindTexture(TextureMap.locationBlocksTexture);

        renderItem.zLevel -= 50.0F;

        for (ItemStackFastRenderer slot : renderItems3d) {
            slot.renderOverlay();
        }

        for (ItemStackFastRenderer slot : renderItems2d) {
            slot.renderOverlay();
        }

        GlStateManager.disableLighting();

        for (IngredientRenderer slot : renderOther) {
            slot.renderSlow();
        }

        RenderHelper.disableStandardItemLighting();
    }
}
