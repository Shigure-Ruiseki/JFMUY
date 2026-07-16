package ruiseki.jfmuy.config;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.util.GiveMode;

public class ConfigValues {

    // advanced
    public boolean debugModeEnabled = false;
    public boolean centerSearchBarEnabled = false;
    public boolean ultraLowMemoryUsage = true;
    public boolean asyncSearchTreeBuilding = true;
    public boolean addBookmarksToFront = true;
    public GiveMode giveMode = GiveMode.MOUSE_PICKUP;
    public String modNameFormat = Config.parseFriendlyModNameFormat(Config.defaultModNameFormatFriendly);
    public int maxColumns = 9;
    public int maxRecipeGuiHeight = 350;

    // search
    public Config.SearchMode modNameSearchMode = Config.SearchMode.REQUIRE_PREFIX;
    public Config.SearchMode tooltipSearchMode = Config.SearchMode.ENABLED;
    public Config.SearchMode oreDictSearchMode = Config.SearchMode.DISABLED;
    public Config.SearchMode creativeTabSearchMode = Config.SearchMode.DISABLED;
    public Config.SearchMode colorSearchMode = Config.SearchMode.DISABLED;
    public Config.SearchMode resourceIdSearchMode = Config.SearchMode.DISABLED;
    public boolean searchAdvancedTooltips = false;
    public boolean searchStrippedDiacritics = false;

    // per-world
    public boolean overlayEnabled = true;
    public boolean cheatItemsEnabled = false;
    public boolean editModeEnabled = false;
    public boolean bookmarkOverlayEnabled = true;
    public String filterText = "";
    public ItemStack defaultFluidContainerItem = new ItemStack(Items.bucket);

    // rendering
    public boolean bufferIngredientRenders = false;

    // misc
    public boolean mouseClickToSeeRecipes = true;
    public boolean tooltipShowRecipeBy = true;
    public boolean showHiddenIngredientsInCreative = false;
    public boolean skipShowingProgressBar = false;
}
