package ruiseki.jfmuy.search;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import cpw.mods.fml.common.ProgressManager;
import ruiseki.jfmuy.api.search.ISearchIndex;
import ruiseki.jfmuy.api.search.ISearchIndexBuilder;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.util.LoggedTimer;
import ruiseki.okcore.datastructure.NonNullList;

public class PrefixedSearchable implements ISearchable<IIngredientListElement<?>>, IBuildable {

    protected final ISearchIndexBuilder<IIngredientListElement<?>> searchIndexBuilder;
    protected final PrefixInfo prefixInfo;

    /**
     * Null until {@link #build()} bakes the builder's contents.
     * Submits that arrive afterwards go straight to the storage, which handles them itself.
     */
    @Nullable
    protected volatile ISearchIndex<IIngredientListElement<?>> searchIndex;

    protected LoggedTimer timer;

    public PrefixedSearchable(ISearchIndexBuilder<IIngredientListElement<?>> searchIndexBuilder,
        PrefixInfo prefixInfo) {
        this.searchIndexBuilder = searchIndexBuilder;
        this.prefixInfo = prefixInfo;
    }

    @Nullable
    public ISearchIndex<IIngredientListElement<?>> getSearchIndex() {
        return searchIndex;
    }

    public Collection<String> getStrings(IIngredientListElement<?> element) {
        return prefixInfo.getStrings(element);
    }

    @Override
    public Config.SearchMode getMode() {
        return prefixInfo.getMode();
    }

    @Override
    public void submit(IIngredientListElement<?> ingredient) {
        if (prefixInfo.getMode() == Config.SearchMode.DISABLED) {
            return;
        }
        Collection<String> strings = prefixInfo.getStrings(ingredient);
        ISearchIndex<IIngredientListElement<?>> index = this.searchIndex;
        for (String string : strings) {
            if (index == null) {
                searchIndexBuilder.put(string, ingredient);
            } else {
                index.put(string, ingredient);
            }
        }
    }

    @Override
    public void submitAll(NonNullList<IIngredientListElement> ingredients) {
        if (prefixInfo.getMode() == Config.SearchMode.DISABLED) {
            return;
        }
        if (IngredientFilter.firstBuild) {
            start();
            ProgressManager.ProgressBar progressBar = null;
            if (!IngredientFilter.rebuild) {
                long modNameCount = ingredients.stream()
                    .map(IIngredientListElement::getModNameForSorting)
                    .distinct()
                    .count();
                if (!Config.skipShowingProgressBar()) {
                    progressBar = ProgressManager.push("Indexing ingredients", (int) modNameCount);
                }
            }
            String currentModName = null;
            for (IIngredientListElement ingredient : ingredients) {
                String modname = ingredient.getModNameForSorting();
                if (!Objects.equals(currentModName, modname)) {
                    currentModName = modname;
                    if (progressBar != null) {
                        progressBar.step(modname);
                    }
                }
                submit(ingredient);
            }
            if (progressBar != null) {
                ProgressManager.pop(progressBar);
            }
            stop();
        } else {
            ProgressManager.ProgressBar progressBar = ProgressManager
                .push("Adding ingredients at runtime", ingredients.size());
            for (IIngredientListElement ingredient : ingredients) {
                progressBar.step(ingredient.getDisplayName());
                submit(ingredient);
            }
            ProgressManager.pop(progressBar);
        }
    }

    @Override
    public void getSearchResults(String token, Set<IIngredientListElement<?>> results) {
        ISearchIndex<IIngredientListElement<?>> index = this.searchIndex;
        if (index != null) {
            index.getSearchResults(token, results);
        }
    }

    @Override
    public void getAllElements(Set<IIngredientListElement<?>> results) {
        ISearchIndex<IIngredientListElement<?>> index = this.searchIndex;
        if (index != null) {
            index.getAllElements(results);
        }
    }

    /**
     * Bakes everything submitted so far into the search storage. Idempotent:
     * once built, later submits are handled by the storage itself.
     */
    @Override
    public void build() {
        if (this.searchIndex == null) {
            this.searchIndex = this.searchIndexBuilder.build();
        }
    }

    @Override
    public void start() {
        this.timer = new LoggedTimer();
        this.timer.start("Building [" + prefixInfo.getDesc() + "] search index");
    }

    @Override
    public void stop() {
        build();
        if (this.timer != null) {
            this.timer.stop();
            this.timer = null;
        }
    }

}
