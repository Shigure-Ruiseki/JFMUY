package ruiseki.jfmuy.gui;

import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.gui.ICraftingGridHelper;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.gui.IDrawableAnimated;
import ruiseki.jfmuy.api.gui.IDrawableBuilder;
import ruiseki.jfmuy.api.gui.IDrawableStatic;
import ruiseki.jfmuy.api.gui.ITickTimer;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.gui.elements.DrawableAnimated;
import ruiseki.jfmuy.gui.elements.DrawableBlank;
import ruiseki.jfmuy.gui.elements.DrawableBuilder;
import ruiseki.jfmuy.gui.elements.DrawableIngredient;
import ruiseki.jfmuy.gui.elements.DrawableNineSliceTexture;
import ruiseki.jfmuy.gui.elements.DrawableSprite;
import ruiseki.jfmuy.gui.textures.TextureInfo;
import ruiseki.jfmuy.gui.textures.Textures;
import ruiseki.jfmuy.util.ErrorUtil;

public class GuiHelper implements IGuiHelper {

    private final IIngredientRegistry ingredientRegistry;
    private final IDrawableStatic slotDrawable;
    private final IDrawableStatic tabSelected;
    private final IDrawableStatic tabUnselected;
    private final IDrawableStatic shapelessIcon;
    private final IDrawableStatic arrowPrevious;
    private final IDrawableStatic arrowNext;
    private final IDrawableStatic recipeTransfer;
    private final IDrawableStatic favoriteDisabled;
    private final IDrawableStatic favoriteEnabled;
    private final IDrawableStatic recipeBookmarkIcon;
    private final IDrawableStatic configButtonIcon;
    private final IDrawableStatic configButtonCheatIcon;
    private final IDrawableStatic bookmarkButtonDisabledIcon;
    private final IDrawableStatic bookmarkButtonEnabledIcon;
    private final DrawableNineSliceTexture buttonDisabled;
    private final DrawableNineSliceTexture buttonEnabled;
    private final DrawableNineSliceTexture buttonHighlight;
    private final DrawableNineSliceTexture guiBackground;
    private final DrawableNineSliceTexture recipeBackground;
    private final DrawableNineSliceTexture searchBackground;
    private final DrawableNineSliceTexture catalystTab;
    private final DrawableNineSliceTexture nineSliceSlot;
    private final IDrawableStatic infoIcon;
    private final IDrawableStatic flameIcon;

    public GuiHelper(IIngredientRegistry ingredientRegistry) {
        this.ingredientRegistry = ingredientRegistry;
        this.slotDrawable = createDrawable(Textures.slot);
        this.nineSliceSlot = createNineSliceDrawable(Textures.slot);

        this.tabSelected = createDrawable(Textures.tabSelected);
        this.tabUnselected = createDrawable(Textures.tabUnselected);

        this.buttonDisabled = createNineSliceDrawable(Textures.buttonDisabled);
        this.buttonEnabled = createNineSliceDrawable(Textures.buttonEnabled);
        this.buttonHighlight = createNineSliceDrawable(Textures.buttonHighlight);
        this.guiBackground = createNineSliceDrawable(Textures.guiBackground);
        this.recipeBackground = createNineSliceDrawable(Textures.recipeBackground);
        this.searchBackground = createNineSliceDrawable(Textures.searchBackground);
        this.catalystTab = createNineSliceDrawable(Textures.catalystTab);

        this.shapelessIcon = createDrawable(Textures.shapelessIcon);
        this.arrowPrevious = createDrawable(Textures.arrowPrevious);
        this.arrowNext = createDrawable(Textures.arrowNext);
        this.recipeTransfer = createDrawable(Textures.recipeTransfer);
        this.favoriteDisabled = createDrawable(Textures.favoriteDisabled);
        this.favoriteEnabled = createDrawable(Textures.favoriteEnabled);
        this.recipeBookmarkIcon = createDrawable(Textures.recipeBookmarkIcon);

        this.configButtonIcon = createDrawable(Textures.configButtonIcon);
        this.configButtonCheatIcon = createDrawable(Textures.configButtonCheatIcon);
        this.bookmarkButtonDisabledIcon = createDrawable(Textures.bookmarkButtonDisabledIcon);
        this.bookmarkButtonEnabledIcon = createDrawable(Textures.bookmarkButtonEnabledIcon);

        this.infoIcon = createDrawable(Textures.infoIcon);
        this.flameIcon = createDrawable(Textures.flameIcon);
    }

    @Override
    public IDrawableBuilder drawableBuilder(ResourceLocation resourceLocation, int u, int v, int width, int height) {
        return new DrawableBuilder(resourceLocation, u, v, width, height);
    }

    @Override
    public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, int ticksPerCycle,
        IDrawableAnimated.StartDirection startDirection, boolean inverted) {
        ErrorUtil.checkNotNull(drawable, "drawable");
        ErrorUtil.checkNotNull(startDirection, "startDirection");
        return new DrawableAnimated(drawable, ticksPerCycle, startDirection, inverted);
    }

    @Override
    public IDrawableStatic getSlotDrawable() {
        return slotDrawable;
    }

    @Override
    public IDrawableStatic createBlankDrawable(int width, int height) {
        return new DrawableBlank(width, height);
    }

    @Override
    public <V> IDrawable createDrawableIngredient(V ingredient) {
        IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
        return new DrawableIngredient<>(ingredient, ingredientRenderer);
    }

    @Override
    public ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot) {
        return new CraftingGridHelper(craftInputSlot1, craftOutputSlot);
    }

    @Override
    public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
        return new TickTimer(ticksPerCycle, maxValue, countDown);
    }

    private IDrawableStatic createDrawable(TextureInfo textureInfo) {
        return new DrawableSprite(textureInfo);
    }

    private DrawableNineSliceTexture createNineSliceDrawable(TextureInfo textureInfo) {
        return new DrawableNineSliceTexture(textureInfo);
    }

    public IDrawableStatic getTabSelected() {
        return tabSelected;
    }

    public IDrawableStatic getTabUnselected() {
        return tabUnselected;
    }

    public IDrawableStatic getShapelessIcon() {
        return shapelessIcon;
    }

    public IDrawableStatic getArrowPrevious() {
        return arrowPrevious;
    }

    public IDrawableStatic getArrowNext() {
        return arrowNext;
    }

    public IDrawableStatic getRecipeTransfer() {
        return recipeTransfer;
    }

    public IDrawableStatic getFavoriteDisabled() {
        return favoriteDisabled;
    }

    public IDrawableStatic getFavoriteEnabled() {
        return favoriteEnabled;
    }

    public IDrawableStatic getRecipeBookmarkIcon() {
        return recipeBookmarkIcon;
    }

    public IDrawableStatic getConfigButtonIcon() {
        return configButtonIcon;
    }

    public IDrawableStatic getConfigButtonCheatIcon() {
        return configButtonCheatIcon;
    }

    public IDrawableStatic getBookmarkButtonDisabledIcon() {
        return bookmarkButtonDisabledIcon;
    }

    public IDrawableStatic getBookmarkButtonEnabledIcon() {
        return bookmarkButtonEnabledIcon;
    }

    public DrawableNineSliceTexture getButtonDisabled() {
        return buttonDisabled;
    }

    public DrawableNineSliceTexture getButtonEnabled() {
        return buttonEnabled;
    }

    public DrawableNineSliceTexture getButtonHighlight() {
        return buttonHighlight;
    }

    public DrawableNineSliceTexture getButtonForState(int state) {
        if (state == 0) {
            return getButtonDisabled();
        } else if (state == 2) {
            return getButtonHighlight();
        } else {
            return getButtonEnabled();
        }
    }

    public DrawableNineSliceTexture getGuiBackground() {
        return guiBackground;
    }

    public DrawableNineSliceTexture getRecipeBackground() {
        return recipeBackground;
    }

    public DrawableNineSliceTexture getSearchBackground() {
        return searchBackground;
    }

    public IDrawableStatic getInfoIcon() {
        return infoIcon;
    }

    public DrawableNineSliceTexture getCatalystTab() {
        return catalystTab;
    }

    public DrawableNineSliceTexture getNineSliceSlot() {
        return nineSliceSlot;
    }

    public IDrawableStatic getFlameIcon() {
        return flameIcon;
    }
}
