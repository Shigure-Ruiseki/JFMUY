package ruiseki.jfmuy.ingredients;

import java.util.Collection;

import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.suffixtree.GeneralizedSuffixTree;

class PrefixedSearchTree {

    private final GeneralizedSuffixTree tree;
    private final IStringsGetter stringsGetter;
    private final IModeGetter modeGetter;

    public PrefixedSearchTree(GeneralizedSuffixTree tree, IStringsGetter stringsGetter, IModeGetter modeGetter) {
        this.tree = tree;
        this.stringsGetter = stringsGetter;
        this.modeGetter = modeGetter;
    }

    public GeneralizedSuffixTree getTree() {
        return tree;
    }

    public IStringsGetter getStringsGetter() {
        return stringsGetter;
    }

    public Config.SearchMode getMode() {
        return modeGetter.getMode();
    }

    @FunctionalInterface
    interface IStringsGetter {

        Collection<String> getStrings(IIngredientListElement<?> element);
    }

    @FunctionalInterface
    interface IModeGetter {

        Config.SearchMode getMode();
    }
}
