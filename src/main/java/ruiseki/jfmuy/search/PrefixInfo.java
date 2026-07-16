package ruiseki.jfmuy.search;

import java.util.Collection;
import java.util.Collections;

import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.util.Translator;

public class PrefixInfo {

    public static final PrefixInfo NO_PREFIX = new PrefixInfo();

    private final IModeGetter modeGetter;
    private final IStringsGetter stringsGetter;

    private PrefixInfo() {
        this.modeGetter = () -> Config.SearchMode.ENABLED;
        this.stringsGetter = element -> Collections
            .singleton(Translator.toLowercaseWithLocale(element.getDisplayName()));
    }

    public PrefixInfo(IModeGetter modeGetter, IStringsGetter stringsGetter) {
        this.modeGetter = modeGetter;
        this.stringsGetter = stringsGetter;
    }

    public Config.SearchMode getMode() {
        return modeGetter.getMode();
    }

    public Collection<String> getStrings(IIngredientListElement<?> element) {
        return this.stringsGetter.getStrings(element);
    }

    @FunctionalInterface
    public interface IStringsGetter {

        Collection<String> getStrings(IIngredientListElement<?> element);
    }

    @FunctionalInterface
    public interface IModeGetter {

        Config.SearchMode getMode();
    }

}
