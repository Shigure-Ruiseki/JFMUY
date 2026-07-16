package ruiseki.jfmuy.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import ruiseki.jfmuy.ClientProxy;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRenderer;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.startup.IModIdHelper;
import ruiseki.jfmuy.util.LegacyUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.StringUtil;
import ruiseki.jfmuy.util.Translator;

public class IngredientListElement<V> implements IIngredientListElement<V> {

    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

    private final V ingredient;
    private final int orderIndex;
    private final IIngredientHelper<V> ingredientHelper;
    private final IIngredientRenderer<V> ingredientRenderer;
    private final Object modIds; // Can be String or String[]
    private final Object modNames; // Can be String or String[]

    private boolean visible = true;

    @Nullable
    public static <V> IngredientListElement<V> create(V ingredient, IIngredientHelper<V> ingredientHelper,
        IIngredientRenderer<V> ingredientRenderer, IModIdHelper modIdHelper, int orderIndex) {
        try {
            return new IngredientListElement<>(
                ingredient,
                orderIndex,
                ingredientHelper,
                ingredientRenderer,
                modIdHelper);
        } catch (RuntimeException e) {
            try {
                String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
                Log.get()
                    .warn("Found a broken ingredient {}", ingredientInfo, e);
            } catch (RuntimeException e2) {
                Log.get()
                    .warn("Found a broken ingredient.", e2);
            }
            return null;
        }
    }

    protected IngredientListElement(V ingredient, int orderIndex, IIngredientHelper<V> ingredientHelper,
        IIngredientRenderer<V> ingredientRenderer, IModIdHelper modIdHelper) {
        this.ingredient = ingredient;
        this.orderIndex = orderIndex;
        this.ingredientHelper = ingredientHelper;
        this.ingredientRenderer = ingredientRenderer;
        String displayModId = ingredientHelper.getDisplayModId(ingredient);
        String modId = ingredientHelper.getModId(ingredient);
        if (modId.equals(displayModId)) {
            this.modIds = StringUtil.intern(modId);
            this.modNames = StringUtil.intern(modIdHelper.getModNameForModId(modId));
        } else {
            this.modIds = new String[] { StringUtil.intern(modId), StringUtil.intern(displayModId) };
            String modIdName = modIdHelper.getModNameForModId(modId);
            String displayModIdName = modIdHelper.getModNameForModId(displayModId);
            if (modIdName.equals(displayModIdName)) {
                this.modNames = StringUtil.intern(modIdName);
            } else {
                this.modNames = new String[] { StringUtil.intern(modIdName), StringUtil.intern(displayModIdName) };
            }
        }
    }

    @Override
    public final V getIngredient() {
        return ingredient;
    }

    @Override
    public int getOrderIndex() {
        return orderIndex;
    }

    @Override
    public IIngredientHelper<V> getIngredientHelper() {
        return ingredientHelper;
    }

    @Override
    public IIngredientRenderer<V> getIngredientRenderer() {
        return ingredientRenderer;
    }

    @Override
    public final String getDisplayName() {
        return IngredientInformation.getDisplayName(ingredient, ingredientHelper);
    }

    @Override
    public String getModNameForSorting() {
        return this.modNames instanceof String ? (String) this.modNames : ((String[]) this.modNames)[0];
    }

    @Override
    public Set<String> getModNameStrings() {
        Set<String> modNameStrings = new ObjectArraySet<>();
        if (this.modIds instanceof String) {
            addModIdStrings(modNameStrings, (String) this.modIds);
        } else {
            String[] modIdsCasted = (String[]) this.modIds;
            for (String modId : modIdsCasted) {
                addModIdStrings(modNameStrings, modId);
            }
        }
        if (this.modNames instanceof String) {
            addModNameStrings(modNameStrings, (String) this.modNames);
        } else {
            String[] modNamesCasted = (String[]) this.modNames;
            for (String modName : modNamesCasted) {
                addModNameStrings(modNameStrings, modName);
            }
        }
        return modNameStrings;
    }

    private static void addModIdStrings(Set<String> modNames, String modId) {
        String modIdNoSpaces = SPACE_PATTERN.matcher(modId)
            .replaceAll("");
        modNames.add(modId);
        modNames.add(modIdNoSpaces);
    }

    private static void addModNameStrings(Set<String> modNames, String modName) {
        String modNameLowercase = modName.toLowerCase(Locale.ENGLISH);
        String modNameNoSpaces = SPACE_PATTERN.matcher(modNameLowercase)
            .replaceAll("");
        modNames.add(modNameNoSpaces);
    }

    @Override
    public final List<String> getTooltipStrings() {
        String modId = this.modIds instanceof String ? (String) this.modIds : ((String[]) this.modIds)[0];
        String modName = this.modNames instanceof String ? (String) this.modNames : ((String[]) this.modNames)[0];
        String modNameLowercase = modName.toLowerCase(Locale.ENGLISH);
        String displayNameLowercase = Translator.toLowercaseWithLocale(this.getDisplayName());
        return IngredientInformation.getTooltipStrings(
            ingredient,
            ingredientRenderer,
            ImmutableSet.of(modId, modNameLowercase, displayNameLowercase, this.getResourceId()));
    }

    @Override
    public Collection<String> getOreDictStrings() {
        Collection<String> oreDictNames = ingredientHelper.getOreDictNames(ingredient);
        return oreDictNames.stream()
            .map(s -> s.toLowerCase(Locale.ENGLISH))
            .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getCreativeTabsStrings() {
        Collection<String> creativeTabsStrings = ingredientHelper.getCreativeTabNames(ingredient);
        return creativeTabsStrings.stream()
            .map(Translator::toLowercaseWithLocale)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getColorStrings() {
        return IngredientInformation.getColorStrings(ingredient, ingredientHelper);
    }

    @Override
    public String getResourceId() {
        return LegacyUtil.getResourceId(ingredient, ingredientHelper);
    }

    @Override
    public int getOrdinal() {
        return ingredientHelper.getOrdinal(ingredient);
    }

    @Override
    public boolean isVisible() {
        if (visible) {
            return true;
        }
        if (FMLLaunchHandler.side()
            .isClient()) {
            return Config.getShowHiddenIngredientsInCreative() && ClientProxy.isCreative();
        }
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
