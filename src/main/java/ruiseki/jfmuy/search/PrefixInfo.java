package ruiseki.jfmuy.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.util.StringUtil;
import ruiseki.jfmuy.util.Translator;

public class PrefixInfo implements Comparable<PrefixInfo> {

    private static final Logger LOGGER = LogManager.getLogger(PrefixInfo.class);

    public static final PrefixInfo NO_PREFIX;
    private static final Char2ObjectMap<PrefixInfo> instances = new Char2ObjectArrayMap<>(6);
    private static final Supplier<ISearchStorageBuilder<IIngredientListElement<?>>> BAKED = BakedSubstringIndexBuilder::new;
    private static final Supplier<ISearchStorageBuilder<IIngredientListElement<?>>> LIMITED_BAKED = () -> new LimitedStringStorageBuilder<IIngredientListElement<?>>(
        BakedSubstringIndexBuilder::new);

    static {
        NO_PREFIX = new PrefixInfo(
            '\0',
            -1,
            true,
            "default",
            () -> Config.SearchMode.ENABLED,
            i -> Collections.singleton(Translator.toLowercaseWithLocale(i.getDisplayName())),
            BAKED);
        addPrefix(
            new PrefixInfo(
                '#',
                0,
                true,
                false,
                "tooltip",
                Config::getTooltipSearchMode,
                IIngredientListElement::getTooltipStrings,
                BAKED));
        addPrefix(
            new PrefixInfo(
                '&',
                1,
                false,
                "resource_id",
                Config::getResourceIdSearchMode,
                e -> Collections.singleton(Translator.toLowercaseWithLocale(e.getResourceId())),
                BAKED));
        addPrefix(
            new PrefixInfo(
                '^',
                2,
                true,
                "color",
                Config::getColorSearchMode,
                IIngredientListElement::getColorStrings,
                LIMITED_BAKED));
        addPrefix(
            new PrefixInfo(
                '$',
                3,
                false,
                "oredict",
                Config::getOreDictSearchMode,
                IIngredientListElement::getOreDictStrings,
                LIMITED_BAKED));
        addPrefix(
            new PrefixInfo(
                '@',
                4,
                false,
                "mod_name",
                Config::getModNameSearchMode,
                IIngredientListElement::getModNameStrings,
                LIMITED_BAKED));
        addPrefix(
            new PrefixInfo(
                '%',
                5,
                true,
                "creative_tab",
                Config::getCreativeTabSearchMode,
                IIngredientListElement::getCreativeTabsStrings,
                LIMITED_BAKED));
    }

    private static void addPrefix(PrefixInfo info) {
        instances.put(info.getPrefix(), info);
    }

    public static Collection<PrefixInfo> all() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public static PrefixInfo get(char ch) {
        return instances.get(ch);
    }

    private final char prefix;
    private final int priority;
    private final boolean potentialDialecticInclusion, async;
    private final String desc;
    private final IModeGetter modeGetter;
    private final IStringsGetter stringsGetter;
    private final Supplier<ISearchStorageBuilder<IIngredientListElement<?>>> storage;

    public PrefixInfo(char prefix, int priority, boolean potentialDialecticInclusion, String desc,
        IModeGetter modeGetter, IStringsGetter stringsGetter,
        Supplier<ISearchStorageBuilder<IIngredientListElement<?>>> storage) {
        this(prefix, priority, potentialDialecticInclusion, true, desc, modeGetter, stringsGetter, storage);
    }

    public PrefixInfo(char prefix, int priority, boolean potentialDialecticInclusion, boolean async, String desc,
        IModeGetter modeGetter, IStringsGetter stringsGetter,
        Supplier<ISearchStorageBuilder<IIngredientListElement<?>>> storage) {
        this.prefix = prefix;
        this.priority = priority;
        this.potentialDialecticInclusion = potentialDialecticInclusion;
        this.async = async;
        this.desc = desc;
        this.modeGetter = modeGetter;
        this.stringsGetter = stringsGetter;
        this.storage = storage;
    }

    public char getPrefix() {
        return prefix;
    }

    public int getPriority() {
        return priority;
    }

    public boolean hasPotentialDialecticInclusion() {
        return potentialDialecticInclusion;
    }

    public boolean isAsyncable() {
        return this.async;
    }

    public String getDesc() {
        return desc;
    }

    public Config.SearchMode getMode() {
        return modeGetter.getMode();
    }

    public ISearchStorageBuilder<IIngredientListElement<?>> createStorageBuilder() {
        return this.storage.get();
    }

    public Collection<String> getStrings(IIngredientListElement<?> element) {
        if (element == null) {
            LOGGER.error("[PrefixInfo:{}] Element truyen vao getStrings bi NULL!", desc);
            return Collections.emptyList();
        }

        try {
            Collection<String> strings = this.stringsGetter.getStrings(element);
            if (strings == null) {
                LOGGER.warn("[PrefixInfo:{}] stringsGetter tra ve NULL cho element: {}", desc, element);
                return Collections.emptyList();
            }

            if (!Config.getSearchStrippedDiacritics() || !this.potentialDialecticInclusion) {
                return strings;
            }

            Collection<String> newStrings = null;
            for (String string : strings) {
                if (string == null) continue;
                for (int i = 0; i < string.length(); i++) {
                    if (string.charAt(i) > 0x7F) {
                        String stripped = StringUtil.stripAccents(string);
                        if (!stripped.equals(string)) {
                            if (newStrings == null) {
                                newStrings = new ArrayList<>(strings);
                            }
                            newStrings.add(stripped);
                        }
                        break;
                    }
                }
            }
            return newStrings == null ? strings : newStrings;
        } catch (Throwable t) {
            LOGGER.error("[PrefixInfo:{}] Loi khi lay chuoi tu element: {}", desc, element, t);
            throw t;
        }
    }

    @Override
    public int compareTo(PrefixInfo o) {
        return Integer.compare(o.priority, this.priority);
    }

    @FunctionalInterface
    public interface IStringsGetter {

        Collection<String> getStrings(IIngredientListElement<?> element);
    }

    @FunctionalInterface
    public interface IModeGetter {

        Config.SearchMode getMode();
    }

    @Override
    public String toString() {
        return "PrefixInfo{" + desc + '}';
    }
}
