package ruiseki.jfmuy;

import ruiseki.jfmuy.util.Translator;

public class Reference {

    public static final String MOD_ID = Tags.MOD_ID;
    public static final String MOD_NAME = Tags.MOD_NAME;
    public static final String VERSION = Tags.VERSION;
    public static final String DEPENDENCIES = "required-after:okcore@[26.07.14.1,);";

    public static final String PROXY_COMMON = Tags.MOD_GROUP + ".CommonProxy";
    public static final String PROXY_CLIENT = Tags.MOD_GROUP + ".ClientProxy";
    public static final String GUI_FACTORY = Tags.MOD_GROUP + ".config.JFMUYModGuiFactory";

    public static final String PREFIX_MOD = MOD_ID + ":";
    public static final String TEXTURE_GUI_PATH = "textures/gui/";
    public static final String TEXTURE_RECIPE_BACKGROUND_PATH = TEXTURE_GUI_PATH + "recipeBackground2.png";
    public static final String TEXTURE_RECIPE_BACKGROUND_TALL_PATH = TEXTURE_GUI_PATH + "recipeBackgroundTall2.png";

    public static final int MAX_TOOLTIP_WIDTH = 125;

    public static final String RECIPE_TRANSFER_TOOLTIP = Translator.translateToLocal("jfmuy.tooltip.transfer");

    public static final String UNIVERSAL_RECIPE_TRANSFER_UID = "universal recipe transfer handler";

    public static final String MINECRAFT_MOD_NAME = "minecraft";
}
