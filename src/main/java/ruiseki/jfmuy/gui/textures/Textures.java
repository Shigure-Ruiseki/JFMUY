package ruiseki.jfmuy.gui.textures;

import net.minecraft.util.ResourceLocation;

import ruiseki.jfmuy.Reference;

public class Textures {

    public static final TextureInfo slot;
    public static final TextureInfo tabSelected;
    public static final TextureInfo tabUnselected;
    public static final TextureInfo buttonDisabled;
    public static final TextureInfo buttonEnabled;
    public static final TextureInfo buttonHighlight;
    public static final TextureInfo guiBackground;
    public static final TextureInfo recipeBackground;
    public static final TextureInfo favoriteDisabled;
    public static final TextureInfo favoriteEnabled;
    public static final TextureInfo recipeBookmarkIcon;
    public static final TextureInfo searchBackground;
    public static final TextureInfo shapelessIcon;
    public static final TextureInfo arrowPrevious;
    public static final TextureInfo arrowNext;
    public static final TextureInfo recipeTransfer;
    public static final TextureInfo configButtonIcon;
    public static final TextureInfo configButtonCheatIcon;
    public static final TextureInfo bookmarkButtonDisabledIcon;
    public static final TextureInfo bookmarkButtonEnabledIcon;
    public static final TextureInfo infoIcon;
    public static final TextureInfo catalystTab;
    public static final TextureInfo flameIcon;
    public static final TextureInfo searchIcon;

    static {
        slot = registerGuiSprite("slot", 18, 18).slice(4, 4, 4, 4);
        tabSelected = registerGuiSprite("tab_selected", 24, 24);
        tabUnselected = registerGuiSprite("tab_unselected", 24, 24);
        buttonDisabled = registerGuiSprite("button_disabled", 20, 20).slice(2, 2, 2, 2);
        buttonEnabled = registerGuiSprite("button_enabled", 20, 20).slice(2, 2, 2, 2);
        buttonHighlight = registerGuiSprite("button_highlight", 20, 20).slice(2, 2, 2, 2);
        guiBackground = registerGuiSprite("gui_background", 64, 64).slice(16, 16, 16, 16);
        recipeBackground = registerGuiSprite("single_recipe_background", 64, 64).slice(16, 16, 16, 16);
        searchBackground = registerGuiSprite("search_background", 20, 20).slice(4, 4, 4, 4);
        catalystTab = registerGuiSprite("catalyst_tab", 28, 28).slice(8, 9, 8, 8);

        shapelessIcon = registerGuiSprite("icons/shapeless_icon", 36, 36).trim(1, 2, 1, 1);
        arrowPrevious = registerGuiSprite("icons/arrow_previous", 9, 9).trim(0, 0, 1, 1);
        arrowNext = registerGuiSprite("icons/arrow_next", 9, 9).trim(0, 0, 1, 1);
        recipeTransfer = registerGuiSprite("icons/recipe_transfer", 7, 7);
        favoriteDisabled = registerGuiSprite("icons/favorite_disabled", 7, 7);
        favoriteEnabled = registerGuiSprite("icons/favorite_enabled", 7, 7);
        recipeBookmarkIcon = registerGuiSprite("icons/recipe_bookmark_icon", 7, 7);

        configButtonIcon = registerGuiSprite("icons/config_button", 16, 16);
        configButtonCheatIcon = registerGuiSprite("icons/config_button_cheat", 16, 16);
        bookmarkButtonDisabledIcon = registerGuiSprite("icons/bookmark_button_disabled", 16, 16);
        bookmarkButtonEnabledIcon = registerGuiSprite("icons/bookmark_button_enabled", 16, 16);
        infoIcon = registerGuiSprite("icons/info", 16, 16);
        flameIcon = registerGuiSprite("icons/flame", 14, 14);
        searchIcon = registerGuiSprite("icons/search", 8, 8);
    }

    private static TextureInfo registerGuiSprite(String name, int width, int height) {
        ResourceLocation location = new ResourceLocation(Reference.MOD_ID, "textures/gui/" + name + ".png");
        return new TextureInfo(location, width, height);
    }

    private Textures() {}
}
