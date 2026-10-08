package ruiseki.jfmuy.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.util.CollapsedClickAction;
import ruiseki.jfmuy.util.GiveMode;

public class ConfigValues {

    // advanced
    public boolean debugModeEnabled = false;
    public boolean centerSearchBarEnabled = true;
    public boolean ultraLowMemoryUsage = false;
    public boolean asyncSearchTreeBuilding = true;
    public boolean addBookmarksToFront = true;
    public GiveMode giveMode = GiveMode.INVENTORY;
    public String modNameFormat = Config.parseFriendlyModNameFormat(Config.defaultModNameFormatFriendly);
    public int maxColumns = 9;
    public int maxRecipeGuiHeight = 350;
    public int recipeBookmarkGroupColor = 0x9F00FF00;

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
    public boolean recipeBookmarksEnabled = true;
    public boolean autocraftingEnabled = true;
    public String filterText = "";
    public ItemStack defaultFluidContainerItem = new ItemStack(Items.bucket);

    // rendering
    public boolean bufferIngredientRenders = false;

    // misc
    public boolean mouseClickToSeeRecipes = true;
    public boolean holdToDragGhostIngredients = false;
    public boolean tooltipShowRecipeBy = true;
    public boolean recipeIngredientPreviewEnabled = true;
    public boolean showHiddenIngredientsInCreative = false;
    public boolean skipShowingProgressBar = true;
    public Config.ButtonPosition configButtonPosition = Config.ButtonPosition.RIGHT;
    public boolean hideBottomLeftCornerBookmarkButton = false;
    public int historyRows = 2;
    public Config.HistoryPosition historyPosition = Config.HistoryPosition.RIGHT;
    public boolean historyMatchNbt = true;
    public boolean versionChecker = true;

    // category
    public List<String> categoryUidOrder = new ArrayList<>();
    public Set<String> disabledRecipeCategoryUids = new HashSet<>();

    // collapsible groups
    public boolean collapsibleGroupsEnabled = true;
    public boolean collapseOnClose = false;
    public CollapsedClickAction collapsedClickAction = CollapsedClickAction.OPEN_GROUP;
    public Set<String> disabledGroups = new HashSet<>();
}
