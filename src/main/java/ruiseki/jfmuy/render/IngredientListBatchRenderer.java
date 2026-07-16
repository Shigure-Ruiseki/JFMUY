package ruiseki.jfmuy.render;

import static ruiseki.jfmuy.gui.overlay.IngredientGrid.INGREDIENT_HEIGHT;
import static ruiseki.jfmuy.gui.overlay.IngredientGrid.INGREDIENT_WIDTH;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
import ruiseki.jfmuy.ingredients.group.CollapsedGroupIngredient;
import ruiseki.jfmuy.input.ClickedIngredient;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.okcore.client.renderer.GlStateManager;

public class IngredientListBatchRenderer {

    protected final List<List<IngredientListSlot>> slots = new ObjectArrayList<>();

    protected final List<ItemStackFastRenderer> renderItems2d = new ArrayList<>();
    protected final List<ItemStackFastRenderer> renderItems3d = new ArrayList<>();
    protected final List<IngredientRenderer> renderOther = new ArrayList<>();
    protected final List<CollapsedGroupRenderer> renderCollapsed = new ArrayList<>();
    protected final Map<Integer, CollapsedGroupIngredient> collapsedStackIndexed = new HashMap<>();
    protected final Map<IIngredientListElement<?>, CollapsedGroupIngredient> expandedElementToGroup = new HashMap<>();
    // Per-group list of individual slot rectangles (used for per-slot fill + edge-detection border).
    protected final Map<CollapsedGroupIngredient, List<Rectangle>> expandedGroupSlots = new HashMap<>();

    @Nullable
    private Framebuffer framebuffer = null;
    private boolean allowBuffering;
    private boolean refreshBuffer = true;
    protected int size = 0;
    protected int maxSize = 0;
    private int width;
    private int maxWidth;
    private int height;

    public IngredientListBatchRenderer() {
        this(true);
    }

    public IngredientListBatchRenderer(boolean allowBuffering) {
        this.allowBuffering = allowBuffering;
    }

    public void clear() {
        slots.clear();

        renderItems2d.clear();
        renderItems3d.clear();
        renderOther.clear();
        renderCollapsed.clear();
        collapsedStackIndexed.clear();
        expandedElementToGroup.clear();
        expandedGroupSlots.clear();
        size = 0;
        maxSize = 0;

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
        renderCollapsed.clear();
        collapsedStackIndexed.clear();
        expandedElementToGroup.clear();
        expandedGroupSlots.clear();
        maxSize = 0;
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

    /**
     * Sets the grid contents from a collapsed ingredient list (mixed IIngredientListElement and CollapsedStack
     * objects).
     * Collapsed groups are rendered as a single slot; expanded groups have their items rendered individually.
     */
    public void setCollapsed(final int startIndex, List<IIngredientListElement> collapsedList) {
        renderItems2d.clear();
        renderItems3d.clear();
        renderOther.clear();
        renderCollapsed.clear();
        collapsedStackIndexed.clear();
        expandedElementToGroup.clear();
        expandedGroupSlots.clear();
        maxSize = 0;
        size = 0;

        for (List<IngredientListSlot> row : slots) {
            for (IngredientListSlot slot : row) {
                slot.clear();
            }
        }

        // Flatten the ENTIRE collapsed list into display items first, then slice at startIndex.
        // This ensures expanded groups don't break pagination — firstItemIndex is an index into
        // the flattened view, which matches what collapsedSize() now returns.
        List<IIngredientListElement> displayItems = new ArrayList<>();
        Map<IIngredientListElement, CollapsedGroupIngredient> itemToCollapsed = new HashMap<>();
        for (IIngredientListElement obj : collapsedList) {
            if (obj instanceof CollapsedGroupIngredient) {
                CollapsedGroupIngredient collapsed = (CollapsedGroupIngredient) obj;
                if (collapsed.isExpanded()) {
                    // Expanded: add each ingredient individually, track which belong to this group
                    for (IIngredientListElement<?> element : collapsed.getIngredients()) {
                        displayItems.add(element);
                        itemToCollapsed.put(element, collapsed);
                    }
                } else {
                    // Collapsed: add the CollapsedStack itself as a single display item
                    displayItems.add(collapsed);
                }
            } else {
                displayItems.add(obj);
            }
        }

        int i = startIndex;
        int slotIndex = 0;
        for (List<IngredientListSlot> row : slots) {
            maxSize += (int) row.stream()
                .filter(IngredientListSlot::isFree)
                .count();
            for (int column = 0; column < row.size(); column++) {
                if (i >= displayItems.size()) {
                    break;
                }
                IngredientListSlot ingredientListSlot = row.get(column);
                if (ingredientListSlot.isBlocked()) {
                    slotIndex++;
                    continue;
                }
                IIngredientListElement displayItem = displayItems.get(i);
                if (displayItem instanceof CollapsedGroupIngredient) {
                    CollapsedGroupIngredient collapsed = (CollapsedGroupIngredient) displayItem;
                    CollapsedGroupRenderer renderer = new CollapsedGroupRenderer(collapsed);
                    renderer.setArea(ingredientListSlot.getArea());
                    renderer.setPadding(1);
                    renderCollapsed.add(renderer);
                    collapsedStackIndexed.put(slotIndex, collapsed);
                } else {
                    set(ingredientListSlot, displayItem);
                    CollapsedGroupIngredient parentCollapsed = itemToCollapsed.get(displayItem);
                    if (parentCollapsed != null) {
                        collapsedStackIndexed.put(slotIndex, parentCollapsed);
                        expandedElementToGroup.put(displayItem, parentCollapsed);
                        expandedGroupSlots.computeIfAbsent(parentCollapsed, k -> new ArrayList<>())
                            .add(new Rectangle(ingredientListSlot.getArea()));
                    }
                }
                size++;
                i++;
                slotIndex++;
            }
        }

        invalidateBuffer();
    }

    /**
     * Returns the maximum number of ingredients that can be displayed, if none of them ended rows early.
     *
     * @return the maximum number of ingredients.
     */
    public int getMaxSize() {
        return maxSize;
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
            maxSize += (int) row.stream()
                .filter(IngredientListSlot::isFree)
                .count();
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
        // Check collapsed renderers first
        CollapsedGroupRenderer collapsedHovered = getHoveredCollapsed(mouseX, mouseY);
        if (collapsedHovered != null) {
            return collapsedHovered.getClickedIngredient();
        }
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

    @Nullable
    public CollapsedGroupIngredient getExpandedCollapsedGroupAt(int mouseX, int mouseY) {
        IngredientRenderer hovered = getHovered(mouseX, mouseY);
        if (hovered == null) return null;
        return expandedElementToGroup.get(hovered.getElement());
    }

    public void renderExpandedGroupOutlines() {
        if (expandedGroupSlots.isEmpty()) return;
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        int bgColor = 0x33555555; // subtle smoke background
        int borderColor = 0xCC888888; // medium smoke border
        for (List<Rectangle> slots : expandedGroupSlots.values()) {
            // Build a fast lookup set keyed by "x,y" to detect adjacent group slots.
            java.util.Set<String> keys = new java.util.HashSet<>();
            for (Rectangle r : slots) keys.add(r.x + "," + r.y);
            for (Rectangle r : slots) {
                // Subtle background fill over each slot
                Gui.drawRect(r.x, r.y, r.x + r.width, r.y + r.height, bgColor);
                // Draw only the edges that are NOT shared with another group slot
                if (!keys.contains(r.x + "," + (r.y - INGREDIENT_HEIGHT))) {
                    Gui.drawRect(r.x, r.y, r.x + r.width, r.y + 1, borderColor); // top
                }
                if (!keys.contains(r.x + "," + (r.y + INGREDIENT_HEIGHT))) {
                    Gui.drawRect(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height, borderColor); // bottom
                }
                if (!keys.contains((r.x - INGREDIENT_WIDTH) + "," + r.y)) {
                    Gui.drawRect(r.x, r.y, r.x + 1, r.y + r.height, borderColor); // left
                }
                if (!keys.contains((r.x + INGREDIENT_WIDTH) + "," + r.y)) {
                    Gui.drawRect(r.x + r.width - 1, r.y, r.x + r.width, r.y + r.height, borderColor); // right
                }
            }
        }
        GlStateManager.disableBlend();
    }

    @Nullable
    public CollapsedGroupRenderer getHoveredCollapsed(int mouseX, int mouseY) {
        for (CollapsedGroupRenderer renderer : renderCollapsed) {
            if (renderer.isMouseOver(mouseX, mouseY)) {
                return renderer;
            }
        }
        return null;
    }

    public Map<Integer, CollapsedGroupIngredient> getCollapsedStackIndexed() {
        return collapsedStackIndexed;
    }

    public void render(Minecraft minecraft) {
        if (allowBuffering && !Config.isEditModeEnabled()
            && Config.bufferIngredientRenders()
            && OpenGlHelper.framebufferSupported) {
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

        if (allowBuffering && refreshBuffer
            && !Config.isEditModeEnabled()
            && Config.bufferIngredientRenders()
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

        // collapsed group rendering — lighting enabled once for all groups; each renderer
        // assumes it is on and does not toggle it per item.
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        for (CollapsedGroupRenderer collapsed : renderCollapsed) {
            collapsed.render(minecraft);
        }

        RenderHelper.disableStandardItemLighting();
    }
}
